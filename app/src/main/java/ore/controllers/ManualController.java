package ore.controllers;

import ch.aplu.jgamegrid.GGKeyListener;
import ore.machines.Machine;
import ore.OreSim;

import java.awt.event.KeyEvent;
import java.util.Map;

public class ManualController extends SimController implements GGKeyListener {
    private Machine machine;

    /**
     * Constructor to initialise ManualController with the simulation instance and machine map.
     *
     * @param sim       The OreSim instance representing the main simulation.
     * @param machines  A map containing machines organised by type and ID.
     * */
    public ManualController(OreSim sim, Map<String, Map<Integer, Machine>> machines) {
        super(sim, machines);
    }

    /**
     * Sets the current machine to control based on type and ID.
     *
     * @param type  The type of machine.
     * @param id    The ID of the machine.
     * */
    public void setMachine(String type, int id) {
        this.machine = getMachine(type, id);
    }

    /**
     * Handles the keyPressed event for manual machine control and simulation update.
     *
     * @param keyEvent  The KeyEvent object representing the key that was pressed.
     * @return          True if the key event is consumed, false otherwise.
     * */
    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        // Code to manually change machine
        /*
        String m = switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_1 -> "P";
            case KeyEvent.VK_2 -> "B";
            case KeyEvent.VK_3 -> "E";
            default -> null;
        };

        if (m != null) {
            setMachine(m, 1);
            return true;
        }
        */

        // Try to move the current machine and update simulation log.
        if (machine.tryMove(keyEvent)) {
            // Only update log if move was successful
            updateLog();
        }

        // Refresh the simulation display
        refreshSim();

        return true;
    }

    /**
     * Handles the keyReleased event
     *
     * @param keyEvent The KeyEvent object representing the key that was released.
     * @return         True indicates that the key event is consumed, false otherwise.
     * */
    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        return true;
    }
}
