package smartpositioning.model;

import smartpositioning.model.particle.Particle;
import smartpositioning.model.particle.ParticleImpl;
import smartpositioning.util.Point2d;
import smartpositioning.util.Utility;
import smartpositioning.util.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Model {

    private List<Particle> particles;
    private int nWorkers;

    public Model() {
        this.particles = this.generateParticles();
    }

    public List<Particle> generateParticles() {
        List<Particle> particles = new ArrayList<>();
        for (int i = 0; i < Utility.N_PARTICLES; i++) {
            double x = generateRandomDouble(Utility.MIN_POS_X, Utility.MAX_POS_X);
            double y = generateRandomDouble(Utility.MIN_POS_Y, Utility.MAX_POS_Y);
            Point2d position = new Point2d(x, y);
            double alpha = generateRandomDouble(Utility.MIN_ALPHA, Utility.MAX_ALPHA);
            double mass = generateRandomDouble(Utility.MIN_MASS, Utility.MAX_MASS);
            ParticleImpl newParticle = new ParticleImpl(alpha, mass, position, Vector2d.zero());
            particles.add(newParticle);
        }
        return particles;
    }

    private double generateRandomDouble(double min, double max) {
        Random rand = new Random();
        return min + (max - min) * rand.nextDouble();
    }

    public List<Particle> getParticles() {
        return this.particles;
    }

    public int getNumWorkers() {
        // Non è "Runtime.getRuntime().availableProcessors() + 1", perché un processore sarà usato per eseguire il Master
        this.nWorkers = Runtime.getRuntime().availableProcessors();
        return this.nWorkers;
    }

}
