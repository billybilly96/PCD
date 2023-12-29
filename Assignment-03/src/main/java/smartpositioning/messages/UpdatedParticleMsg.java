package smartpositioning.messages;

import smartpositioning.common.Particle;

public class UpdatedParticleMsg {

	private final Particle updatedParticle;
	private final int particleIndex;

	public UpdatedParticleMsg(Particle particle, int index) {
		updatedParticle = particle;
		particleIndex = index;
	}

	public Particle getParticle() {
		return updatedParticle;
	}

	public int getParticleIndex() {
		return particleIndex;
	}

}