package smartpositioning.common;

public interface InputListener {

	void start();

	void step();

	void pause();

	void stop();

	void add(double x, double y);

	void remove();

}
