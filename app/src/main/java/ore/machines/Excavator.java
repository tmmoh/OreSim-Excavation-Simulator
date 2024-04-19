/**
 * Workshop 03
 * Team 19
 * Team Members:
 * - Taher Mohamed, tmmoh@student.unimelbe.edu, @tmmoh, @The-Real-T
 * - Noel Abraham, ncabraham@student.unimelb.edu.au, @noelabraham1
 * - Spencer Vaughan, stvaughan@student.unimelb.edu.au, @SpencerTVaughan
 */
package ore.machines;

import ore.obstacles.Rock;

import java.util.Collections;
import java.util.List;

public class Excavator extends Machine {
    /**
     * Constructor to initialise the Excavator with its ID.
     *
     * @param id    The unique identifier for the excavator.
     * */
    public Excavator(int id) {
        super(id, true, "sprites/excavator.png", Collections.emptyList(), List.of(Rock.class));  // Rotatable
    }
}
