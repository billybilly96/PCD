package smartpositioning.actors;

import smartpositioning.common.Controller;

public class SmartPositioning {

	private static final int N_STEPS = 2500;
	private static final int N_PARTICLES = 250;
	private static final int FRAME_RATE = 100; // vengono mostrati 100 frame ogni secondo

	public static void main(String[] args) {
		new Controller(N_STEPS, N_PARTICLES, FRAME_RATE);
	}

}