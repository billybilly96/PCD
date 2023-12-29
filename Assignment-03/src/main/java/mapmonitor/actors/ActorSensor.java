package mapmonitor.actors;

import java.time.Duration;

import javax.swing.SwingUtilities;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import assignment.utility.Point2d;
import assignment.utility.Vector2d;
import mapmonitor.common.UtilitySensor;
import mapmonitor.common.UtilityValues;
import mapmonitor.common.ViewSensor;
import mapmonitor.messages.CheckSensorMsg;
import mapmonitor.messages.DetectedOutMsg;
import mapmonitor.messages.DetectedPositionMsg;
import mapmonitor.messages.DetectedValueMsg;
import mapmonitor.messages.TopicsClusterMessages;

public class ActorSensor extends AbstractActorWithTimers {

	private final String hostAddress;
	private double currentValue, currentDeltaValue;
	private Point2d currentPosition;
	private Vector2d currentDeltaPosition;
	private ActorRef mediator;
	private ViewSensor viewSensor;

	public ActorSensor(String address) {
		viewSensor = new ViewSensor();
		hostAddress = address;
		currentValue = UtilitySensor.initializeValue();
		currentDeltaValue = UtilitySensor.initializeDeltaValue();
		currentPosition = UtilitySensor.initializePosition();
		currentDeltaPosition = UtilitySensor.initializeDeltaPosition();		
	}

	@Override
	public void preStart() throws Exception {
		mediator = DistributedPubSub.get(getContext().system()).mediator();
		getTimers().startPeriodicTimer(UtilityValues.TIMER_KEY, new CheckSensorMsg(), Duration.ofMillis(UtilityValues.SENSOR_TIMER_MILLIS));
		SwingUtilities.invokeLater(() -> viewSensor.setVisible(true));
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(CheckSensorMsg.class, msg -> {
			int lastPatch = UtilitySensor.getCurrentPatch(currentPosition);
			updatePositionAndValue();
			int currentPatch = UtilitySensor.getCurrentPatch(currentPosition);
			if (lastPatch != -1 && currentPatch != lastPatch) {
				DetectedOutMsg outMsg = new DetectedOutMsg(hostAddress);
				if (currentPatch == -1) {
					mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.SENSOR_OUT_FROM_MAP, outMsg), getSelf());
				}
				mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.SENSOR_OUT_FROM_PATCH + lastPatch, outMsg), getSelf());
			}
			if (currentPatch != -1) {
				DetectedPositionMsg positionMsg = new DetectedPositionMsg(hostAddress, currentPosition, currentPatch);
				mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.SENSOR_POSITION, positionMsg), getSelf());
				DetectedValueMsg valueMsg = new DetectedValueMsg(hostAddress, currentValue);
				mediator.tell(new DistributedPubSubMediator.Publish(TopicsClusterMessages.SENSOR_VALUE_IN_PATCH + currentPatch, valueMsg), getSelf());
				viewSensor.update(currentPatch, currentPosition, currentValue);				
			}
		}).build();
	}

	private void updatePositionAndValue() {
		currentPosition = currentPosition.sum(currentDeltaPosition);
		currentValue += currentDeltaValue;
		if (currentValue < UtilityValues.MIN_VALUE || currentValue > UtilityValues.MAX_VALUE) {
			currentValue = UtilitySensor.initializeValue();
			currentDeltaValue = UtilitySensor.initializeDeltaValue();
		}
		if (UtilitySensor.getCurrentPatch(currentPosition) == -1) {
			currentPosition = UtilitySensor.initializePosition();
			currentDeltaPosition = UtilitySensor.initializeDeltaPosition();		
		}
	}

	@Override
	public void postStop() throws Exception {
		getTimers().cancelAll();
		SwingUtilities.invokeLater(() -> viewSensor.setVisible(false));
	}

}