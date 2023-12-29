package smartpositioning.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import assignment.utility.Point2d;
import assignment.utility.Vector2d;

public class ParticlesUtility {

	public static final double MIN_POS_X = -1, MAX_POS_X = 1;
	public static final double MIN_POS_Y = -1, MAX_POS_Y = 1;
	public static final double MIN_ALFA = 0.01, MAX_ALFA = 0.05;
	public static final double MIN_MASS = 0.1, MAX_MASS = 0.9;

	public static List<Particle> generateRandomParticles(int nParticles, double partitionX, double partitionY) {
		List<Particle> particles = new ArrayList<Particle>();
		for (int i = 0; i < nParticles; i++) {
			particles.add(generateRandomParticle(partitionX, partitionY));
		}
		return particles;
	}

	public static Particle generateRandomParticle(double partitionX, double partitionY) {
		double x = generateRandomDouble(MIN_POS_X / partitionX, MAX_POS_X / partitionX);
		double y = generateRandomDouble(MIN_POS_Y / partitionY, MAX_POS_Y / partitionY);
		Point2d position = new Point2d(x, y);
		double alfa = generateRandomDouble(MIN_ALFA, MAX_ALFA);
		double mass = generateRandomDouble(MIN_MASS, MAX_MASS);
		return new Particle(alfa, mass, position, Vector2d.nullV());
	}

	public static Particle generateParticleFromPoint(double x, double y) {
		Point2d position = new Point2d(x, y);
		double alfa = generateRandomDouble(MIN_ALFA, MAX_ALFA);
		double mass = generateRandomDouble(MIN_MASS, MAX_MASS);
		return new Particle(alfa, mass, position, Vector2d.nullV());
	}

	private static double generateRandomDouble(double min, double max) {
		Random rand = new Random();
		return min + (max - min) * rand.nextDouble();
	}

}