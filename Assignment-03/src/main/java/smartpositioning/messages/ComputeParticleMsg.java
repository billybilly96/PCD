package smartpositioning.messages;

import java.util.List;

import smartpositioning.common.Particle;

public class ComputeParticleMsg {

	private final List<Particle> backupParticles;
	private final int particleIndex;

	public ComputeParticleMsg(List<Particle> backup, int index) {
		backupParticles = backup;
		particleIndex = index;
	}

	public List<Particle> getBackupParticles() {
		return backupParticles;
	}

	public int getParticleIndex() {
		return particleIndex;
	}

}