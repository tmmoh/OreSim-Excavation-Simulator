package ore.machines;

import ore.obstacles.Rock;

import java.util.Collections;
import java.util.List;

public class Excavator extends Machine {
    private int rocksDestroyed;

    /**
     * Constructor to initialise the Excavator with its ID.
     *
     * @param id    The unique identifier for the excavator.
     * */
    public Excavator(int id) {
        super(id, true, "sprites/excavator.png", Collections.emptyList(), List.of(Rock.class));  // Rotatable
        rocksDestroyed = 0;
    }
}
