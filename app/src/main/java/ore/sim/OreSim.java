package ore.sim;

import ch.aplu.jgamegrid.*;
import ore.MapGrid;

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

  // ------------- End of inner classes ------
  //
  private MapGrid grid;
  private int nbHorzCells;
  private int nbVertCells;
  public static final Color borderColor = new Color(100, 100, 100);
  private Ore[] ores;
  private Target[] targets;
  private OrePusher pusher;
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

  private int autoMovementIndex = 0;
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

        Location.CompassDirection direction = switch (move) {
            case "L" -> Location.CompassDirection.WEST;
            case "U" -> Location.CompassDirection.NORTH;
            case "R" -> Location.CompassDirection.EAST;
            case "D" -> Location.CompassDirection.SOUTH;
            default -> null;
        };
        if (direction == null) {
          return;
        }
        pusher.tryMove(direction);
        refresh();
      }
    }
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
          autoMoveNext();
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
          pusher = new OrePusher();
          addActor(pusher, location);
          pusher.setupMachine(isAutoMode, controls);
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

  /**
   * The method is automatically called by the framework when a key is pressed. Based on the pressed key, the pusher
   *  will change the direction and move
   * @param evt
   * @return
   */
  public boolean keyPressed(KeyEvent evt)
  {
    if (isFinished)
      return true;

    Location.CompassDirection direction = switch (evt.getKeyCode()) {
        case KeyEvent.VK_LEFT -> Location.CompassDirection.WEST;
        case KeyEvent.VK_UP -> Location.CompassDirection.NORTH;
        case KeyEvent.VK_RIGHT -> Location.CompassDirection.EAST;
        case KeyEvent.VK_DOWN -> Location.CompassDirection.SOUTH;
        default -> null;
    };

    if (direction == null) {
      return false;
    }

    pusher.tryMove(direction);
    updateLogResult();
    refresh();
    return true;
  }

  public boolean keyReleased(KeyEvent evt)
  {
    return true;
  }



  /**
   * The method will generate a log result for all the movements of all actors
   * The log result will be tested against our expected output.
   * Your code will need to pass all the 3 test suites with 9 test cases.
   */
  private void updateLogResult() {
    movementIndex++;
    List<Actor> pushers = getActors(OrePusher.class);
    List<Actor> ores = getActors(Ore.class);
    List<Actor> targets = getActors(Target.class);
    List<Actor> rocks = getActors(Clay.class);
    List<Actor> clays = getActors(Clay.class);
    List<Actor> bulldozers = getActors(Bulldozer.class);
    List<Actor> excavators = getActors(Excavator.class);

    logResult.append(movementIndex + "#");
    logResult.append(ElementType.PUSHER.getShortType()).append(actorLocations(pushers)).append("#");
    logResult.append(ElementType.ORE.getShortType()).append(actorLocations(ores)).append("#");
    logResult.append(ElementType.TARGET.getShortType()).append(actorLocations(targets)).append("#");
    logResult.append(ElementType.ROCK.getShortType()).append(actorLocations(rocks)).append("#");
    logResult.append(ElementType.CLAY.getShortType()).append(actorLocations(clays)).append("#");
    logResult.append(ElementType.BULLDOZER.getShortType()).append(actorLocations(bulldozers)).append("#");
    logResult.append(ElementType.EXCAVATOR.getShortType()).append(actorLocations(excavators));

    logResult.append("\n");
  }

}
