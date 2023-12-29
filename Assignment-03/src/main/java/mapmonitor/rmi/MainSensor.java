package mapmonitor.rmi;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import org.json.simple.parser.ParseException;

import assignment.utility.Pair;
import mapmonitor.common.UtilityJson;
import mapmonitor.common.UtilityValues;

public class MainSensor {

	public static void main(String[] args) {
		if (args.length == 1) {
			try {
				String hostname = args[0];
				System.setProperty("java.security.policy", UtilityValues.RMI_SECURITY_POLICY_FILE_PATH);
				if (System.getSecurityManager() == null) {
					System.setSecurityManager(new SecurityManager());
				}
				Registry registry = LocateRegistry.getRegistry(hostname);
				Set<Pair<String, String>> sensors = UtilityJson.getSensorsFromJson(UtilityValues.RMI_SENSORS_CONFIGURATION_FILE_PATH);
				for (Pair<String, String> hostnameAndStub : sensors) {
					// Se l'hostname nel file di configurazione è quello corrente, creo l'oggetto remoto
					if (hostnameAndStub.getX().equals(hostname)) {
						SensorRemote sensorObj = new SensorImpl();
						// Thread usato soltanto per simulare il cambiamento di valori di un sensore (non comunica con nessun oggetto remoto)
						new SensorThread((Sensor) sensorObj).start();	// Il thread viene lanciato
						SensorRemote sensorObjStub = (SensorRemote) UnicastRemoteObject.exportObject(sensorObj, 0);
						registry.rebind(hostnameAndStub.getY(), sensorObjStub);
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
