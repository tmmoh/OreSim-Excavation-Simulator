package ore;

import ch.aplu.jgamegrid.*;
import ore.auto.AutoController;
import ore.manual.ManualController;
import ore.obstacles.Rock;
import ore.obstacles.Target;
import ore.machines.Bulldozer;
import ore.machines.Excavator;
import ore.machines.Machine;
import ore.machines.Pusher;
import ore.obstacles.Clay;
import ore.obstacles.Ore;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Properties;
import org.apache.commons.text.WordUtils;

public class OreSim extends GameGrid {
  // ------------- Inner classes -------------
  // Enum to represent different types of elements on the game grid
  public enum ElementType{
    OUTSIDE("OS"), EMPTY("ET"), BORDER("BD"),
    PUSHER("P"), BULLDOZER("B"), EXCAVATOR("E"), ORE("O"),
    ROCK("R"), CLAY("C"), TARGET("T");
    private final String shortType;

    ElementType(String shortType) {
      this.shortType = shortType;
    }

    public String getShortType() {
      return shortType;
    }
    // Method to retrieve ElementType based on its short type
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

  private final Map<String, Map<Integer, Machine>> machines;

  private final ManualController manualController;
  private final AutoController autoController;

  private final MapGrid grid;
  private final int nbHorzCells;
  private final int nbVertCells;
  private final Ore[] ores;
  private final Target[] targets;
  private boolean isFinished = false;
  private final boolean isAutoMode;

  private double gameDuration;
  private int movementIndex;

  private final StringBuilder logResult = new StringBuilder();

  /**
   * Constructor for the OreSim class, initialises the game grid, controllers, machines and other parameters
   *
   * @param properties Properties object containing simulation settings
   * @param grid       MapGrid object representing the game grid
   */
  public OreSim(Properties properties, MapGrid grid) {
    super(grid.getNbHorzCells(), grid.getNbVertCells(), 30, false);
    this.grid = grid;
    nbHorzCells = grid.getNbHorzCells();
    nbVertCells = grid.getNbVertCells();

    ores = new Ore[grid.getNbOres()];
    targets = new Target[grid.getNbOres()];

    isAutoMode = properties.getProperty("movement.mode").equals("auto");
    gameDuration = Integer.parseInt(properties.getProperty("duration"));
    setSimulationPeriod(Integer.parseInt(properties.getProperty("simulationPeriod")));

    machines = new LinkedHashMap<>();
    for (String type : List.of(ElementType.PUSHER.getShortType(), ElementType.EXCAVATOR.getShortType(), ElementType.BULLDOZER.getShortType())) {
      machines.put(type, new TreeMap<>());
    }

    autoController = new AutoController(this, properties, machines);
    manualController = new ManualController(this, machines);
  }

  /**
   * Method to check the number of ores that have been collected
   *
   * @return  A string containing the simulation log
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
   * @param isDisplayingUI  Flag indicating whether to display GUI
   * @return                A string containing the simulation log
   */
  public String runApp(boolean isDisplayingUI) {
    // Draw the initial game board
    GGBackground bg = getBg();
    drawBoard(bg);
    drawActors();
    if (isDisplayingUI) {
      show();
    }

    // Run simulation based on auto or manual mode
    if (isAutoMode) {
      doRun();
      autoController.runControls();
    } else {
      addKeyListener(manualController);
    }

    // Continue simulation until all ores are collected or time runs out
    int oresDone = checkOresDone();
    double ONE_SECOND = 1000.0;
    while(oresDone < grid.getNbOres() && gameDuration >= 0) {
      try {
        Thread.sleep(simulationPeriod);
        double minusDuration = (simulationPeriod / ONE_SECOND);
        gameDuration -= minusDuration;
        String title = String.format("# Ores at Target: %d. Time left: %.2f seconds", oresDone, gameDuration);
        setTitle(title);


        oresDone = checkOresDone();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    // Pause simulation and display final status
    doPause();

    if (oresDone == grid.getNbOres()) {
      setTitle("Mission Complete. Well done!");
    } else if (gameDuration < 0) {
      setTitle("Mission Failed. You ran out of time");
    }

    // Update and return simulation log
    updateStatistics();
    isFinished = true;
    return logResult.toString();
  }

  /**
   * Transform the list of actors to a string of location for a specific kind of actor.
   * @param actors  A list of Actor objects whose locations need to be represented.
   * @return        A string representing the locations of the actors in format "x1-y1, x2-y2,..."
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
   * Method to update statistics and write them to a file
   */
  private void updateStatistics() {
    // Initialize file writer
    File statisticsFile = new File("statistics.txt");
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(statisticsFile);

      // Iterate over each machine and write its statistics to the file
      FileWriter finalFileWriter = fileWriter;
      machines.forEach((type, machines) -> {
        machines.forEach((id, m) -> {
          try {
            ElementType t = ElementType.getElementByShortType(type);
            finalFileWriter.write( WordUtils.capitalize(t.name().toLowerCase()) + "-" + m.getId() + " Moves: " + m.getMoves() + "\n");

            // Write statistics for destroyed objects related to the machine
            m.getDestroyed().forEach((k, v) -> {
                try {
                    finalFileWriter.write( WordUtils.capitalize(t.name().toLowerCase()) + "-" + m.getId() + " " + k.getSimpleName() + " removed: " + v + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
          } catch (IOException e) {
            System.out.println("Cannot write to file - e: " + e.getLocalizedMessage());
          }
        });
      });

      } catch (IOException e) {
      // Handle IO Exception
    } finally {
      // Close the file writer
      try {
        fileWriter.close();
      } catch (IOException e) {
        System.out.println("Cannot close file - e: " + e.getLocalizedMessage());
      }
    }
  }

  /**
   * Helper method to calculate the total number of rocks removed by the excavator
   *
   * @return The total number of rocks removed by the excavator
   * */
  private int getNumberOfRocksRemoved() {
    int rocksRemoved = 0;
    List<Actor> rocks = getActors(Rock.class);
    for (int i = 0; i < rocks.size(); i++) {
      Actor rock = rocks.get(i);
      if (!rock.isVisible()) {
        rocksRemoved++;
      }
    }
    return rocksRemoved;
  }

  /**
   * Helper method to calculate the total number of clay removed by bulldozer.
   *
   * @return The total amount of clay removed by the bulldozer.
   * */
  private int getNumberOfClayRemoved() {
    int clayRemoved = 0;
    List<Actor> clays = getActors(Clay.class);
    for (int i = 0; i < clays.size(); i++) {
      Actor clay = clays.get(i);
      if (!clay.isVisible()) {
        clayRemoved++;
      }
    }
    return clayRemoved;
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

        // Add actors based on the type of element on the grid
        switch (a) {
          case PUSHER -> {
            String type = ElementType.PUSHER.getShortType();
            Pusher pusher = new Pusher(machines.get(type).size() + 1);
            addActor(pusher, location);
            machines.get(type).put(pusher.getId(), pusher);
            manualController.setMachine(type, pusher.getId());
          }
          case ORE -> {
            ores[oreIndex] = new Ore();
            addActor(ores[oreIndex], location);
            oreIndex++;
          }
          case TARGET -> {
            targets[targetIndex] = new Target();
            addActor(targets[targetIndex], location);
            targetIndex++;
          }
          case ROCK -> {
            addActor(new Rock(), location);
          }
          case CLAY -> {
            addActor(new Clay(), location);
          }
          case BULLDOZER -> {
            String type = ElementType.BULLDOZER.getShortType();
            Bulldozer bulldozer = new Bulldozer(machines.get(type).size() + 1);
            addActor(bulldozer, location);
            machines.get(type).put(bulldozer.getId(), bulldozer);
          }
          case EXCAVATOR -> {
            String type = ElementType.EXCAVATOR.getShortType();
            Excavator excavator = new Excavator(machines.get(type).size() + 1);
            addActor(excavator, location);
            machines.get(type).put(excavator.getId(), excavator);
          }
          default -> {}
        }
      }
    }
    System.out.println("ores = " + Arrays.asList(ores));
    setPaintOrder(Target.class);
  }

  /**
   * Draw the basic board with outside color and border color
   *
   * @param bg  The background object to draw on
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
          bg.fillCell(location, MapGrid.BORDER_COLOR);
      }
    }
  }



  /**
   * The method will generate a log result for all the movements of all actors
   * The log result will be tested against our expected output.
   * Your code will need to pass all the 3 test suites with 9 test cases.
   */
  public void updateLogResult() {
    movementIndex++;
    List<Actor> pushers = getActors(Pusher.class);
    List<Actor> ores = getActors(Ore.class);
    List<Actor> targets = getActors(Target.class);
    List<Actor> rocks = getActors(Rock.class);
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
