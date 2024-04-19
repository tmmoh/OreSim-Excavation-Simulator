package ore;

import java.util.Properties;

public class Driver {
    public static final String DEFAULT_PROPERTIES_PATH = "properties/game1.properties";

    public static void main(String[] args) {
        String propertiesPath = DEFAULT_PROPERTIES_PATH;
        if (args.length > 0) {
            propertiesPath = args[0];
        }
        final Properties properties = PropertiesLoader.loadPropertiesFile(propertiesPath);

        // Retrieve the map model from the properties sand initialize the map grid
        int model = Integer.parseInt(properties.getProperty("map"));
        MapGrid grid = new MapGrid(model);

        // Create an instance of OreSim with the loaded properties and map grid, run the simulation

        String logResult = new OreSim(properties, grid).runApp(true);
        System.out.println("logResult = " + logResult);
    }
}