package ore;

import ch.aplu.jgamegrid.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Properties;

public class OreSim extends GameGrid implements GGKeyListener
{
  // ------------- Inner classes -------------
  public enum ElementType{
    OUTSIDE("OS"), EMPTY("ET"), BORDER("BD"),
    PUSHER("P"), BULLDOZER("B"), EXCAVATOR("E"), ORE("O"),
    ROCK("R"), CLAY("C"), TARGET("T");
    private String shortType;

    ElementType(String shortType) {
      this.shortType = shortType;
    }

    public String getShortType() {
      return shortType;
    }

    public static ElementType getElementByShortType(String shortType) {
      ElementType[] types = ElementType.values();
      for (ElementType type: types) {
        if (type.getShortType().equals(shortType)) {
          return type;
        }
      }

      return ElementType.EMPTY;
    }
  }

  private class Target extends Actor
  {
    public Target()
    {
      super("sprites/target.gif");
    }
  }

  private class Ore extends Actor
  {
    public Ore()
    {
      super("sprites/ore.png",2);
    }
  }

  private class Pusher extends Actor
  {
    private List<String> controls = null;
    private int autoMovementIndex = 0;
    public Pusher()
    {
      super(true, "sprites/pusher.png");  // Rotatable
    }
    public void setupPusher(boolean isAutoMode, List<String> controls) {
      this.controls = controls;
    }

    /**
     * Method to move pusher automatically based on the instructions input from properties file
     */
    public void autoMoveNext() {
      if (controls != null && autoMovementIndex < controls.size()) {
        String[] currentMove = controls.get(autoMovementIndex).split("-");
        String machine = currentMove[0];
        String move = currentMove[1];
        autoMovementIndex++;
        if (machine.equals("P")) {
          if (isFinished)
            return;

          Location next = null;
          switch (move)
          {
            case "L":
              next = getLocation().getNeighbourLocation(Location.WEST);
              setDirection(Location.WEST);
              break;
            case "U":
              next = getLocation().getNeighbourLocation(Location.NORTH);
              setDirection(Location.NORTH);
              break;
            case "R":
              next = getLocation().getNeighbourLocation(Location.EAST);
              setDirection(Location.EAST);
              break;
            case "D":
              next = getLocation().getNeighbourLocation(Location.SOUTH);
              setDirection(Location.SOUTH);
              break;
          }

          Target curTarget = (Target) getOneActorAt(getLocation(), Target.class);
          if (curTarget != null){
            curTarget.show();
          }
          if (next != null && canMove(next))
          {
            setLocation(next);
          }
          refresh();
        }
      }
    }
  }

  private class Bulldozer extends Actor
  {
    public Bulldozer()
    {
      super(true, "sprites/bulldozer.png");  // Rotatable
    }
  }

  private class Excavator extends Actor
  {
    public Excavator()
    {
      super(true, "sprites/excavator.png");  // Rotatable
    }
  }

  private class Rock extends Actor
  {
    public Rock() {super("sprites/rock.png");}
  }

  private class Clay extends Actor
  {
    public Clay() {super("sprites/clay.png");  }
    }
  // ------------- End of inner classes ------
  //
  private MapGrid grid;
  private int nbHorzCells;
  private int nbVertCells;
  private final Color borderColor = new Color(100, 100, 100);
  private Ore[] ores;
  private Target[] targets;
  private Pusher pusher;
  private Bulldozer bulldozer;
  private Excavator excavator;
  private boolean isFinished = false;
  private Properties properties;
  private boolean isAutoMode;
  private double gameDuration;
  private List<String> controls;
  private int movementIndex;
  private StringBuilder logResult = new StringBuilder();
  public OreSim(Properties properties, MapGrid grid)
  {
    super(grid.getNbHorzCells(), grid.getNbVertCells(), 30, false);
    this.grid = grid;
    nbHorzCells = grid.getNbHorzCells();
    nbVertCells = grid.getNbVertCells();
    this.properties = properties;

    ores = new Ore[grid.getNbOres()];
    targets = new Target[grid.getNbOres()];

    isAutoMode = properties.getProperty("movement.mode").equals("auto");
    gameDuration = Integer.parseInt(properties.getProperty("duration"));
    setSimulationPeriod(Integer.parseInt(properties.getProperty("simulationPeriod")));
    controls = Arrays.asList(properties.getProperty("machines.movements").split(","));
  }

  /**
   * Check the number of ores that are collected
   * @return
   */

  private int checkOresDone() {
    int nbTarget = 0;
    for (int i = 0; i < grid.getNbOres(); i++)
    {
      if (ores[i].getIdVisible() == 1)
        nbTarget++;
    }

    return nbTarget;
  }

  /**
   * The main method to run the game
   * @param isDisplayingUI
   * @return
   */
  public String runApp(boolean isDisplayingUI) {
    GGBackground bg = getBg();
    drawBoard(bg);
    drawActors();
    addKeyListener(this);
    if (isDisplayingUI) {
      show();
    }

    if (isAutoMode) {
        doRun();
    }

    int oresDone = checkOresDone();
    double ONE_SECOND = 1000.0;
    while(oresDone < grid.getNbOres() && gameDuration >= 0) {
      try {
        Thread.sleep(simulationPeriod);
        double minusDuration = (simulationPeriod / ONE_SECOND);
        gameDuration -= minusDuration;
        String title = String.format("# Ores at Target: %d. Time left: %.2f seconds", oresDone, gameDuration);
        setTitle(title);
        if (isAutoMode) {
          pusher.autoMoveNext();
          updateLogResult();
        }

        oresDone = checkOresDone();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    doPause();

    if (oresDone == grid.getNbOres()) {
      setTitle("Mission Complete. Well done!");
    } else if (gameDuration < 0) {
      setTitle("Mission Failed. You ran out of time");
    }

    updateStatistics();
    isFinished = true;
    return logResult.toString();
  }

  /**
   * Transform the list of actors to a string of location for a specific kind of actor.
   * @param actors
   * @return
   */
  private String actorLocations(List<Actor> actors) {
    StringBuilder stringBuilder = new StringBuilder();
    boolean hasAddedColon = false;
    boolean hasAddedLastComma = false;
    for (int i = 0; i < actors.size(); i++) {
      Actor actor = actors.get(i);
      if (actor.isVisible()) {
        if (!hasAddedColon) {
          stringBuilder.append(":");
          hasAddedColon = true;
        }
        stringBuilder.append(actor.getX() + "-" + actor.getY());
        stringBuilder.append(",");
        hasAddedLastComma = true;
      }
    }

    if (hasAddedLastComma) {
      stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");
    }

    return stringBuilder.toString();
  }


  /**
   * Students need to modify this method so it can write an actual statistics into the statistics file. It currently
   *  only writes the sample data.
   */
  private void updateStatistics() {
    File statisticsFile = new File("statistics.txt");
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(statisticsFile);
      fileWriter.write("Pusher-1 Moves: 10\n");
      fileWriter.write("Excavator-1 Moves: 5\n");
      fileWriter.write("Excavator-1 Rock removed: 3\n");
      fileWriter.write("Bulldozer-1 Moves: 2\n");
      fileWriter.write("Bulldozer-1 Clay removed: 1\n");
    } catch (IOException e) {
      System.out.println("Cannot write to file - e: " + e.getLocalizedMessage());
    } finally {
      try {
        fileWriter.close();
      } catch (IOException e) {
        System.out.println("Cannot close file - e: " + e.getLocalizedMessage());
      }
    }
  }

  /**
   * Draw all different actors on the board: pusher, ore, target, rock, clay, bulldozer, excavator
   */
  private void drawActors()
  {
    int oreIndex = 0;
    int targetIndex = 0;

    for (int y = 0; y < nbVertCells; y++)
    {
      for (int x = 0; x < nbHorzCells; x++)
      {
        Location location = new Location(x, y);
        ElementType a = grid.getCell(location);
        if (a == ElementType.PUSHER)
        {
          pusher = new Pusher();
          addActor(pusher, location);
          pusher.setupPusher(isAutoMode, controls);
        }
        if (a == ElementType.ORE)
        {
          ores[oreIndex] = new Ore();
          addActor(ores[oreIndex], location);
          oreIndex++;
        }
        if (a == ElementType.TARGET)
        {
          targets[targetIndex] = new Target();
          addActor(targets[targetIndex], location);
          targetIndex++;
        }

        if (a == ElementType.ROCK)
        {
          addActor(new Rock(), location);
        }

        if (a == ElementType.CLAY)
        {
          addActor(new Clay(), location);
        }

        if (a == ElementType.BULLDOZER)
        {
          bulldozer = new Bulldozer();
          addActor(bulldozer, location);

        }
        if (a == ElementType.EXCAVATOR)
        {
          excavator = new Excavator();
          addActor(excavator, location);

        }
      }
    }
    System.out.println("ores = " + Arrays.asList(ores));
    setPaintOrder(Target.class);
  }

  /**
   * Draw the basic board with outside color and border color
   * @param bg
   */

  private void drawBoard(GGBackground bg)
  {
    bg.clear(new Color(230, 230, 230));
    bg.setPaintColor(Color.darkGray);
    for (int y = 0; y < nbVertCells; y++)
    {
      for (int x = 0; x < nbHorzCells; x++)
      {
        Location location = new Location(x, y);
        ElementType a = grid.getCell(location);
        if (a != ElementType.OUTSIDE)
        {
          bg.fillCell(location, Color.lightGray);
        }
        if (a == ElementType.BORDER)  // Border
          bg.fillCell(location, borderColor);
      }
    }
  }

  // Refactored method to handle movement
  private void handleMovement(Location next) {
    if (isFinished)
      return;

    Target curTarget = (Target) getOneActorAt(pusher.getLocation(), Target.class);
    if (curTarget != null) {
      curTarget.show();
    }

    if (next != null && canMove(next)) {
      pusher.setLocation(next);
      updateLogResult();
    }
    refresh();
  }

  /**
   * The method is automatically called by the framework when a key is pressed. Based on the pressed key, the pusher
   *  will change the direction and move
   * @param evt
   * @return
   */
  public boolean keyPressed(KeyEvent evt) {
    if (isFinished)
      return true;

    Location next = null;
    switch (evt.getKeyCode()) {
      case KeyEvent.VK_LEFT:
        next = pusher.getLocation().getNeighbourLocation(Location.WEST);
        pusher.setDirection(Location.WEST);
        break;
      case KeyEvent.VK_UP:
        next = pusher.getLocation().getNeighbourLocation(Location.NORTH);
        pusher.setDirection(Location.NORTH);
        break;
      case KeyEvent.VK_RIGHT:
        next = pusher.getLocation().getNeighbourLocation(Location.EAST);
        pusher.setDirection(Location.EAST);
        break;
      case KeyEvent.VK_DOWN:
        next = pusher.getLocation().getNeighbourLocation(Location.SOUTH);
        pusher.setDirection(Location.SOUTH);
        break;
    }
    handleMovement(next);
    return true;
  }

  public boolean keyReleased(KeyEvent evt)
  {
    return true;
  }

  /**
   * Check if we can move the pusher into the location
   * @param location
   * @return
   */
  private boolean canMove(Location location)
  {
    // Test if try to move into border, rock or clay
    Color c = getBg().getColor(location);
    Rock rock = (Rock)getOneActorAt(location, Rock.class);
    Clay clay = (Clay)getOneActorAt(location, Clay.class);
    Bulldozer bulldozer = (Bulldozer)getOneActorAt(location, Bulldozer.class);
    Excavator excavator = (Excavator)getOneActorAt(location, Excavator.class);
    if (c.equals(borderColor) || rock != null || clay != null || bulldozer != null || excavator != null)
      return false;
    else // Test if there is an ore
    {
      Ore ore = (Ore)getOneActorAt(location, Ore.class);
      if (ore != null)
      {

          // Try to move the ore
           ore.setDirection(pusher.getDirection());
          if (moveOre(ore))
            return true;
          else
            return false;

      }
    }

    return true;
  }

  /**
   * When the pusher pushes the ore in 1 direction, this method will be called to check if the ore can move in that direction
   *  and if it can move, then it changes the location
   * @param ore
   * @return
   */
  private boolean moveOre(Ore ore)
  {
    Location next = ore.getNextMoveLocation();
    // Test if try to move into border
    Color c = getBg().getColor(next);;
    Rock rock = (Rock)getOneActorAt(next, Rock.class);
    Clay clay = (Clay)getOneActorAt(next, Clay.class);
    if (c.equals(borderColor) || rock != null || clay != null)
      return false;

    // Test if there is another ore
    Ore neighbourOre =
      (Ore)getOneActorAt(next, Ore.class);
    if (neighbourOre != null)
      return false;

    // Reset the target if the ore is moved out of target
    Location currentLocation = ore.getLocation();
    List<Actor> actors = getActorsAt(currentLocation);
    if (actors != null) {
      for (Actor actor : actors) {
        if (actor instanceof Target) {
          Target currentTarget = (Target) actor;
          currentTarget.show();
          ore.show(0);
        }
      }
    }

    // Move the ore
    ore.setLocation(next);

    // Check if we are at a target
    Target nextTarget = (Target) getOneActorAt(next, Target.class);
    if (nextTarget != null) {
      ore.show(1);
      nextTarget.hide();
    } else {
      ore.show(0);
    }

    return true;
  }

  /**
   * The method will generate a log result for all the movements of all actors
   * The log result will be tested against our expected output.
   * Your code will need to pass all the 3 test suites with 9 test cases.
   */
  private void updateLogResult() {
    movementIndex++;
    Map<ElementType, List<Actor>> actorMap = new HashMap<>();
    actorMap.put(ElementType.PUSHER, getActors(Pusher.class));
    actorMap.put(ElementType.ORE, getActors(Ore.class));
    actorMap.put(ElementType.TARGET, getActors(Target.class));
    actorMap.put(ElementType.ROCK, getActors(Rock.class));
    actorMap.put(ElementType.CLAY, getActors(Clay.class));
    actorMap.put(ElementType.BULLDOZER, getActors(Bulldozer.class));
    actorMap.put(ElementType.EXCAVATOR, getActors(Excavator.class));

    logResult.append(movementIndex).append("#");
    for (Map.Entry<ElementType, List<Actor>> entry : actorMap.entrySet()) {
      ElementType type = entry.getKey();
      List<Actor> actors = entry.getValue();
      logResult.append(type.getShortType()).append(actorLocations(actors)).append("#");
    }
    logResult.append("\n");
  }

}
