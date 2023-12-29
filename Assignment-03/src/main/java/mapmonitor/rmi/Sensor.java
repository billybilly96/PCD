package mapmonitor.rmi;

import assignment.utility.Pair;
import assignment.utility.Point2d;

public interface Sensor {

	public double updateValue();

	public Pair<Integer, Point2d> updatePosition();

}
