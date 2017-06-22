package main;

public enum Configuration {
    instance;

    boolean isDebug = false;
    boolean recordRoutes = true;

    String[] colLabels = {"6", "5", "4", "3", "2", "1"};
    String[] rowLabels = {"a", "b", "c", "d", "e", "f"};

    String userDirectory = System.getProperty("user.dir");
    String fileSeparator = System.getProperty("file.separator");

    String dataDirectory = userDirectory + fileSeparator + "data" + fileSeparator;
    String databaseFile = dataDirectory + "knights_tour.db";
}
