package smartpositioning.synch;

public class Context {

    private enum Status { STOPPED, STARTED, OVER }
    private Status currentStatus;
    private boolean firstTime = true;

    public Context() {
        this.currentStatus = Status.STARTED;
    }

    public synchronized void start() {
        this.currentStatus = Status.STARTED;
    }

    public synchronized void stop() {
        this.currentStatus = Status.STOPPED;
    }

    public synchronized void end() {
        this.currentStatus = Status.OVER;
    }

    public synchronized boolean isStopped() {
        return currentStatus == Status.STOPPED;
    }

    public synchronized boolean isStarted() {
        return currentStatus == Status.STARTED;
    }

    public synchronized boolean isOver() {
        return currentStatus == Status.OVER;
    }

    public synchronized boolean isFirstTime() {
        return this.firstTime;
    }

    public synchronized void setFirstTime() {
        this.firstTime = false;
    }

}
