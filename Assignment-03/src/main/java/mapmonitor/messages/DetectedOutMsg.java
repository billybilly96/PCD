package mapmonitor.messages;

import java.io.Serializable;

public class DetectedOutMsg implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String senderAddress;

	public DetectedOutMsg(String address) {
		senderAddress = address;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

}