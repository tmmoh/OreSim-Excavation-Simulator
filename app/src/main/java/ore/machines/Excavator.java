package ore.machines;

import ore.obstacles.Rock;

import java.util.Collections;
import java.util.List;

public class Excavator extends Machine {
    private int rocksDestroyed;

    public Excavator(int id) {
        super(id, true, "sprites/excavator.png", Collections.emptyList(), List.of(Rock.class));  // Rotatable
        rocksDestroyed = 0;
    }
}
