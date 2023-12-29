package smartpositioning.util;

public class Chrono {

    private boolean running;
    private long startTime;
    private long pauseTime;
    private long startPauseTime;

    public Chrono() {
        this.running = false;
    }

    public void start() {
        this.running = true;
        this.startTime = System.currentTimeMillis();
    }

    public void stop() {
        this.startTime = getTime();
        this.running = false;
    }

    public void pause(boolean b) {
        if (b) {
            this.startPauseTime = System.currentTimeMillis();
        } else {
            this.pauseTime += System.currentTimeMillis() - this.startPauseTime;
        }
    }

    public long getTime() {
        return this.running ? System.currentTimeMillis() - (this.startTime + this.pauseTime): this.startTime;
    }

}
