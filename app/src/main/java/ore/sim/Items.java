package ore.sim;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class Target extends Actor {
    public Target()
    {
        super("sprites/target.gif");
    }
}

class Ore extends MovableActor implements Pushable {
    Target target = null;
    public Ore() { super("sprites/ore.png",2); }

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
        target = (Target) gameGrid.getOneActorAt(getLocation(), Target.class);
        if (target != null) {
            show(1);
            target.hide();
        }

        return true;
    }
}

class Rock extends Actor implements Destroyable {
    public Rock() { super("sprites/rock.png"); }
}

class Clay extends Actor implements Destroyable {
    public Clay() { super("sprites/clay.png"); }
}
