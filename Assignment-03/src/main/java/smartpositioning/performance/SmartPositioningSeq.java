package smartpositioning.performance;

public class SmartPositioningSeq {

	private static final int N_STEPS = 500;
	private static final int N_PARTICLES = 3000;

	public static void main(String[] args) {
		new SmartPositioningWorker(N_STEPS, N_PARTICLES).start();
	}

}