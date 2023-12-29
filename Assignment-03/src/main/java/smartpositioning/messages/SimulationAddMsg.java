package smartpositioning.messages;

public class SimulationAddMsg {

	private final double xVal, yVal;

	public SimulationAddMsg(double x, double y) {
		xVal = x;
		yVal = y;
	}

	public double getX() {
		return xVal;
	}

	public double getY() {
		return yVal;
	}

}