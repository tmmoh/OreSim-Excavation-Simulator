package ore.sim;

import ch.aplu.jgamegrid.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Machine extends MovableActor {
    private final int id;
    private final List<Class<? extends MovableActor>> pushable;
    private final List<Class<? extends Actor>> destroyable;

    public Machine(int id, boolean isRotatable, String fileName, List<Class<? extends MovableActor>> pushable, List<Class<? extends Actor>> destroyable) {
        super(isRotatable, fileName);
        this.pushable = pushable;
        this.destroyable = destroyable;
        this.id = id;
    }

    private boolean canPush(Actor actor) {
        if (!(actor instanceof MovableActor)) {
            return false;
        }


        for (Class<?> clazz: pushable) {
            if (actor.getClass().equals(clazz)) {
                return true;
            }
        }

        return false;
    }

    private boolean canDestroy(Actor actor) {
        if (actor == null) {
            return false;
        }

        for (Class<?> clazz: destroyable) {
            if (actor.getClass().equals(clazz)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if we can move the pusher into the location
     * @param location
     * @return
     */
    @Override
    protected boolean canMove(Location location) {

        Actor actor = gameGrid.getOneActorAt(location);

        // Check if the actor at location is not pushable or destroyable
        return (super.canMove(location) || canPush(actor) || canDestroy(actor));
    }

    @Override
    public boolean tryMove(Location.CompassDirection dir) {
        setDirection(dir);
        Location next = getNextMoveLocation();

        if (canMove(next)) {
            // Try to push or destroy objects in the way
            Actor actor = gameGrid.getOneActorAt(next);

            if (canPush(actor)) {
                MovableActor a = (MovableActor) actor;
                if (!a.tryMove(dir)) {
                    return false;
                }
            }

            // Destroy any destroyable objects
            if (canDestroy(actor)) {
                actor.removeSelf();
            }

            move(1);

            return true;
        }

        return false;
    }

    public boolean tryMove(KeyEvent evt) {
        Location.CompassDirection direction = switch (evt.getKeyCode()) {
            case KeyEvent.VK_LEFT -> Location.CompassDirection.WEST;
            case KeyEvent.VK_UP -> Location.CompassDirection.NORTH;
            case KeyEvent.VK_RIGHT -> Location.CompassDirection.EAST;
            case KeyEvent.VK_DOWN -> Location.CompassDirection.SOUTH;
            default -> null;
        };

        return (direction != null && tryMove(direction));
    }

    public boolean tryMove(String move) {
        Location.CompassDirection direction = switch (move) {
            case "L" -> Location.CompassDirection.WEST;
            case "U" -> Location.CompassDirection.NORTH;
            case "R" -> Location.CompassDirection.EAST;
            case "D" -> Location.CompassDirection.SOUTH;
            default -> null;
        };

        return (direction != null && tryMove(direction));
    }

    public int getId() {
        return id;
    }

}

class OrePusher extends Machine {
    public OrePusher(int id) {
        super(id, true, "sprites/pusher.png", List.of(Ore.class), Collections.emptyList());  // Rotatable
    }
}

class Bulldozer extends Machine {
    public Bulldozer(int id) {
        super(id, true, "sprites/bulldozer.png", Collections.emptyList(), List.of(Clay.class));  // Rotatable
    }
}

class Excavator extends Machine {
    public Excavator(int id) {
        super(id, true, "sprites/excavator.png", Collections.emptyList(), List.of(Rock.class));  // Rotatable
    }
}

