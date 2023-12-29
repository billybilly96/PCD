package smartpositioning.model;

import smartpositioning.synch.CyclicBarrier;
import smartpositioning.synch.Coordinator;
import smartpositioning.synch.Context;
import smartpositioning.util.*;
import smartpositioning.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Master extends Thread {

	private View view;
	private Model model;
	private Coordinator coordinator;
	private Semaphore semaphore;
	private List<Worker> workers;
	private Context context;
	private CyclicBarrier barrier;
	private int nWorkers;
	private Chrono chrono;
	
	public Master(View view, Model model, Semaphore semaphore, Coordinator coordinator, Context context) {
		this.view = view;
		this.model = model;
		this.nWorkers = model.getNumWorkers();
		// barriera dove si sincronizzano master e workers
		this.barrier = new CyclicBarrier(this.nWorkers + 1);
		this.semaphore = semaphore;
		this.coordinator = coordinator;
		this.context = context;
		this.chrono = new Chrono();
		this.workers = new ArrayList<>();
	}

	public void run() {
		try {
			this.createWorkers();
			this.view.setNumThread(this.nWorkers);
			this.chrono.start();
			for (int i = 0; i < Utility.N_STEPS; i++) {
				// Gestione dei button START e STOP e metto in pausa il cronometro
				if (this.context.isStopped()) {
					this.chrono.pause(true);
					this.coordinator.block();
					this.chrono.pause(false);
				}
				// Mi sincronizzo rispetto ai workers (significa che hanno terminato la computazione)
				this.barrier.hitAndWaitAll();
				// Aggiorno la View
				this.view.update(this.model.getParticles(), i, this.chrono.getTime());
				// Rilascio un n° di permessi pari al numero di workers
				this.semaphore.release(this.nWorkers);
			}			
			this.chrono.stop();
			this.context.end();
			this.view.setTime(this.chrono.getTime());
			System.out.println("Time elapsed: " + this.chrono.getTime() + " ms.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}			
	}

	public void createWorkers() {
		int inc = 0;
		int size;
		// Significa che il numero delle particelle è minore al numero dei workers
		if (this.model.getParticles().size() / this.nWorkers == 0) {
			this.nWorkers = this.model.getParticles().size();
			size = 1;
		} else {
			inc = this.model.getParticles().size() % this.nWorkers;
			size = this.model.getParticles().size() / this.nWorkers;
		}
		for (int i = 0; i < this.nWorkers - 1; i++) {
			this.workers.add(i, new Worker(i * size, (i + 1) * size, this.model.getParticles(), this.semaphore, this.barrier, this.context));
			this.workers.get(i).start();
		}
		// Assegno all'ultimo worker le particelle mancanti (così gestisco il problema in cui il numero delle particelle non + un multiplo al numero dei workers)
		this.workers.add(this.nWorkers - 1, new Worker(this.model.getParticles().size() - size - inc, this.model.getParticles().size(), this.model.getParticles(), this.semaphore, this.barrier, this.context));
		this.workers.get(this.nWorkers - 1).start();
	}

}
