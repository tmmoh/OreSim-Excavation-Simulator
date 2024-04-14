package ore.machines;

import ore.obstacles.Ore;

import java.util.Collections;
import java.util.List;

public class Pusher extends Machine {
    public Pusher(int id) {
        super(id, true, "sprites/pusher.png", List.of(Ore.class), Collections.emptyList());  // Rotatable
    }
}
