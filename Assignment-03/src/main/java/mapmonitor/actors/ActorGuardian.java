package mapmonitor.actors;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import mapmonitor.common.UtilityValues;
import mapmonitor.common.StateGuardian;
import mapmonitor.messages.CheckStatusMsg;
import mapmonitor.messages.ConsensusValuesMsg;
import mapmonitor.messages.ConsensusWarningMsg;
import mapmonitor.messages.DetectedOutMsg;
import mapmonitor.messages.DetectedValueMsg;
import mapmonitor.messages.GuardianStateMsg;
import mapmonitor.messages.RecoveryPatchMsg;
import mapmonitor.messages.TopicsClusterMessages;

public class ActorGuardian extends AbstractActorWithStash {

	private final int supervisedPatch;
	private final String hostAddress;
	private AbstractActor.Receive normalBehavior, checkBehavior, dangerBehavior;
	private Map<String, Map<ActorRef, Double>> sensors;
	private StateGuardian currentState;
	private int consensusRound = 0; // Used for crash model algorithm implementation
	private List<StateGuardian> sendedValues, notSendedValues; // Used for crash model algorithm implementation
	private Cancellable thresholdExceededTask;
	private Cluster cluster;
	private ActorRef mediator;

	public ActorGuardian(String address, int patch) {
		hostAddress = address;
		supervisedPatch = patch;
		normalBehavior = setupNormalBehaviour();
		checkBehavior = setupCheckBehaviour();
		dangerBehavior = setupDangerBehaviour();
		sensors = new HashMap<>();
		currentState = StateGuardian.NORMAL;
		sendedValues = new ArrayList<>();
		notSendedValues = new ArrayList<>();
		consensusRound = 0;
	}

	@Override
	public void preStart() throws Exception {
		cluster = Cluster.get(getContext().system());
		mediator = DistributedPubSub.get(getContext().system()).mediator();
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
		mediator.tell(new DistributedPubSubMediator.Subscribe(TopicsClusterMessages.SENSOR_VALUE_IN_PATCH + supervisedPatch, getSelf()), getSelf());
		mediator.tell(new DistributedPubSubMediator.Subscribe(TopicsClusterMessages.SENSOR_OUT_FROM_PATCH + supervisedPatch, getSelf()), getSelf());
		mediator.tell(new DistributedPubSubMediator.Subscribe(TopicsClusterMessages.CONSENSUS_WARNING_PATCH + supervisedPatch, getSelf()), getSelf());
		mediator.tell(new DistributedPubSubMediator.Subscribe(TopicsClusterMessages.CONSENSUS_VALUES_PATCH + supervisedPatch, getSelf()), getSelf());
		mediator.tell(new DistributedPubSubMediator.Subscribe(TopicsClusterMessages.RECOVERY_PATCH + supervisedPatch, getSelf()), getSelf());
		mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.GUARDIAN_STATE, new GuardianStateMsg(hostAddress, currentState, supervisedPatch)), getSelf());
	}

	private AbstractActor.Receive setupNormalBehaviour() {
		return receiveBuilder().match(DistributedPubSubMediator.SubscribeAck.class, msg -> {
			// Silently ignored (Subscription OK)
		}).match(DetectedValueMsg.class, msg -> {
			if (!sensors.containsKey(msg.getSenderAddress())) {
				sensors.put(msg.getSenderAddress(), new HashMap<>());
			}
			sensors.get(msg.getSenderAddress()).put(getSender(), msg.getValue());
			evaluateSensorValues();
		}).match(DetectedOutMsg.class, msg -> {
			if (sensors.containsKey(msg.getSenderAddress()) && sensors.get(msg.getSenderAddress()).containsKey(getSender())) {
				sensors.get(msg.getSenderAddress()).remove(getSender());
				evaluateSensorValues();
			}
		}).match(MemberUp.class, msg -> {
			mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.GUARDIAN_STATE, new GuardianStateMsg(hostAddress, currentState, supervisedPatch)), getSelf());
		}).match(MemberRemoved.class, msg -> {
			if (sensors.containsKey(msg.member().address().toString())) {
				sensors.remove(msg.member().address().toString());
				evaluateSensorValues();
			}
		}).match(UnreachableMember.class, msg -> {
			if (sensors.containsKey(msg.member().address().toString())) {
				sensors.remove(msg.member().address().toString());
				evaluateSensorValues();
			}
		}).match(ConsensusWarningMsg.class, msg -> {
			getContext().become(checkBehavior, true);
			getSelf().tell(new CheckStatusMsg(), getSelf());
			notSendedValues.add(currentState);
		}).build();
	}

	private void evaluateSensorValues() {
		try {
			double avg = sensors.values().stream().map(x -> x.values()).flatMap(x -> x.stream()).mapToDouble(x -> x).average().getAsDouble();
			if (avg > UtilityValues.THRESHOLD && currentState.equals(StateGuardian.NORMAL) && thresholdExceededTask == null) {
				thresholdExceededTask = getContext().getSystem().getScheduler().scheduleOnce(Duration.ofMillis(UtilityValues.K_MILLIS), () -> {
					thresholdExceededTask = null;
					currentState = StateGuardian.WARNING;
					mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.GUARDIAN_STATE, new GuardianStateMsg(hostAddress, currentState, supervisedPatch)), getSelf());
					mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.CONSENSUS_WARNING_PATCH + supervisedPatch, new ConsensusWarningMsg()), getSelf());
				}, getContext().dispatcher());
			}
			if (avg <= UtilityValues.THRESHOLD && thresholdExceededTask != null) {
				thresholdExceededTask.cancel();
				thresholdExceededTask = null;
			}
			if (avg <= UtilityValues.THRESHOLD && !currentState.equals(StateGuardian.NORMAL)) {
				currentState = StateGuardian.NORMAL;
				mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.GUARDIAN_STATE, new GuardianStateMsg(hostAddress, currentState, supervisedPatch)), getSelf());
			}	
		} catch (NoSuchElementException e) { // No stored value for sensors
			currentState = StateGuardian.NORMAL;
			mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.GUARDIAN_STATE, new GuardianStateMsg(hostAddress, currentState, supervisedPatch)), getSelf());
		}
	}

	private AbstractActor.Receive setupCheckBehaviour() {
		return receiveBuilder().match(CheckStatusMsg.class, msg -> {
			consensusRound++;
			if (consensusRound > UtilityValues.FAILURES + 1) {
				consensusRound = 0;
				sendedValues.addAll(notSendedValues);
				AbstractActor.Receive nextBehaviour;
				int nWarning = (int) sendedValues.stream().filter(x -> x.equals(StateGuardian.WARNING)).count();
				if (nWarning > sendedValues.size() - nWarning) {
					nextBehaviour = dangerBehavior;
					currentState = StateGuardian.DANGER;	
				} else {
					nextBehaviour = normalBehavior;
					currentState = StateGuardian.NORMAL;
				}
				sendedValues.clear();
				notSendedValues.clear();
				mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.GUARDIAN_STATE, new GuardianStateMsg(hostAddress, currentState, supervisedPatch)), getSelf());
				getContext().become(nextBehaviour, true);
				unstashAll();
			} else {
				mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.CONSENSUS_VALUES_PATCH + supervisedPatch, new ConsensusValuesMsg(notSendedValues)), getSelf());
				sendedValues.addAll(notSendedValues);
				notSendedValues.clear();
				getContext().getSystem().getScheduler().scheduleOnce(Duration.ofMillis(UtilityValues.MESSAGE_TIMEOUT_MILLIS), () -> {
					getSelf().tell(new CheckStatusMsg(), getSelf());
				}, getContext().dispatcher());
			}
		}).match(ConsensusValuesMsg.class, msg -> {
			if (!getSender().equals(getSelf())) {
				notSendedValues.addAll(msg.getValues());
			}
		}).match(MemberUp.class, msg -> {
			mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.GUARDIAN_STATE, new GuardianStateMsg(hostAddress, currentState, supervisedPatch)), getSelf());
		}).matchAny(msg -> {
			stash();
		}).build();
	}

	private AbstractActor.Receive setupDangerBehaviour() {
		return receiveBuilder().match(RecoveryPatchMsg.class, msg -> {
			sensors.clear();
			if (thresholdExceededTask != null) {
				thresholdExceededTask.cancel();
				thresholdExceededTask = null;
			}
			currentState = StateGuardian.NORMAL;
			mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.GUARDIAN_STATE, new GuardianStateMsg(hostAddress, currentState, supervisedPatch)), getSelf());
			getContext().become(normalBehavior, true);
		}).match(MemberUp.class, msg -> {
			mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.GUARDIAN_STATE, new GuardianStateMsg(hostAddress, currentState, supervisedPatch)), getSelf());
		}).build();
	}

	@Override
	public Receive createReceive() {
		return normalBehavior;
	}

	@Override
	public void postStop() {
		cluster.unsubscribe(getSelf());
		mediator.tell(new DistributedPubSubMediator.Unsubscribe(TopicsClusterMessages.SENSOR_VALUE_IN_PATCH + supervisedPatch, getSelf()), getSelf());
		mediator.tell(new DistributedPubSubMediator.Unsubscribe(TopicsClusterMessages.SENSOR_OUT_FROM_PATCH + supervisedPatch, getSelf()), getSelf());
		mediator.tell(new DistributedPubSubMediator.Unsubscribe(TopicsClusterMessages.CONSENSUS_WARNING_PATCH + supervisedPatch, getSelf()), getSelf());
		mediator.tell(new DistributedPubSubMediator.Unsubscribe(TopicsClusterMessages.CONSENSUS_VALUES_PATCH + supervisedPatch, getSelf()), getSelf());
		mediator.tell(new DistributedPubSubMediator.Unsubscribe(TopicsClusterMessages.RECOVERY_PATCH + supervisedPatch, getSelf()), getSelf());
	}

}