package mapmonitor.rmi;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.parser.ParseException;

import assignment.utility.Pair;
import mapmonitor.common.UtilityJson;
import mapmonitor.common.UtilityValues;

public class MainGuardian {

	public static void main(String[] args) {
		if (args.length == 1) {
			try {
				String hostname = args[0];
				System.setProperty("java.security.policy", UtilityValues.RMI_SECURITY_POLICY_FILE_PATH);
				if (System.getSecurityManager() == null) {
					System.setSecurityManager(new SecurityManager());
				}
				Registry registry = LocateRegistry.getRegistry(hostname);
				Map<Integer, Set<Pair<String, String>>> guardians = UtilityJson.getGuardiansFromJson(UtilityValues.RMI_GUARDIANS_CONFIGURATION_FILE_PATH);
				Set<Pair<String, String>> sensors = UtilityJson.getSensorsFromJson(UtilityValues.RMI_SENSORS_CONFIGURATION_FILE_PATH);
				for (Integer patch : guardians.keySet()) {
					for (Pair<String, String> hostnameAndStub : guardians.get(patch)) {
						// Se l'hostname nel file di configurazione è quello corrente, creo l'oggetto remoto
						if (hostnameAndStub.getX().equals(hostname)) {
							Set<Pair<String, String>> otherPatchGuardians = new HashSet<>(guardians.get(patch));
							// Rimuovo dalla lista degli altri guardiani quello che devo istanziare
							otherPatchGuardians.remove(hostnameAndStub);
							GuardianRemote guardianObj = new GuardianImpl();
							new GuardianThread(otherPatchGuardians, sensors, patch, (Guardian) guardianObj).start();
							GuardianRemote guardianObjStub = (GuardianRemote) UnicastRemoteObject.exportObject(guardianObj, 0);
							registry.rebind(hostnameAndStub.getY(), guardianObjStub);
						}
					}
				}
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("The arguments' number is not valid!");
			System.exit(0);
		}
	}

}