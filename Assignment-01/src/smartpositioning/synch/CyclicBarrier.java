package smartpositioning.synch;

public class CyclicBarrier {

    private int nHits;
    private int nParticipants;
    private boolean broken;

    public CyclicBarrier(int nParticipants) {
        this.nParticipants = nParticipants;
        this.broken = false;
        this.nHits = 0;
    }

    public synchronized void hitAndWaitAll() throws InterruptedException {
        while (this.broken) {
            wait();
        }
        this.nHits++;
        if (this.nHits == this.nParticipants) {
            this.broken = true;
            this.nHits--;
            notifyAll();
        } else {
            while (this.nHits < this.nParticipants && !this.broken) {
                wait();
            }
            this.nHits--;
            if (this.nHits == 0) {
                this.broken = false;
                notifyAll();
            }
        }
    }

}
