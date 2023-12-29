package smartpositioning.model.particle;

import smartpositioning.util.Point2d;
import smartpositioning.util.Vector2d;

public interface Particle {

    double getAlpha();
    double getMass();
    Point2d getPosition();
    Vector2d getVelocity();
    void update(Point2d r, Vector2d v);

}
