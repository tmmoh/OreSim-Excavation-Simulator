package ore.manual;

import ch.aplu.jgamegrid.GGKeyListener;
import ore.machines.Machine;
import ore.OreSim;

import java.awt.event.KeyEvent;
import java.util.Map;

public class ManualController implements GGKeyListener {
    private final OreSim sim;
    private final Map<String, Map<Integer, Machine>> machines;
    private Machine machine;

    public ManualController(OreSim sim, Map<String, Map<Integer, Machine>> machines) {
        this.sim = sim;
        this.machines = machines;
    }

    public void setMachine(String type, int id) {
        this.machine = machines.get(type).get(id);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {

        // Code to manually change machine
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


        if (machine.tryMove(keyEvent)) {
            sim.updateLogResult();
        }

        sim.refresh();

        return true;
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        return true;
    }
}
