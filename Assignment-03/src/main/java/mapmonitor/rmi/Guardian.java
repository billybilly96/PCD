package mapmonitor.rmi;

import mapmonitor.common.StateGuardian;

public interface Guardian {

	public boolean consumeNotifiedRecovery();

	public boolean consumeNotifiedWarning();

	public StateGuardian getState();

	public void setState(StateGuardian newState);

}