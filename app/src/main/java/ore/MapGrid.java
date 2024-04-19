package ore;
import ch.aplu.jgamegrid.*;

import java.awt.*;

/**
 * Represents the grid map of the simulation environment.
 * Each cell of the grid contains an element type defining its characteristics.
 * */
public class MapGrid {
    private final OreSim.ElementType[][] mapElements; // = new OreSim.ElementType[nbHorzCells][nbVertCells];
    private int nbOres = 0;
    public static final Color BORDER_COLOR = new Color(100, 100, 100);

    // Map layouts represented as strings with information on the number of hor/vert cells
    private final static String map_0 =
        "    xxxxx          " + // 0 (19)
        "    x...x          " + // 1
        "    x*..x          " + // 2
        "  xxx...xx         " + // 3
        "  x......x         " + // 4
        "xxx...RD.x   xxxxxx" + // 5
        "x.....RD.xxxxx....x" + // 6
        "x...*............ox" + // 7
        "xxxxx.DDD.xPxx...ox" + // 8
        "    x.....xxxxxxxxx" + // 9
        "    xxxxxxx        ";  //10

    private final static int nbHorzCells_0 = 19;
    private final static int nbVertCells_0 = 11;

    private final static String map_1 =
        "xxxxxxxxxxxx" + // 0  (14)
        "x..........x" + // 0  (14)
        "x....RB....x" + // 1
        "xo...R.*...x" + // 2
        "xo...RDDDDDx" + // 3
        "xP....ERRRRx" + // 4
        "x....RRR*.xx" + // 5
        "x..........x" + // 6
        "xxxxxxxxxxxx";  // 7

    private final static int nbHorzCells_1 = 12;
    private final static int nbVertCells_1 = 9;

    private final static String[] mapModel = {
      map_0, map_1
    };

    private final static int[] nbHorzCellsModel = {
        nbHorzCells_0, nbHorzCells_1
    };

    private final static int[] nbVertCellsModel = {
        nbVertCells_0, nbVertCells_1
    };

    private static int model;

    /**
    * Mapping from the string to a HashMap to prepare drawing
     *
    * @param model The index of the map model to use.
    */
    public MapGrid(int model) {
        MapGrid.model = model;
        int nbHorzCells = nbHorzCellsModel[model];
        int nbVertCells = nbVertCellsModel[model];

        // Initialize the grid map elements array
        mapElements = new OreSim.ElementType[nbHorzCells][nbVertCells];

        // Populate integer array
        for (int k = 0; k < nbVertCellsModel[model]; k++) {
            for (int i = 0; i < nbHorzCellsModel[model]; i++) {
                mapElements[i][k] = switch (mapModel[model].charAt(nbHorzCellsModel[model] * k + i)) {
                    case ' ' -> OreSim.ElementType.OUTSIDE;  // Empty outside case
                    case '.' -> OreSim.ElementType.EMPTY;  // Empty inside
                    case 'x' -> OreSim.ElementType.BORDER;  // Border
                    case '*' -> { nbOres++; yield OreSim.ElementType.ORE; } // Ores
                    case 'o' -> OreSim.ElementType.TARGET;  // Target positions
                    case 'P' -> OreSim.ElementType.PUSHER;
                    case 'B' -> OreSim.ElementType.BULLDOZER;
                    case 'E' -> OreSim.ElementType.EXCAVATOR;
                    case 'R' -> OreSim.ElementType.ROCK; // Rocks
                    case 'D' -> OreSim.ElementType.CLAY; // Clay
                    default -> OreSim.ElementType.EMPTY; // Logically unreachable
                };
            }
        }
    }

    /**
     * Gets the number of horizontal cells in the grid.
     *
     * @return The number of horizontal cells.
     * */
    public int getNbHorzCells() {
        return nbHorzCellsModel[model];
    }

    /**
     * Gets the number of vertical cells in the grid.
     *
     * @return The number of vertical cells.
     * */
    public int getNbVertCells() {
        return nbVertCellsModel[model];
    }

    /**
     * Gets the number of ores in the grid.
     *
     * @return The number of ores.
     * */
    public int getNbOres() {
        return nbOres;
    }

    /**
     * Retrieves the element type at the specified location in the grid.
     *
     * @param location The location to retrieve the element type from.
     * @return         The element type at the specified location.
     * */
    public OreSim.ElementType getCell(Location location) {
        return mapElements[location.x][location.y];
    }
}
