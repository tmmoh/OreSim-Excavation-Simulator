package ore.machines;

import ore.obstacles.Clay;

import java.util.Collections;
import java.util.List;

public class Bulldozer extends Machine {

    /**
     * Constructor to initialise the Bulldozer with its ID.
     *
     * @param id    The unique identifier for the bulldozer.
     * */
    public Bulldozer(int id) {
        super(id, true, "sprites/bulldozer.png", Collections.emptyList(), List.of(Clay.class));  // Rotatable
    }
}
