package mapmonitor.common;

public class UtilityValues {

	// Constants used for both the implementations done
	public static final int M = 2, N = 2;
	public static final double MAP_MIN_X = 0, MAP_MIN_Y = 0;
	public static final double MAP_MAX_X = 300, MAP_MAX_Y = 100;
	public static final double MIN_VALUE = 0, MAX_VALUE = 100;
	public static final double THRESHOLD = 70;
	public static final long K_MILLIS = 5000; // Time = 5 s
	public static final long SENSOR_TIMER_MILLIS = 1000; // Time = 1 s

	// Constants used only for akka actors implementation
	public static final int FAILURES = 2; // Guardians than can fail during consensus algorithm
	public static final long MESSAGE_TIMEOUT_MILLIS = 300; // Time = 0.3 s
	public static final Object TIMER_KEY = "TimerKey";
	public static final String CLUSTER_ACTOR_SYSTEM = "ClusterActorSystem";
	public static final String AKKA_CONFIGURATION_FILE_PATH = "src/main/java/mapmonitor/actors/mapmonitor.akka.conf";

	// Constants used only for rmi implementation
	public static final long TIMER_POLLING_MILLIS = 500; // Time = 0.5 s
	public static final String RMI_GUARDIANS_CONFIGURATION_FILE_PATH = "src/main/java/mapmonitor/rmi/mapmonitor.rmi.guardians.json";
	public static final String RMI_SENSORS_CONFIGURATION_FILE_PATH = "src/main/java/mapmonitor/rmi/mapmonitor.rmi.sensors.json";
	public static final String RMI_SECURITY_POLICY_FILE_PATH = "src/main/java/mapmonitor/rmi/mapmonitor.rmi.security.policy";

}