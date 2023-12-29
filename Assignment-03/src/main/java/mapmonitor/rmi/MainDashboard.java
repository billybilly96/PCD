package mapmonitor.rmi;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.json.simple.parser.ParseException;

import assignment.utility.Pair;
import mapmonitor.common.UtilityJson;
import mapmonitor.common.UtilityValues;

public class MainDashboard {

	public static void main(String[] args) {
		try {
			System.setProperty("java.security.policy", UtilityValues.RMI_SECURITY_POLICY_FILE_PATH);
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}
			Map<Integer, Set<Pair<String, String>>> guardians = UtilityJson.getGuardiansFromJson(UtilityValues.RMI_GUARDIANS_CONFIGURATION_FILE_PATH);
			Set<Pair<String, String>> sensors = UtilityJson.getSensorsFromJson(UtilityValues.RMI_SENSORS_CONFIGURATION_FILE_PATH);
			new DashboardThread(guardians, sensors).start();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

}