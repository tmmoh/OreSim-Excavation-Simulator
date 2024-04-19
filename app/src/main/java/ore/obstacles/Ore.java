package ore.obstacles;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;
import ore.MovableActor;

import java.util.List;
import java.util.stream.Collectors;

public class Ore extends MovableActor {
    public Ore() { super("sprites/ore.png",2); }

    /**
     * Attempts to move the ore in the specified direction.
     *
     * @param dir   The compass direction in which the ore intends to move.
     * @return      True if the move is successful, false otherwise.
     * */
    @Override
    public boolean tryMove(Location.CompassDirection dir) {
        // Reset target if moving out of it
        List<Actor> actors = gameGrid.getActorsAt(getLocation());
        List<Actor> targets = actors.stream().filter((actor -> actor instanceof Target)).collect(Collectors.toList());

        if (!super.tryMove(dir)) {
            return false;
        }

        if (!targets.isEmpty()) {
            targets.get(0).show();
            this.show(0);
        }

        // Update target if moving into it
        Target target = (Target) gameGrid.getOneActorAt(getLocation(), Target.class);
        if (target != null) {
            show(1);
            target.hide();
        }

        return true;
    }
}

