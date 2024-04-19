/**
 * Workshop 03
 * Team 19
 * Team Members:
 * - Taher Mohamed, tmmoh@student.unimelbe.edu, @tmmoh, @The-Real-T
 * - Noel Abraham, ncabraham@student.unimelb.edu.au, @noelabraham1
 * - Spencer Vaughan, stvaughan@student.unimelb.edu.au, @SpencerTVaughan
 */
package ore;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;
import ore.OreSim;
import ore.obstacles.Target;

/**
 * Abstract class representing a movable actor in the simulation environment.
 * Extends the Actor class
 * */
public abstract class MovableActor extends Actor {
    /**
     * Constructs a new MovableActor with the specified image file and number of sprites
     *
     * @param filename  The filename of the image representing the actor.
     * @param nbSprites The number of sprites in the image file.
     * */
    public MovableActor(String filename, int nbSprites) {
        super(filename, nbSprites);
    }

    /**
     * Constructs a new MovableActor with the specified rotatability and image file.
     *
     * @param isRotatable Whether the actor can be rotated.
     * @param filename    The filename of the image representing the actor.
     * */
    public MovableActor(boolean isRotatable, String filename) {
        super(isRotatable, filename);
    }

    /**
     * Checks if the actor can move to the specified location
     *
     * @param location  The location to move to.
     * @return          True if the actor can move to the specified location, false otherwise.
     * */
    protected boolean canMove(Location location) {
        // Test if trying to move into border
        if (gameGrid.getBg().getColor(location).equals(MapGrid.BORDER_COLOR)) {
            return false;
        }

        // Test if there's an actor in the way
        Actor actor = gameGrid.getOneActorAt(location);

        // The actor can move if there's no actor at the location or if it's a target
        return (actor instanceof Target || actor == null);
    }

    /**
     * Attempts to move the actor in the specified direction.
     *
     * @param dir   The direction in which to move the actor.
     * @return      True if the actor successfully moved, false otherwise.
     * */
    public boolean tryMove(Location.CompassDirection dir) {
        setDirection(dir);
        Location next = getNextMoveLocation();

        // Move the actor if it can move to the next location
        if (canMove(next)) {
            move();
            return true;
        }

        return false;
    }
}
