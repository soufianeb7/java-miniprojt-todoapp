package com.todoapp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the MySQL database connection for the To-Do List application.
 *
 * <p>Implements the Singleton pattern to ensure only one connection
 * instance exists throughout the application lifecycle.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *     Connection conn = DatabaseConnection.getInstance().getConnection();
 * </pre>
 */
public class DatabaseConnection {

    // ─── Configuration ────────────────────────────────────────────────────────

    /** MySQL server address */
    private static final String HOST = "localhost";

    /** MySQL default port */
    private static final String PORT = "3308";

    /** The database name we created in Stage 2 */
    private static final String DATABASE = "todoapp_db";

    /** Your MySQL username */
    private static final String USERNAME = "root";

    /** Your MySQL password — change this to your actual password */
    private static final String PASSWORD = "931206";

    /** Full JDBC connection URL */
    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
                    + "?useSSL=false"
                    + "&serverTimezone=UTC"
                    + "&allowPublicKeyRetrieval=true";

    // ─── Singleton ────────────────────────────────────────────────────────────

    /** The single shared instance of this class */
    private static DatabaseConnection instance;

    /** The active MySQL connection */
    private Connection connection;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Private constructor — prevents external instantiation.
     * Opens the MySQL connection immediately when first called.
     *
     * @throws SQLException if the connection cannot be established
     */
    private DatabaseConnection() throws SQLException {
        this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        System.out.println("[DB] Connected to MySQL database: " + DATABASE);
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Returns the single shared instance of DatabaseConnection.
     * Creates it on first call (lazy initialization).
     *
     * @return the DatabaseConnection singleton instance
     * @throws SQLException if the connection cannot be established
     */
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns the active {@link Connection} object for executing SQL queries.
     *
     * @return the MySQL connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Closes the active database connection.
     * Should be called when the application shuts down.
     */
    public static void closeConnection() {
        try {
            if (instance != null && !instance.getConnection().isClosed()) {
                instance.getConnection().close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}