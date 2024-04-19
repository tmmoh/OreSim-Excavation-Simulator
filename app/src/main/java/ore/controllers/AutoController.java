/**
 * Workshop 03
 * Team 19
 * Team Members:
 * - Taher Mohamed, tmmoh@student.unimelbe.edu, @tmmoh, @The-Real-T
 * - Noel Abraham, ncabraham@student.unimelb.edu.au, @noelabraham1
 * - Spencer Vaughan, stvaughan@student.unimelb.edu.au, @SpencerTVaughan
 */
package ore.controllers;

import ore.machines.Machine;
import ore.OreSim;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AutoController extends SimController {
    private final List<String> controls;

    /**
     * Constructor to initialise the AutoController with the simulation instance.
     *
     * @param sim           The OreSim instance representing the main simulation.
     * @param properties    The properties containing machine movement information.
     * @param machines      A map containing machines organised by type and ID.
     * */
    public AutoController(OreSim sim, Properties properties, Map<String, Map<Integer, Machine>> machines) {
        super(sim, machines);
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
                    // Auto control machines are always id 1
                    getMachine(move[0], 1).tryMove(move[1]);
                    updateLog();
                    refreshSim();
                }
            }
        }

        RunThread runThread = new RunThread();
        runThread.start();
    }
}
