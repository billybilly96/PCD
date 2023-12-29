package smartpositioning.performance;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import smartpositioning.messages.SimulationStartMsg;

public class SmartPositioningAct {

	private static final int N_STEPS = 500;
	private static final int N_PARTICLES = 3000;

	public static void main(String[] args) {
		long startTime = System.nanoTime();
		System.out.println("Start Time: " + startTime);		
		ActorSystem actorSystem = ActorSystem.create();
		ActorRef actorManager = actorSystem.actorOf(Props.create(ActorManagerSer.class, N_STEPS, N_PARTICLES));
		actorManager.tell(new SimulationStartMsg(), ActorRef.noSender());
		actorSystem.registerOnTermination(() -> {
			long endTime = System.nanoTime();
			System.out.println("End Time: " + endTime);
			System.out.println("Elapsed Time: " + (endTime - startTime));
		});
	}
	
}