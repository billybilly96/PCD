package smartpositioning.messages;

import smartpositioning.common.Snapshot;

public class ShowChangesMsg {

	private final Snapshot snapshot;

	public ShowChangesMsg(Snapshot currentSnapshot) {
		snapshot = currentSnapshot;
	}

	public Snapshot getSnapshot() {
		return snapshot;
	}

}