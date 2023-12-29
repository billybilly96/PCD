package mapmonitor.actors;

import akka.actor.ActorSystem;
import akka.actor.Props;
import mapmonitor.common.UtilityValues;

import java.io.File;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class MainDashboard {
	
	public static void main(String[] args) {
		if (args.length >= 3) {
			int numDashboards = Integer.parseInt(args[0]);
			String hostnameConfiguration = "akka.remote.netty.tcp.hostname=" + args[1];
			String portConfiguration = "akka.remote.netty.tcp.port=" + args[2];
			String seedNodesConfiguration = "akka.cluster.seed-nodes = [";
			for (int i = 0; i < args.length - 3; i++) {
				seedNodesConfiguration += '"' + "akka.tcp://" + UtilityValues.CLUSTER_ACTOR_SYSTEM + "@" + args[i + 3] + '"';
				if (i < args.length - 4) seedNodesConfiguration += ",";
			}
			seedNodesConfiguration += "]";
			Config config = ConfigFactory.parseString(hostnameConfiguration)
					.withFallback(ConfigFactory.parseString(portConfiguration))
					.withFallback(ConfigFactory.parseString(seedNodesConfiguration))
					.withFallback(ConfigFactory.load(ConfigFactory.parseFile(new File(UtilityValues.AKKA_CONFIGURATION_FILE_PATH))));
			ActorSystem system = ActorSystem.create(UtilityValues.CLUSTER_ACTOR_SYSTEM, config);
			for (int i = 0; i < numDashboards; i++) {
				system.actorOf(Props.create(ActorDashboard.class, UtilityValues.M, UtilityValues.N));
			}
		} else {
			System.out.println("The arguments' number is not valid!");
			System.exit(0);
		}
	}

}