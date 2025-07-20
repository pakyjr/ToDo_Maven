package dao;

import models.User;
import models.ToDo;
import models.board.Board;
import models.board.BoardName;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Interfaccia Data Access Object (DAO) per la gestione dei dati relativi agli utenti, board e ToDo.
 */
public interface UserDAO {

    /**
     * Salva un nuovo utente nel database.
     *
     * @param user l'utente da salvare
     * @return true se l'utente è stato salvato con successo, false altrimenti
     * @throws SQLException se si verifica un errore SQL
     */
    boolean saveUser(User user) throws SQLException;

    /**
     * Recupera un utente in base allo username.
     *
     * @param username lo username dell'utente
     * @return un Optional contenente l'utente se trovato, altrimenti vuoto
     * @throws SQLException se si verifica un errore SQL
     */
    Optional<User> getUserByUsername(String username) throws SQLException;

    /**
     * Carica tutti i board e i ToDo associati a un utente.
     *
     * @param user l'utente di cui caricare i dati
     * @throws SQLException se si verifica un errore SQL
     */
    void loadUserBoardsAndToDos(User user) throws SQLException;

    /**
     * Ottiene l'ID di un board specifico per utente.
     *
     * @param boardName il nome del board
     * @param username lo username dell'utente
     * @return l'ID del board
     * @throws SQLException se si verifica un errore SQL
     */
    int getBoardId(BoardName boardName, String username) throws SQLException;

    /**
     * Salva un nuovo ToDo in un board specifico.
     *
     * @param toDo il ToDo da salvare
     * @param boardId l'ID del board associato
     * @throws SQLException se si verifica un errore SQL
     */
    void saveToDo(ToDo toDo, int boardId) throws SQLException;

    /**
     * Aggiorna un ToDo esistente.
     *
     * @param toDo il ToDo aggiornato
     * @param boardId l'ID del board associato
     * @throws SQLException se si verifica un errore SQL
     */
    void updateToDo(ToDo toDo, int boardId) throws SQLException;

    /**
     * Aggiorna l'ID del board associato a un ToDo.
     *
     * @param toDoId l'ID del ToDo da aggiornare
     * @param newBoardId il nuovo ID del board
     * @throws SQLException se si verifica un errore SQL
     */
    void updateToDoBoardId(String toDoId, int newBoardId) throws SQLException;

    /**
     * Elimina un ToDo.
     *
     * @param toDoId l'ID del ToDo
     * @param username lo username del proprietario
     * @throws SQLException se si verifica un errore SQL
     */
    void deleteToDo(String toDoId, String username) throws SQLException;

    /**
     * Condivide un ToDo con un altro utente.
     *
     * @param toDoId l'ID del ToDo da condividere
     * @param sharedWithUsername lo username dell'utente con cui condividere
     * @throws SQLException se si verifica un errore SQL
     */
    void shareToDo(String toDoId, String sharedWithUsername) throws SQLException;

    /**
     * Rimuove la condivisione di un ToDo con un utente specifico.
     *
     * @param toDoId l'ID del ToDo
     * @param sharedWithUsername l'username da rimuovere
     * @throws SQLException se si verifica un errore SQL
     */
    void removeToDoSharing(String toDoId, String sharedWithUsername) throws SQLException;

    /**
     * Rimuove tutte le condivisioni associate a un ToDo.
     *
     * @param toDoId l'ID del ToDo
     * @throws SQLException se si verifica un errore SQL
     */
    void removeAllToDoSharing(String toDoId) throws SQLException;

    /**
     * Restituisce l'insieme di tutti gli utenti presenti nel sistema.
     *
     * @return un insieme di oggetti User
     * @throws SQLException se si verifica un errore SQL
     */
    Set<User> getAllUsers() throws SQLException;

    /**
     * Ottiene gli username degli utenti con cui un ToDo è condiviso.
     *
     * @param toDoId l'ID del ToDo
     * @return una lista di username
     * @throws SQLException se si verifica un errore SQL
     */
    List<String> getSharedUsernamesForToDo(String toDoId) throws SQLException;

    /**
     * Salva un nuovo board per un utente.
     *
     * @param board il board da salvare
     * @param userId l'UUID dell'utente proprietario
     * @throws SQLException se si verifica un errore SQL
     */
    void saveBoard(Board board, UUID userId) throws SQLException;

    /**
     * Aggiorna un board esistente.
     *
     * @param board il board aggiornato
     * @throws SQLException se si verifica un errore SQL
     */
    void updateBoard(Board board) throws SQLException;
}
