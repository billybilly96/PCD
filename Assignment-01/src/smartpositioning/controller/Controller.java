package smartpositioning.controller;

import smartpositioning.model.*;
import smartpositioning.model.Master;
import smartpositioning.synch.Coordinator;
import smartpositioning.synch.Context;
import smartpositioning.view.View;

import java.util.concurrent.Semaphore;

public class Controller {

    private Model model;
    private View view;
    private Coordinator coordinator;
    private Semaphore semaphore;
    private Master master;
    private Context context;

    public Controller(Model m, View v) {
        this.model = m;
        this.view = v;
        this.coordinator = new Coordinator();
        this.semaphore = new Semaphore(0);
        this.init();
    }

    public void init() {
        this.context = new Context();
        this.master = new Master(this.view, this.model, this.semaphore, this.coordinator, this.context);
    }

    public void start() {
        if (context.isFirstTime()) {
            this.context.setFirstTime();
            this.master.start();
        } else {
            this.context.start();
            this.coordinator.releaseAll();
        }
    }

    public void stop() {
        this.context.stop();
    }

}
