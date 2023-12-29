package smartpositioning.util;

import java.io.Serializable;

/*
 * Class for 2-dimensional vectors
 * Objects of this class are completely state-less
 */
public class Vector2d implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final double ZERO = 0;
	private double x, y;

	public Vector2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public double getAbs() {
		return Math.sqrt(this.x * this.x + this.y * this.y);
	}

	public Vector2d sum(Vector2d v) {
		return new Vector2d(this.x + v.getX(), this.y + v.getY());
	}

	public Vector2d sub(Vector2d v) {
		return new Vector2d(this.x - v.getX(), this.y - v.getY());
	}
	
	public Vector2d mul(double fact) {
		return new Vector2d(this.x * fact, this.y * fact);
	}

	public Vector2d div(double fact) {
		return new Vector2d(this.x / fact, this.y / fact);
	}

	public Vector2d normalize() {
		double module = this.getAbs();
		return new Vector2d(this.x / module, this.y / module);
	}
	
	public static Vector2d zero() {
		return new Vector2d(ZERO, ZERO);
	}

	public String toString() {
		return "Vector2d(" + this.x + "," + this.y + ")";
	}

}