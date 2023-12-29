package smartpositioning.actors;

import java.time.Duration;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import smartpositioning.common.View;
import smartpositioning.messages.ShowChangesMsg;
import smartpositioning.messages.ShowStepMsg;
import smartpositioning.messages.UpdatedStepMsg;
import smartpositioning.messages.ViewerTimerMsg;

public class ActorViewer extends AbstractActorWithTimers {

	private static final Object TIMER_KEY = "TimerKey";
	private final int frameRate;
	private boolean stepSnapshotShown;
	private ActorRef managerActor;
	private View view;

	public ActorViewer(ActorRef manager, View applicationView, int rate) {
		frameRate = rate;
		stepSnapshotShown = false;
		managerActor = manager;
		view = applicationView;
	}

	@Override
	public void preStart() throws Exception {
		getTimers().startPeriodicTimer(TIMER_KEY, new ViewerTimerMsg(), Duration.ofMillis(1000 / frameRate));
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(ShowChangesMsg.class, msg -> {
			view.update(msg.getSnapshot().getParticles(), msg.getSnapshot().getStep());
		}).match(ShowStepMsg.class, msg -> {
			stepSnapshotShown = true;
			view.update(msg.getSnapshot().getParticles(), msg.getSnapshot().getStep());
		}).match(ViewerTimerMsg.class, msg -> {
			if (stepSnapshotShown) {
				stepSnapshotShown = false;
				managerActor.tell(new UpdatedStepMsg(), getSelf());
			}
		}).build();
	}

	@Override
	public void postStop() {
		getTimers().cancelAll();
	}

}