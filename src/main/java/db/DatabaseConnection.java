package db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {
    private static Connection conn;
    private static String url = "jdbc:postgresql://localhost:5432/ToDo";
    private static String username = "postgres";
    private static String password = "Sherlock96@";

    public static Connection getInstance() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(
                    url , username, password);
        }
        return conn;
    }
}
