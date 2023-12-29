package smartpositioning.common;

import assignment.utility.Point2d;
import assignment.utility.Vector2d;

public class Particle {

	private final double alfa;
	private final double mass;
	private Point2d position;
	private Vector2d speed;

	public Particle(double a, double m, Point2d r, Vector2d v) {
		alfa = a;
		mass = m;
		position = r;
		speed = v;
	}

	public double getAlfa() {
		return alfa;
	}

	public double getMass() {
		return mass;
	}

	public Point2d getPosition() {
		return position;
	}

	public Vector2d getSpeed() {
		return speed;
	}

	public void update(Point2d r, Vector2d v) {
		position = r;
		speed = v;
	}

}