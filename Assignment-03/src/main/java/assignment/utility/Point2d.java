package assignment.utility;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Class for 2-dimensional points Objects of this class are completely state-less
 */
public class Point2d implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final double NULL_COMPONENT = 0;
	private double x, y;

	public Point2d(double xVal, double yVal) {
		x = xVal;
		y = yVal;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public Point2d sum(Vector2d v) {
		return new Point2d(x + v.getX(), y + v.getY());
	}

	public Vector2d sub(Point2d p) {
		return new Vector2d(x - p.getX(), y - p.getY());
	}

	public Point2d clone() {
		return new Point2d(x, y);
	}

	public String toString() {
		return "Point2d [x=" + new DecimalFormat("#0.00").format(x) + ", y=" + new DecimalFormat("#0.00").format(y) + "]";
	}

	public static Point2d nullP() {
		return new Point2d(NULL_COMPONENT, NULL_COMPONENT);
	}

}