package mapmonitor.messages;

import java.io.Serializable;

import assignment.utility.Point2d;

public class DetectedPositionMsg implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String senderAddress;
	private final Point2d position;
	private final int patch;

	public DetectedPositionMsg(String address, Point2d pos, int nPatch) {
		senderAddress = address;
		position = pos;
		patch = nPatch;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

	public Point2d getPosition() {
		return position;
	}

	public int getPatch() {
		return patch;
	}

}