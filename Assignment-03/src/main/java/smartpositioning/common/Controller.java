package smartpositioning.common;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import smartpositioning.actors.ActorManager;
import smartpositioning.messages.SimulationAddMsg;
import smartpositioning.messages.SimulationPauseMsg;
import smartpositioning.messages.SimulationRemoveMsg;
import smartpositioning.messages.SimulationStartMsg;

public class Controller implements InputListener {

	private final int nSteps, nParticles, frameRate;
	private AtomicBoolean actorsWorking;
	private ActorSystem actorSystem;
	private ActorRef actorManager;
	private View view;

	public Controller(int numSteps, int numParticles, int rate) {
		nSteps = numSteps;
		nParticles = numParticles;
		frameRate = rate;
		actorsWorking = new AtomicBoolean(false);
		view = new View();
		view.addListener(this);
		SwingUtilities.invokeLater(() -> view.setVisible(true));
	}

	private void tryToInitializeActors() {
		if (!actorsWorking.get()) {
			actorsWorking.set(true);
			actorSystem = ActorSystem.create();
			actorManager = actorSystem.actorOf(Props.create(ActorManager.class, view, nSteps, nParticles, frameRate));
			actorSystem.registerOnTermination(() -> {
				actorsWorking.set(false);
			});
		}
	}

	public void start() {
		tryToInitializeActors();
		actorManager.tell(new SimulationStartMsg(), ActorRef.noSender());
	}

	public void step() {
		tryToInitializeActors();
		actorManager.tell(new SimulationStartMsg(), ActorRef.noSender());
		actorManager.tell(new SimulationPauseMsg(), ActorRef.noSender());
	}

	public void add(double x, double y) {
		if (actorsWorking.get()) {
			actorManager.tell(new SimulationAddMsg(x, y), ActorRef.noSender());
		}
	}

	public void remove() {
		if (actorsWorking.get()) {
			actorManager.tell(new SimulationRemoveMsg(), ActorRef.noSender());
		}
	}

	public void pause() {
		if (actorsWorking.get()) {
			actorManager.tell(new SimulationPauseMsg(), ActorRef.noSender());
		}
	}

	public void stop() {
		if (actorsWorking.get()) {
			actorSystem.stop(actorManager);
		}
	}

}
