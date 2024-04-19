/**
 * Workshop 03
 * Team 19
 * Team Members:
 * - Taher Mohamed, tmmoh@student.unimelbe.edu, @tmmoh, @The-Real-T
 * - Noel Abraham, ncabraham@student.unimelb.edu.au, @noelabraham1
 * - Spencer Vaughan, stvaughan@student.unimelb.edu.au, @SpencerTVaughan
 */
package ore.controllers;

import ore.OreSim;
import ore.machines.Machine;

import java.util.Map;

public abstract class SimController {

    private final OreSim sim;
    private final Map<String, Map<Integer, Machine>> machines;

    public SimController(OreSim sim, Map<String, Map<Integer, Machine>> machines) {
        this.sim = sim;
        this.machines = machines;
    }

    /**
     * Starts the controller, allowing for machines to be controlled,
     * depends on subclass implementation
     */
    public abstract void startControls();

    /**
     * Stops the controller, no longer allowing for machines to be controlled,
     * depends on subclass implementation
     */
    public abstract void stopControls();

    protected void updateLog() {
        sim.updateLogResult();
    }

    protected void refreshSim() {
        sim.refresh();
    }

    protected Machine getMachine(String type, int id) {
        return machines.get(type).get(id);
    }

    protected OreSim getSim() { return sim; }
}
