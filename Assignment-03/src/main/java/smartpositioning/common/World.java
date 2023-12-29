package smartpositioning.common;

import java.util.Collections;
import java.util.List;

public class World {

	private static final double PART_X = 3, PART_Y = 3;
	private final int lastGameStep, numberParticles;
	private List<Particle> particles;
	private int currentGameStep;

	public World(int nSteps, int nParticles) {
		lastGameStep = nSteps;
		numberParticles = nParticles;
		currentGameStep = 0;
		particles = ParticlesUtility.generateRandomParticles(numberParticles, PART_X, PART_Y);
	}

	public void addParticle(double x, double y) {
		particles.add(ParticlesUtility.generateParticleFromPoint(x, y));
	}

	public void removeFirstParticle() {
		if (!particles.isEmpty()) {
			particles.remove(0);
		}
	}

	public List<Particle> getParticles() {
		return particles;
	}

	public List<Particle> createBackupParticles() {
		return Collections.unmodifiableList(particles);
	}

	public boolean increaseCurrentStep() {
		currentGameStep++;
		return currentGameStep == lastGameStep;
	}

	public Snapshot createSnapshot() {
		return new Snapshot(particles, currentGameStep);
	}

}