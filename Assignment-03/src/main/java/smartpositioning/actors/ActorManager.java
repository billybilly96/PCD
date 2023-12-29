package smartpositioning.actors;

import java.util.ArrayList;
import java.util.List;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import smartpositioning.common.Particle;
import smartpositioning.common.View;
import smartpositioning.common.World;
import smartpositioning.messages.ComputeParticleMsg;
import smartpositioning.messages.ComputeStepMsg;
import smartpositioning.messages.ShowChangesMsg;
import smartpositioning.messages.SimulationAddMsg;
import smartpositioning.messages.SimulationPauseMsg;
import smartpositioning.messages.SimulationRemoveMsg;
import smartpositioning.messages.SimulationStartMsg;
import smartpositioning.messages.UpdatedParticleMsg;
import smartpositioning.messages.UpdatedStepMsg;
import smartpositioning.messages.ShowStepMsg;

public class ActorManager extends AbstractActorWithStash {

	private AbstractActor.Receive normalBehaviour;
	private AbstractActor.Receive computingBehavior;
	private List<ActorRef> calculatorActors;
	private ActorRef viewerActor;
	private World particlesWorld;
	private int updatedParticles;
	private boolean pause;

	public ActorManager(View view, int nSteps, int nParticles, int frameRate) {
		normalBehaviour = setupNormalBehaviour();
		computingBehavior = setupComputingBehaviour();
		calculatorActors = new ArrayList<>();
		for (int i = 0; i < nParticles; i++) {
			calculatorActors.add(getContext().actorOf(Props.create(ActorCalculator.class)));
		}
		viewerActor = getContext().actorOf(Props.create(ActorViewer.class, getSelf(), view, frameRate));
		particlesWorld = new World(nSteps, nParticles);
		pause = true;
	}

	private AbstractActor.Receive setupNormalBehaviour() {
		return receiveBuilder().match(SimulationStartMsg.class, msg -> {
			if (pause) {
				pause = false;
				startStepComputation();
			}
		}).match(SimulationPauseMsg.class, msg -> {
			pause = true;
		}).match(SimulationAddMsg.class, msg -> {
			calculatorActors.add(getContext().actorOf(Props.create(ActorCalculator.class)));
			particlesWorld.addParticle(msg.getX(), msg.getY());
			viewerActor.tell(new ShowChangesMsg(particlesWorld.createSnapshot()), getSelf());
		}).match(SimulationRemoveMsg.class, msg -> {
			if (!calculatorActors.isEmpty()) {
				ActorRef removedActor = calculatorActors.remove(0);
				getContext().getSystem().stop(removedActor);
				particlesWorld.removeFirstParticle();
				viewerActor.tell(new ShowChangesMsg(particlesWorld.createSnapshot()), getSelf());
			}
		}).match(UpdatedStepMsg.class, msg -> {
			if (!pause) {
				startStepComputation();
			}
		}).build();
	}

	private void startStepComputation() {
		getContext().become(computingBehavior, false);
		getSelf().tell(new ComputeStepMsg(), getSelf());
	}

	private AbstractActor.Receive setupComputingBehaviour() {
		return receiveBuilder().match(ComputeStepMsg.class, msg -> {
			if (calculatorActors.isEmpty()) {
				completeStepComputation();
			} else {
				updatedParticles = 0;
				List<Particle> backupParticles = particlesWorld.createBackupParticles();
				for (ActorRef worker : calculatorActors) {
					int index = calculatorActors.indexOf(worker);
					worker.tell(new ComputeParticleMsg(backupParticles, index), getSelf());
				}
			}
		}).match(UpdatedParticleMsg.class, msg -> {
			updatedParticles++;
			particlesWorld.getParticles().set(msg.getParticleIndex(), msg.getParticle());
			if (updatedParticles == particlesWorld.getParticles().size()) {
				completeStepComputation();
			}
		}).matchAny(msg -> {
			stash();
		}).build();
	}

	private void completeStepComputation() {
		boolean gameOver = particlesWorld.increaseCurrentStep();
		viewerActor.tell(new ShowStepMsg(particlesWorld.createSnapshot()), getSelf());
		if (gameOver) {
			getContext().getSystem().stop(getSelf());
		} else {
			getContext().unbecome();
			unstashAll();
		}
	}

	@Override
	public Receive createReceive() {
		return normalBehaviour;
	}

	@Override
	public void postStop() {
		for (ActorRef calculatorActor : calculatorActors) {
			getContext().getSystem().stop(calculatorActor);
		}
		getContext().getSystem().stop(viewerActor);
		getContext().getSystem().terminate();
	}

}