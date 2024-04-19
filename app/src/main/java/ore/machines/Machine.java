package ore.machines;

import ch.aplu.jgamegrid.*;
import ore.MovableActor;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Machine extends MovableActor {
    private final int id;
    private final List<Class<? extends MovableActor>> pushable;
    private final List<Class<? extends Actor>> destroyable;
    private int moves;

    private final Map<Class<? extends MovableActor>, Integer> pushed;
    private final Map<Class<? extends Actor>, Integer> destroyed;

    /**
     * Constructor to initialise the Machine with its characteristics.
     *
     * @param id            The unique identifier for the machine.
     * @param isRotatable   Indicates whether the sprite is rotatable.
     * @param fileName      The file name of the image representing the machine.
     * @param pushable      List of classes of actors that can be pushed by this machine.
     * @param destroyable   List of classes of actors that can be destroyed by this machine.
     * */
    public Machine(int id, boolean isRotatable, String fileName, List<Class<? extends MovableActor>> pushable, List<Class<? extends Actor>> destroyable) {
        super(isRotatable, fileName);
        this.pushable = pushable;
        this.destroyable = destroyable;
        this.id = id;
        this.moves = 0;

        this.pushed = new LinkedHashMap<>();
        this.destroyed = new LinkedHashMap<>();

        for (Class<? extends MovableActor> clazz : pushable) {
            pushed.put(clazz, 0);
        }

        for (Class<? extends Actor> clazz : destroyable) {
            destroyed.put(clazz, 0);
        }
    }

    /**
     * Checks if an actor can be pushed by this machine.
     *
     * @param actor     The actor to be checked for pushability.
     * @return          True if the actor can be pushed, false otherwise.
     * */
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

    /**
     * Checks if an actor can be destroyed by this machine.
     *
     * @param actor     The actor to be checked for destroyability.
     * @return          True if the actor can be destroyed, false otherwise
     * */
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
     * Check if we can move the pusher into the location.
     *
     * @param location  The location to be checked for movement
     * @return          True if the machine can move to the location, false otherwise.
     */
    @Override
    protected boolean canMove(Location location) {

        Actor actor = gameGrid.getOneActorAt(location);

        // Check if the actor at location is not pushable or destroyable
        return (super.canMove(location) || canPush(actor) || canDestroy(actor));
    }

    /**
     * Attempts to move the machine in the specified direction.
     *
     * @param dir   The compass direction in which the machine intends to move.
     * @return      True if the move is successful, false otherwise.
     * */
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

    /**
     * Attempts to move the machine based on the specified key event.
     * @param evt   The KeyEvent representing the key event.
     * @return      True if the move is successful, false otherwise.
     * */
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

    /**
     * Attempts to move the machine based on the specified string representation of direction.
     *
     * @param move  The string representation of direction (e.g. "L" for left)
     * @return      True if the move is successful, false otherwise.
     * */
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

    /**
     * Retrieves the total number of moves made by the machine.
     *
     * @return  The total number of moves made by the machine.
     * */
    public int getMoves() {
        return moves;
    }

    /**
     * Retrieves the ID of the machine.
     *
     * @return THe ID of the machine.
     * */
    public int getId() {
        return id;
    }

    /**
     * Retrieves a map of destroyed classes and their destroyed count.
     *
     * @return A map of classes to destroy count.
     */
    public Map<Class<? extends Actor>, Integer> getDestroyed() {
        return destroyed;
    }
}

