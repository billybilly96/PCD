package smartpositioning.performance;

import java.util.List;

import assignment.utility.Point2d;
import assignment.utility.Vector2d;
import smartpositioning.common.Particle;
import smartpositioning.common.World;

public class SmartPositioningWorker extends Thread {

	private static final double DT = 0.10; // tempo logico (che corrisponde a 100 ms)
	private static final double K = 0.05;
	private static final double K_ATTR = 0.15;
	private static final double MIN_DELTA_N = -0.0001, MIN_DELTA_P = 0.0001;
	// le distanze calcolate sono comprese tra questi due valori per evitare che la forza repulsiva mandi in overflow la variabile su cui è memorizzata
	private World particlesWorld;

	public SmartPositioningWorker(int nSteps, int nParticles) {
		particlesWorld = new World(nSteps, nParticles);
	}

	public void run() {
		boolean gameOver = false;
		long startTime = System.nanoTime();
		System.out.println("Start Time: " + startTime);
		while (!gameOver) {
			List<Particle> backupParticles = particlesWorld.createBackupParticles();
			// ad ogni step considero tutte le particelle presenti
			for (int i = 0; i < backupParticles.size(); i++) {
				Particle updatedParticle = updateParticle(i, backupParticles);
				particlesWorld.getParticles().set(i, updatedParticle);
			}
			gameOver = particlesWorld.increaseCurrentStep();
		}
		long endTime = System.nanoTime();
		System.out.println("End Time: " + endTime);
		System.out.println("Elapsed Time: " + (endTime - startTime));
	}

	private Particle updateParticle(int particleIndex, List<Particle> lastStepParticles) {
		Particle pi = lastStepParticles.get(particleIndex);
		Vector2d resultingForce = calculateForce(pi, particleIndex, lastStepParticles);
		double rx = pi.getPosition().getX() + (pi.getSpeed().getX() * DT); // r[i][x] += dt * v[i][x]
		double ry = pi.getPosition().getY() + (pi.getSpeed().getY() * DT); // r[i][y] += dt * v[i][y]
		double vx = pi.getSpeed().getX() + ((resultingForce.getX() / pi.getMass()) * DT); // v[i][x] += dt * f[i][x] / m[i]
		double vy = pi.getSpeed().getY() + ((resultingForce.getY() / pi.getMass()) * DT); // v[i][y] += dt * f[i][y] / m[i]
		pi.update(new Point2d(rx, ry), new Vector2d(vx, vy));
		return pi;
	}

	private Vector2d calculateForce(Particle pi, int particleIndex, List<Particle> lastStepParticles) {
		double fx = 0, fy = 0;
		// per calcolare la forza repulsiva considero la posizione delle particelle nello step precedente
		for (int j = 0; j < lastStepParticles.size(); j++) {
			if (j != particleIndex) {
				Particle pj = lastStepParticles.get(j);
				double dx = pi.getPosition().getX() - pj.getPosition().getX(); // dx = r[i][x] - r[j][x]
				double dy = pi.getPosition().getY() - pj.getPosition().getY(); // dy = r[i][y] - r[j][y]
				dx = (dx > MIN_DELTA_N && dx < 0) ? MIN_DELTA_N : ((dx >= 0 && dx < MIN_DELTA_P) ? MIN_DELTA_P : dx);
				dy = (dy > MIN_DELTA_N && dy < 0) ? MIN_DELTA_N : ((dy >= 0 && dy < MIN_DELTA_P) ? MIN_DELTA_P : dy);
				double d = Math.sqrt(dx * dx + dy * dy); // d = sqrt(dx * dx + dy * dy)
				double d3 = d * d * d; // d3 = d * d * d
				fx += ((K * pi.getAlfa() * pj.getAlfa() / d3) * dx); // f[i][x] += k * alfa[i] * alfa[j] / d3 * (r[i][x] - r[j][x])
				fy += ((K * pi.getAlfa() * pj.getAlfa() / d3) * dy); // f[i][y] += k * alfa[i] * alfa[j] / d3 * (r[i][y] - r[j][y])
			}
		}
		fx -= (K_ATTR * pi.getSpeed().getX()); // f[i][x] += -kattr * v[x]
		fy -= (K_ATTR * pi.getSpeed().getY()); // f[i][y] += -kattr * v[y]
		return new Vector2d(fx, fy);
	}

}