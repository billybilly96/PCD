package mapmonitor.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import assignment.utility.Pair;
import mapmonitor.common.UtilityValues;
import mapmonitor.common.StateGuardian;

public class GuardianThread extends Thread {

	private final Set<Pair<String, String>> patchGuardiansReferences;
	private final Set<Pair<String, String>> sensorsReferences;
	private final int supervisedPatch;
	private Optional<Long> thresholdExceededTime;
	private Guardian myGuardian;

	public GuardianThread(Set<Pair<String, String>> patchGuardians, Set<Pair<String, String>> sensors, int patch, Guardian guardian) {
		patchGuardiansReferences = patchGuardians;
		sensorsReferences = sensors;
		supervisedPatch = patch;
		thresholdExceededTime = Optional.empty();
		myGuardian = guardian;
	}

	@Override
	public void run() {
		long prevMillis = System.currentTimeMillis();
		while (true) {
			try {
				if (System.currentTimeMillis() - prevMillis < UtilityValues.TIMER_POLLING_MILLIS) {
					Thread.sleep(UtilityValues.TIMER_POLLING_MILLIS - (System.currentTimeMillis() - prevMillis));
				}
				prevMillis = System.currentTimeMillis();
				if (myGuardian.getState().equals(StateGuardian.DANGER) && myGuardian.consumeNotifiedRecovery()) {
					myGuardian.setState(StateGuardian.NORMAL);
				} else if (!myGuardian.getState().equals(StateGuardian.DANGER) && myGuardian.consumeNotifiedWarning()) {
					evaluateGuardianStates();
				} else if (!myGuardian.getState().equals(StateGuardian.DANGER)) {
					evaluateSensorValues();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void evaluateSensorValues() {
		List<Double> sensorValues = new ArrayList<>();
		for (Pair<String, String> reference : sensorsReferences) {
			try {
				Registry registry = LocateRegistry.getRegistry(reference.getX());
				SensorRemote sensor = (SensorRemote) registry.lookup(reference.getY());
				sensorValues.add(sensor.getValue(supervisedPatch));
			} catch (RemoteException | NotBoundException | OutFromPatchException e) {
				// Impossible to connect to the remote sensor, or remote sensor out from supervised patch
			}
		}
		try {
			double avg = sensorValues.stream().mapToDouble(x -> x).average().getAsDouble();
			if (avg > UtilityValues.THRESHOLD && !myGuardian.getState().equals(StateGuardian.WARNING) && thresholdExceededTime.isPresent() && System.currentTimeMillis() - thresholdExceededTime.get() >= UtilityValues.K_MILLIS) {
				thresholdExceededTime = Optional.empty();
				myGuardian.setState(StateGuardian.WARNING);
				handleWarningDetected();
				evaluateGuardianStates();
			} else if (avg > UtilityValues.THRESHOLD && !myGuardian.getState().equals(StateGuardian.WARNING) && !thresholdExceededTime.isPresent()) {
				thresholdExceededTime = Optional.of(System.currentTimeMillis());
			} else if (avg <= UtilityValues.THRESHOLD) {
				thresholdExceededTime = Optional.empty();
				myGuardian.setState(StateGuardian.NORMAL);
			}
		} catch (NoSuchElementException e) {
			thresholdExceededTime = Optional.empty();
			myGuardian.setState(StateGuardian.NORMAL);
		}
	}

	private void evaluateGuardianStates() {
		List<StateGuardian> guardianStates = new ArrayList<>();
		guardianStates.add(myGuardian.getState());
		for (Pair<String, String> reference : patchGuardiansReferences) {
			try {
				Registry registry = LocateRegistry.getRegistry(reference.getX());
				GuardianRemote guardian = (GuardianRemote) registry.lookup(reference.getY());
				guardianStates.add(guardian.getState());
			} catch (RemoteException | NotBoundException e) {
				// Impossible to connect to the remote guardian
			}
		}
		int nWarning = (int) guardianStates.stream().filter(s -> !s.equals(StateGuardian.NORMAL)).count();
		if (nWarning > guardianStates.size() - nWarning) {
			thresholdExceededTime = Optional.empty();
			myGuardian.setState(StateGuardian.DANGER);
		}
	}

	private void handleWarningDetected() {
		for (Pair<String, String> reference : patchGuardiansReferences) {
			try {
				Registry registry = LocateRegistry.getRegistry(reference.getX());
				GuardianRemote guardian = (GuardianRemote) registry.lookup(reference.getY());
				guardian.notifyWarning();
			} catch (RemoteException | NotBoundException e) {
				// Impossible to connect to the remote guardian
			}
		}
	}

}