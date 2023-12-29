package mapmonitor.messages;

public class PressedPatchMsg {

	private final int patch;

	public PressedPatchMsg(int nPatch) {
		patch = nPatch;
	}

	public int getPatch() {
		return patch;
	}

}