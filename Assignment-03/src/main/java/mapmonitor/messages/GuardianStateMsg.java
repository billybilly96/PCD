package mapmonitor.messages;

import java.io.Serializable;

import mapmonitor.common.StateGuardian;

public class GuardianStateMsg implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String senderAddress;
	private final StateGuardian state;
	private final int patch;

	public GuardianStateMsg(String address, StateGuardian guardianState, int nPatch) {
		senderAddress = address;
		state = guardianState;
		patch = nPatch;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

	public StateGuardian getState() {
		return state;
	}

	public int getPatch() {
		return patch;
	}

}