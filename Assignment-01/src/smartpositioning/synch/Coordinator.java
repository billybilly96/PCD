package smartpositioning.synch;

public class Coordinator {

    public synchronized void block() throws InterruptedException {
        wait();
    }

    public synchronized void releaseAll() {
        notifyAll();
    }

}
