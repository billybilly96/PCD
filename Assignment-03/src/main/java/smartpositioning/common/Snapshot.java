package smartpositioning.common;

import java.util.ArrayList;
import java.util.List;

public class Snapshot {

	private final List<Particle> particles;
	private final int step;

	public Snapshot(List<Particle> currentParticles, int currentStep) {
		particles = new ArrayList<>(currentParticles);
		step = currentStep;
	}

	public List<Particle> getParticles() {
		return particles;
	}

	public int getStep() {
		return step;
	}

}
