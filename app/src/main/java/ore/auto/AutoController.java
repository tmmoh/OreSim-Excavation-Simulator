package ore.auto;

import ore.machines.Machine;
import ore.OreSim;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AutoController {
    private final OreSim sim;
    private final Map<String, Map<Integer, Machine>> machines;
    private final List<String> controls;

    /**
     * Constructor to initialise the AutoController with the simulation instance.
     *
     * @param sim           The OreSim instance representing the main simulation.
     * @param properties    The properties containing machine movement information.
     * @param machines      A map containing machines organised by type and ID.
     * */
    public AutoController(OreSim sim, Properties properties, Map<String, Map<Integer, Machine>> machines) {
        this.sim = sim;
        this.machines = machines;
        this.controls = List.of(properties.getProperty("machines.movements").split(","));
    }

    /**
     * Executes predefined movements for machines.
     * */
    public void runControls() {
         class RunThread extends Thread {
            @Override
            public void run() {
                for (String control : controls) {
                    String[] move = control.split("-");
                    machines.get(move[0]).get(1).tryMove(move[1]);
                    sim.updateLogResult();
                    sim.refresh();
                }
            }
        }

        RunThread runThread = new RunThread();
        runThread.start();
    }
}
