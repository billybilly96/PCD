package mapmonitor.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import assignment.utility.Pair;
import assignment.utility.Point2d;
import mapmonitor.common.UtilityValues;
import mapmonitor.common.StateGuardian;
import mapmonitor.common.ViewDashboard;

public class DashboardThread extends Thread {

	private final Map<Integer, Set<Pair<String, String>>> guardiansReferences;
	private final Set<Pair<String, String>> sensorsReferences;
	private DashboardPressed pressedDashboard;
	private ViewDashboard viewDashboard;

	public DashboardThread(Map<Integer, Set<Pair<String, String>>> guardians, Set<Pair<String, String>> sensors) {
		guardiansReferences = guardians;
		sensorsReferences = sensors;
		pressedDashboard = new DashboardPressed();
		viewDashboard = new ViewDashboard(pressedDashboard, UtilityValues.M, UtilityValues.N);
	}

	@Override
	public void run() {
		long prevMillis = System.currentTimeMillis();
		SwingUtilities.invokeLater(() -> viewDashboard.setVisible(true));
		while (true) {
			try {
				if (System.currentTimeMillis() - prevMillis < UtilityValues.TIMER_POLLING_MILLIS) {
					Thread.sleep(UtilityValues.TIMER_POLLING_MILLIS - (System.currentTimeMillis() - prevMillis));
				}
				prevMillis = System.currentTimeMillis();
				for (int patch : pressedDashboard.consumeAllPressions()) {
					handleDashboardPressed(patch);
				}
				viewDashboard.updateSensors(getSensorPositions());
				viewDashboard.updateGuardians(getGuardianStates());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private List<Pair<Integer, StateGuardian>> getGuardianStates() {
		List<Pair<Integer, StateGuardian>> states = new ArrayList<>();
		for (Integer patch : guardiansReferences.keySet()) {
			for (Pair<String, String> guardianReference : guardiansReferences.get(patch)) {
				try {
					Registry registry = LocateRegistry.getRegistry(guardianReference.getX());
					GuardianRemote guardian = (GuardianRemote) registry.lookup(guardianReference.getY());
					states.add(new Pair<Integer, StateGuardian>(patch, guardian.getState()));
				} catch (RemoteException | NotBoundException e) {
					// Impossible to connect to the remote guardian
				}
			}
		}
		return states;
	}

	private List<Pair<Integer, Point2d>> getSensorPositions() {
		List<Pair<Integer, Point2d>> positions = new ArrayList<>();
		for (Pair<String, String> reference : sensorsReferences) {
			try {
				Registry registry = LocateRegistry.getRegistry(reference.getX());
				SensorRemote sensor = (SensorRemote) registry.lookup(reference.getY());
				Pair<Integer, Point2d> pos = sensor.getPosition();
				if (pos.getX() != -1) {
					positions.add(pos); // Sensor considered if it is in the map
				}
			} catch (RemoteException | NotBoundException e) {
				// Impossible to connect to the remote sensor
			}
		}
		return positions;
	}

	private void handleDashboardPressed(int patch) {
		if (guardiansReferences.containsKey(patch)) {
			for (Pair<String, String> guardianReference : guardiansReferences.get(patch)) {
				try {
					Registry registry = LocateRegistry.getRegistry(guardianReference.getX());
					GuardianRemote guardian = (GuardianRemote) registry.lookup(guardianReference.getY());
					guardian.notifyRecovery();
				} catch (RemoteException | NotBoundException e) {
					// Impossible to connect to the remote guardian
				}
			}
		}
	}

	@Override
	public void interrupt() {
		SwingUtilities.invokeLater(() -> viewDashboard.setVisible(false));
		super.interrupt();
	}

}