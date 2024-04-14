package ore.machines;

import ore.obstacles.Clay;

import java.util.Collections;
import java.util.List;

public class Bulldozer extends Machine {
    public Bulldozer(int id) {
        super(id, true, "sprites/bulldozer.png", Collections.emptyList(), List.of(Clay.class));  // Rotatable
    }
}
