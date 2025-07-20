package db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe di utility per gestire una connessione singleton al database PostgreSQL.
 */
public class DatabaseConnection {
    private static Connection conn;

    /** URL del database */
    private static String url = "jdbc:postgresql://localhost:5432/ToDo";

    /** Nome utente per il database */
    private static String username = "postgres";

    /** Password per il database */
    private static String password = "Sherlock96@";

    /**
     * Restituisce un'istanza condivisa (singleton) della connessione al database.
     * Se la connessione è nulla o chiusa, ne crea una nuova.
     *
     * @return Connessione al database PostgreSQL
     * @throws SQLException se si verifica un errore nella connessione
     */
    public static Connection getInstance() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(url, username, password);
        }
        return conn;
    }
}
