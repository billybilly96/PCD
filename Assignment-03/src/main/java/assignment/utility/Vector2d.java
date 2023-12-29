package assignment.utility;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Class for 2-dimensional vectors Objects of this class are completely state-less
 */
public class Vector2d implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final double NULL_COMPONENT = 0;
	private double x, y;

	public Vector2d(double xVal, double yVal) {
		x = xVal;
		y = yVal;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getAbs() {
		return Math.sqrt(x * x + y * y);
	}

	public Vector2d sum(Vector2d v) {
		return new Vector2d(x + v.getX(), y + v.getY());
	}

	public Vector2d sub(Vector2d v) {
		return new Vector2d(x - v.getX(), y - v.getY());
	}

	public Vector2d mul(double fact) {
		return new Vector2d(x * fact, y * fact);
	}

	public Vector2d div(double fact) {
		return new Vector2d(x / fact, y / fact);
	}

	public Vector2d normalize() {
		double module = getAbs();
		return new Vector2d(x / module, y / module);
	}

	public Vector2d clone() {
		return new Vector2d(x, y);
	}

	public static Vector2d nullV() {
		return new Vector2d(NULL_COMPONENT, NULL_COMPONENT);
	}

	public String toString() {
		return "Vector2d [x=" + new DecimalFormat("#0.00").format(x) + ", y=" + new DecimalFormat("#0.00").format(y) + "]";
	}

}