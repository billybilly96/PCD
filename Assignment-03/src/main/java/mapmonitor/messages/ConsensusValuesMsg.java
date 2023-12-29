package mapmonitor.messages;

import java.io.Serializable;
import java.util.List;

import mapmonitor.common.StateGuardian;

public class ConsensusValuesMsg implements Serializable {

	private static final long serialVersionUID = 1L;
	private final List<StateGuardian> values;

	public ConsensusValuesMsg(List<StateGuardian> valuesList) {
		values = valuesList;
	}

	public List<StateGuardian> getValues() {
		return values;
	}

}