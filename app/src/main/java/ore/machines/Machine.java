package ore.machines;

import ch.aplu.jgamegrid.*;
import ore.MovableActor;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Machine extends MovableActor {
    private final int id;
    private final List<Class<? extends MovableActor>> pushable;
    private final List<Class<? extends Actor>> destroyable;
    private int moves;

    private final Map<Class<? extends MovableActor>, Integer> pushed;
    private final Map<Class<? extends Actor>, Integer> destroyed;

    public Machine(int id, boolean isRotatable, String fileName, List<Class<? extends MovableActor>> pushable, List<Class<? extends Actor>> destroyable) {
        super(isRotatable, fileName);
        this.pushable = pushable;
        this.destroyable = destroyable;
        this.id = id;
        this.moves = 0;

        this.pushed = new HashMap<>();
        this.destroyed = new HashMap<>();

        for (Class<? extends MovableActor> clazz : pushable) {
            pushed.put(clazz, 0);
        }

        for (Class<? extends Actor> clazz : destroyable) {
            destroyed.put(clazz, 0);
        }
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
                pushed.computeIfPresent(a.getClass(), (k, v) -> v + 1);
            }

            // Destroy any destroyable objects
            if (canDestroy(actor)) {
                actor.removeSelf();
                destroyed.computeIfPresent(actor.getClass(), (k, v) -> v + 1);
            }

            move();
            moves++;

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

    public int getMoves() {
        return moves;
    }

    public int getId() {
        return id;
    }

    public Map<Class<? extends Actor>, Integer> getDestroyed() {
        return destroyed;
    }
}

