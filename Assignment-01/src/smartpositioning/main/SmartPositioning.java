package smartpositioning.main;

import smartpositioning.controller.Controller;
import smartpositioning.model.*;
import smartpositioning.view.View;

import javax.swing.*;


public class SmartPositioning {

    public static void main(String[] args) {
        Model model = new Model();
        View view = new View(model);
        Controller controller = new Controller(model, view);
        view.addController(controller);
        SwingUtilities.invokeLater(() -> view.setVisible(true));
    }

}
