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

    protected void updateLog() {
        sim.updateLogResult();
    }

    protected void refreshSim() {
        sim.refresh();
    }

    protected Machine getMachine(String type, int id) {
        return machines.get(type).get(id);
    }
}
