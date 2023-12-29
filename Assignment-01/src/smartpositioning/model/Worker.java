package smartpositioning.model;

import smartpositioning.model.particle.Particle;
import smartpositioning.synch.CyclicBarrier;
import smartpositioning.synch.Context;
import smartpositioning.util.Point2d;
import smartpositioning.util.Utility;
import smartpositioning.util.Vector2d;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Worker extends Thread {

	private List<Particle> particles;
	private final int from;
    private final int to;
    private CyclicBarrier barrier;
    private Semaphore semaphore;
    private Context context;
	
	public Worker(int f, int t, List<Particle> p, Semaphore se, CyclicBarrier b, Context c) {
		this.from = f;
		this.to = t;
		this.particles = p;
		this.semaphore = se;
		this.barrier = b;
		this.context = c;
	}

	public void run() {
		// Copia difensiva della lista di particelle (così non ci sono problemi nella lettura delle loro posizioni)
		List<Particle> currentParticles = Collections.unmodifiableList(this.particles);
		while(!this.context.isOver()) {
			try {
				// Aggiorno la mia porzione di particelle
				this.updateParticles(currentParticles);
				// Sincronizzazione dei workers e del master nella barriera ciclica (prima di procedere si aspetta che tutti arrivino)
				this.barrier.hitAndWaitAll();
				// Prendo un permesso dal semaforo
				this.semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateParticles(List<Particle> currentParticles) {
		for (int i = this.from; i < this.to; i++) {
			Particle pi = currentParticles.get(i);
			Vector2d resultingForce = this.calculateForce(pi, currentParticles, i);
			double rx = pi.getPosition().getX() + (pi.getVelocity().getX() * Utility.DT);
			double ry = pi.getPosition().getY() + (pi.getVelocity().getY() * Utility.DT);
			double vx = pi.getVelocity().getX() + ((resultingForce.getX() / pi.getMass()) * Utility.DT);
			double vy = pi.getVelocity().getY() + ((resultingForce.getY() / pi.getMass()) * Utility.DT);
			this.particles.get(i).update(new Point2d(rx, ry), new Vector2d(vx, vy));
		}
	}
	
	private Vector2d calculateForce(Particle pi, List<Particle> currentParticles, int particleIndex) {
		double fx = 0, fy = 0;
		for (int j = 0; j < currentParticles.size(); j++) {
			if (j != particleIndex) {
				Particle pj = currentParticles.get(j);
				double dx = pi.getPosition().getX() - pj.getPosition().getX();
				double dy = pi.getPosition().getY() - pj.getPosition().getY();
				double d = Math.sqrt(dx * dx + dy * dy);
				double d3 = d * d *d;
				fx += ((Utility.K * pi.getAlpha() * pj.getAlpha() / d3) * dx);
				fy += ((Utility.K * pi.getAlpha() * pj.getAlpha() / d3) * dy);
			}
		}
		fx -= (Utility.K_ATTR * pi.getVelocity().getX());
		fy -= (Utility.K_ATTR * pi.getVelocity().getY());
		return new Vector2d(fx, fy);
	}
	
}
