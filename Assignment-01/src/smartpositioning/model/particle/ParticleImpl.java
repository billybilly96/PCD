package smartpositioning.model.particle;

import smartpositioning.util.Point2d;
import smartpositioning.util.Vector2d;

public class ParticleImpl implements Particle {

	private final double alpha;
	private final double mass;
	private Point2d position;
	private Vector2d velocity;

	public ParticleImpl(double a, double m, Point2d r, Vector2d v) {
		this.alpha = a;
		this.mass = m;
		this.position = r;
		this.velocity= v;
	}

	public double getAlpha() {
		return this.alpha;
	}

	public double getMass() {
		return this.mass;
	}

	public Point2d getPosition() {
		return this.position;
	}

	public Vector2d getVelocity() {
		return this.velocity;
	}

	public void update(Point2d r, Vector2d v) {
		this.position = r;
		this.velocity = v;
	}
		
}
