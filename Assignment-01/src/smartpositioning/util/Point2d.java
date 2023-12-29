package smartpositioning.util;

import java.io.Serializable;

/*
 * Class for 2-dimensional points
 * Objects of this class are completely state-less
 */
public class Point2d implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final double ZERO = 0;
	private double x, y;

	public Point2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public Point2d sum(Vector2d v) {
		return new Point2d(this.x + v.getX(), this.y + v.getY());
	}

	public Vector2d sub(Point2d p) {
		return new Vector2d(this.x - p.getX(), this.y - p.getY());
	}
	
	public static Point2d zero() {
		return new Point2d(ZERO, ZERO);
	}

	public String toString() {
		return "Point2d(" + this.x + "," + this.y + ")";
	}

}
