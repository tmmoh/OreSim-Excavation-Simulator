/**
 * Workshop 03
 * Team 19
 * Team Members:
 * - Taher Mohamed, tmmoh@student.unimelbe.edu, @tmmoh, @The-Real-T
 * - Noel Abraham, ncabraham@student.unimelb.edu.au, @noelabraham1
 * - Spencer Vaughan, stvaughan@student.unimelb.edu.au, @SpencerTVaughan
 */
package ore.machines;

import ore.obstacles.Ore;

import java.util.Collections;
import java.util.List;

public class Pusher extends Machine {
    /**
     * Constructor to initialise the Pusher with its ID.
     *
     * @param id    The unique identifier for the pusher.
     * */
    public Pusher(int id) {
        super(id, true, "sprites/pusher.png", List.of(Ore.class), Collections.emptyList());  // Rotatable
    }
}
