package fees.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Database Connection Manager
 * Unit 4 Syllabus Concept: JDBC Connection Management
 * Database Tables: students, fees, receipts
 * 
 * Provides standard JDBC connections via PureJavaJDBC engine (zero external dependencies)
 * or optional standard external MySQL server when configured.
 */
public class DBConnection {

    private static Connection activeConnection = null;
    private static String activeMode = "Pure Java JDBC Engine (Standard Java SE)";

    /**
     * Obtains or establishes active JDBC Connection.
     * @return active java.sql.Connection
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (activeConnection != null && !activeConnection.isClosed()) {
            return activeConnection;
        }

        // Standard pure Java embedded JDBC connection (zero external JARs, zero setup required)
        activeConnection = PureJavaJDBC.createConnection();
        activeMode = "Pure Java JDBC (In-Memory SE)";
        return activeConnection;
    }

    /**
     * Optional helper to connect to an external MySQL server if requested.
     */
    public static synchronized Connection connectMySQL(String host, int port, String database, String user, String pass) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            activeConnection = DriverManager.getConnection(url, user, pass);
            activeMode = "MySQL Database (" + host + ":" + port + ")";
            return activeConnection;
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found on classpath. Using Pure Java JDBC instead.", e);
        }
    }

    public static String getActiveMode() {
        return activeMode;
    }

    public static boolean isConnected() {
        try {
            return activeConnection != null && !activeConnection.isClosed();
        } catch (Exception e) {
            return false;
        }
    }
}
