package ore.sim;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;

public abstract class MovableActor extends Actor {
    public MovableActor(String filename, int nbSprites) {
        super(filename, nbSprites);
    }

    public MovableActor(boolean isRotatable, String filename) {
        super(isRotatable, filename);
    }

    protected boolean canMove(Location location) {
        // Test if trying to move into border
        if (gameGrid.getBg().getColor(location).equals(OreSim.borderColor)) {
            return false;
        }

        // Test if there's an actor in the way
        Actor actor = gameGrid.getOneActorAt(location);

        return (actor instanceof Target || actor == null);
    }

    public boolean tryMove(Location.CompassDirection dir) {
        setDirection(dir);
        Location next = getNextMoveLocation();

        if (canMove(next)) {
            move();
            return true;
        }

        return false;
    }
}
