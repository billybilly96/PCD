package mapmonitor.rmi;

import java.util.HashSet;
import java.util.Set;

public class DashboardPressed {

	private Set<Integer> pressedPatches;

	public DashboardPressed() {
		pressedPatches = new HashSet<>();
	}

	public synchronized Set<Integer> consumeAllPressions() {
		Set<Integer> patches = new HashSet<>(pressedPatches);
		pressedPatches.clear();
		return patches;
	}

	public synchronized void registerPression(int patch) {
		pressedPatches.add(patch);
	}

}
