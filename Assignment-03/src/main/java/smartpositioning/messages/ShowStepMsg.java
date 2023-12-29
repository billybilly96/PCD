package smartpositioning.messages;

import smartpositioning.common.Snapshot;

public class ShowStepMsg {

	private final Snapshot snapshot;

	public ShowStepMsg(Snapshot currentSnapshot) {
		snapshot = currentSnapshot;
	}

	public Snapshot getSnapshot() {
		return snapshot;
	}

}