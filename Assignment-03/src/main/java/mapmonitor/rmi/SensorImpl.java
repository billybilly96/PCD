package mapmonitor.rmi;

import assignment.utility.Pair;
import assignment.utility.Point2d;
import assignment.utility.Vector2d;
import mapmonitor.common.UtilitySensor;
import mapmonitor.common.UtilityValues;

public class SensorImpl implements Sensor, SensorRemote {

	private double deltaValue, value;
	private Vector2d deltaPosition;
	private Point2d position;

	public SensorImpl() {
		value = UtilitySensor.initializeValue();
		deltaValue = UtilitySensor.initializeDeltaValue();
		position = UtilitySensor.initializePosition();
		deltaPosition = UtilitySensor.initializeDeltaPosition();
	}

	public synchronized Pair<Integer, Point2d> getPosition() {
		return new Pair<Integer, Point2d>(UtilitySensor.getCurrentPatch(position), position);
	}

	public synchronized double getValue(int patch) throws OutFromPatchException {
		if (UtilitySensor.getCurrentPatch(position) == patch) {
			return value;
		} else {
			throw new OutFromPatchException();
		}
	}

	public synchronized double updateValue() {
		value += deltaValue;
		if (value < UtilityValues.MIN_VALUE || value > UtilityValues.MAX_VALUE) {
			value = UtilitySensor.initializeValue();
			deltaValue = UtilitySensor.initializeDeltaValue();
		}
		return value;
	}

	public synchronized Pair<Integer, Point2d> updatePosition() {
		position = position.sum(deltaPosition);
		if (UtilitySensor.getCurrentPatch(position) == -1) {
			position = UtilitySensor.initializePosition();
			deltaPosition = UtilitySensor.initializeDeltaPosition();
		}
		return new Pair<Integer, Point2d>(UtilitySensor.getCurrentPatch(position), position.clone());
	}

}