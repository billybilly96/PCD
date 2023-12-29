package mapmonitor.messages;

import java.io.Serializable;

public class DetectedValueMsg implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String senderAddress;
	private final double value;

	public DetectedValueMsg(String address, double val) {
		senderAddress = address;
		value = val;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

	public double getValue() {
		return value;
	}

}