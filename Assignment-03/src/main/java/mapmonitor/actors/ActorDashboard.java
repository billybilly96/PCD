package mapmonitor.actors;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import assignment.utility.Pair;
import assignment.utility.Point2d;
import mapmonitor.common.StateGuardian;
import mapmonitor.common.ViewDashboard;
import mapmonitor.messages.DetectedOutMsg;
import mapmonitor.messages.DetectedPositionMsg;
import mapmonitor.messages.GuardianStateMsg;
import mapmonitor.messages.PressedPatchMsg;
import mapmonitor.messages.RecoveryPatchMsg;
import mapmonitor.messages.TopicsClusterMessages;

public class ActorDashboard extends AbstractActor {

	private Map<String, Map<ActorRef, Pair<Integer, StateGuardian>>> guardians;
	private Map<String, Map<ActorRef, Pair<Integer, Point2d>>> sensors;
	private ViewDashboard viewDashboard;
	private Cluster cluster;
	private ActorRef mediator;

	public ActorDashboard(int m, int n) {
		guardians = new HashMap<>();
		sensors = new HashMap<>();		
		viewDashboard = new ViewDashboard(getSelf(), m, n);
	}

	@Override
	public void preStart() throws Exception {
		cluster = Cluster.get(getContext().system());
		mediator = DistributedPubSub.get(getContext().system()).mediator();
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
		mediator.tell(new DistributedPubSubMediator.Subscribe(TopicsClusterMessages.GUARDIAN_STATE, getSelf()), getSelf());
		mediator.tell(new DistributedPubSubMediator.Subscribe(TopicsClusterMessages.SENSOR_POSITION, getSelf()), getSelf());
		mediator.tell(new DistributedPubSubMediator.Subscribe(TopicsClusterMessages.SENSOR_OUT_FROM_MAP, getSelf()), getSelf());
		SwingUtilities.invokeLater(() -> viewDashboard.setVisible(true));
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(DistributedPubSubMediator.SubscribeAck.class, msg -> {
			// Silently ignores (subscription correct)
		}).match(DetectedPositionMsg.class, msg -> {			
			if (!sensors.containsKey(msg.getSenderAddress())) {
				sensors.put(msg.getSenderAddress(), new HashMap<>());
			}
			sensors.get(msg.getSenderAddress()).put(getSender(), new Pair<Integer, Point2d>(msg.getPatch(), msg.getPosition()));		
			viewDashboard.updateSensors(sensors.values().stream().map(x -> x.values()).flatMap(x -> x.stream()).collect(Collectors.toList()));
		}).match(GuardianStateMsg.class, msg -> {
			if (!guardians.containsKey(msg.getSenderAddress())) {
				guardians.put(msg.getSenderAddress(), new HashMap<>());
			}
			guardians.get(msg.getSenderAddress()).put(getSender(), new Pair<Integer, StateGuardian>(msg.getPatch(), msg.getState()));
			viewDashboard.updateGuardians(guardians.values().stream().map(x -> x.values()).flatMap(x -> x.stream()).collect(Collectors.toList()));
		}).match(PressedPatchMsg.class, msg -> {
			mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.RECOVERY_PATCH + msg.getPatch(), new RecoveryPatchMsg()), getSelf());			
		}).match(DetectedOutMsg.class, msg -> {
			if (sensors.containsKey(msg.getSenderAddress()) && sensors.get(msg.getSenderAddress()).containsKey(getSender())) {				
				sensors.get(msg.getSenderAddress()).remove(getSender());					
				viewDashboard.updateSensors(sensors.values().stream().map(x -> x.values()).flatMap(x -> x.stream()).collect(Collectors.toList()));
			}
		}).match(MemberRemoved.class, msg -> {
			handleClusterMemberNotWorking(msg.member().address().toString());
		}).match(UnreachableMember.class, msg -> {
			handleClusterMemberNotWorking(msg.member().address().toString());
		}).build();
	}
	
	private void handleClusterMemberNotWorking(String memberAddress) {
		if (guardians.containsKey(memberAddress)) {
			guardians.remove(memberAddress);
			viewDashboard.updateGuardians(guardians.values().stream().map(x -> x.values()).flatMap(x -> x.stream()).collect(Collectors.toList()));
		}
		if (sensors.containsKey(memberAddress)) {				
			sensors.remove(memberAddress);
			viewDashboard.updateSensors(sensors.values().stream().map(x -> x.values()).flatMap(x -> x.stream()).collect(Collectors.toList()));
		}
	}

	@Override
	public void postStop() throws Exception {
		cluster.unsubscribe(getSelf());
		mediator.tell(new DistributedPubSubMediator.Unsubscribe(TopicsClusterMessages.GUARDIAN_STATE, getSelf()), getSelf());
		mediator.tell(new DistributedPubSubMediator.Unsubscribe(TopicsClusterMessages.SENSOR_POSITION, getSelf()), getSelf());
		mediator.tell(new DistributedPubSubMediator.Unsubscribe(TopicsClusterMessages.SENSOR_OUT_FROM_MAP, getSelf()), getSelf());
		SwingUtilities.invokeLater(() -> viewDashboard.setVisible(false));
	}

}