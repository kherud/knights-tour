package main;

import org.omg.CORBA.CODESET_INCOMPATIBLE;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public enum TourRecorder {
    instance;

    private Connection connection;
    private String driverName = "jdbc:hsqldb:";
    private String username = "sa";
    private String password = "";

    public void startup() {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            String databaseURL = driverName + Configuration.instance.databaseFile;
            connection = DriverManager.getConnection(databaseURL, username, password);
            if (Configuration.instance.isDebug)
                System.out.println("TourRecorder - connection : " + connection);
        } catch (Exception e) {
            if (Configuration.instance.isDebug) System.out.println(e.getMessage());
        }
    }

    public void init() {
        dropTable();
        createTable();
    }

    public synchronized void update(String sqlStatement) {
        try {
            Statement statement = connection.createStatement();
            int result = statement.executeUpdate(sqlStatement);
            if (result == -1)
                System.out.println("error executing " + sqlStatement);
            statement.close();
        } catch (SQLException sqle) {
            if (Configuration.instance.isDebug) System.out.println(sqle.getMessage());
        }
    }

    public void dropTable() {
        StringBuilder sqlStringBuilder = new StringBuilder();
        sqlStringBuilder.append("DROP TABLE knights_tour");
        if (Configuration.instance.isDebug) {
            System.out.println("--- dropTable");
            System.out.println("sqlStringBuilder : " + sqlStringBuilder.toString());
        }
        update(sqlStringBuilder.toString());
    }

    public void createTable() {
        StringBuilder sqlStringBuilder = new StringBuilder();
        sqlStringBuilder.append("CREATE TABLE knights_tour ").append(" ( ");
        sqlStringBuilder.append("id BIGINT NOT NULL").append(",");
        sqlStringBuilder.append("tour VARCHAR(511) NOT NULL").append(",");
        sqlStringBuilder.append("PRIMARY KEY (id)");
        sqlStringBuilder.append(" )");
        update(sqlStringBuilder.toString());
    }

    public String buildSQLStatement(long id, String tour) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO knights_tour (id,tour) VALUES (");
        stringBuilder.append(id).append(",");
        stringBuilder.append("'").append(tour).append("'");
        stringBuilder.append(")");
        if (Configuration.instance.isDebug) {
            System.out.println(stringBuilder.toString());
        }
        return stringBuilder.toString();
    }

    public void insert(String tour) {
        update(buildSQLStatement(System.nanoTime(), tour));
    }

    public void shutdown() {
        try {
            Statement statement = connection.createStatement();
            statement.execute("SHUTDOWN");
            connection.close();
            if (Configuration.instance.isDebug)
                System.out.println("TourRecorder - isClosed : " + connection.isClosed());
        } catch (SQLException sqle) {
            if (Configuration.instance.isDebug) System.out.println(sqle.getMessage());
        }
    }
}
