package mapmonitor.rmi;

import mapmonitor.common.StateGuardian;

public class GuardianImpl implements Guardian, GuardianRemote {

	private StateGuardian currentState;
	private boolean notifiedRecovery;
	private boolean notifiedWarning;

	public GuardianImpl() {
		notifiedRecovery = false;
		notifiedWarning = false;
		currentState = StateGuardian.NORMAL;
	}

	public synchronized boolean consumeNotifiedRecovery() {
		boolean recovery = notifiedRecovery;
		notifiedRecovery = false;
		return recovery;
	}

	public synchronized boolean consumeNotifiedWarning() {
		boolean warning = notifiedWarning;
		notifiedWarning = false;
		return warning;
	}

	public synchronized void notifyRecovery() {
		if (currentState.equals(StateGuardian.DANGER)) {
			notifiedRecovery = true;
		}
	}

	public synchronized void notifyWarning() {
		if (!currentState.equals(StateGuardian.DANGER)) {
			notifiedWarning = true;
		}
	}

	public synchronized StateGuardian getState() {
		return currentState;
	}

	public synchronized void setState(StateGuardian newState) {
		currentState = newState;
	}

}