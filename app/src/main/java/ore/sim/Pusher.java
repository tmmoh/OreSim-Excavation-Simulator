package ore.sim;

import ch.aplu.jgamegrid.*;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

abstract class Machine extends MovableActor {
    private List<String> controls = null;
    private int autoMovementIndex = 0;
    private final List<Class<? extends MovableActor>> pushable;
    private final List<Class<? extends Actor>> destroyable;

    public Machine(boolean isRotatable, String fileName, List<Class<? extends MovableActor>> pushable, List<Class<? extends Actor>> destroyable) {
        super(isRotatable, fileName);
        this.pushable = pushable;
        this.destroyable = destroyable;
    }

    public void setupMachine(boolean isAutoMode, List<String> controls) {
        this.controls = controls;
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

}

class OrePusher extends Machine {
    public OrePusher() {
        super(true, "sprites/pusher.png", List.of(Ore.class), Collections.emptyList());  // Rotatable
    }
}

class Bulldozer extends Machine {
    public Bulldozer() {
        super(true, "sprites/bulldozer.png", Collections.emptyList(), List.of(Rock.class));  // Rotatable
    }
}

class Excavator extends Machine {
    public Excavator() {
        super(true, "sprites/excavator.png", Collections.emptyList(), List.of(Clay.class));  // Rotatable
    }
}

