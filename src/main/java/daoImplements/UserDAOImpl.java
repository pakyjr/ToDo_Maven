package dao;

import models.board.Board;
import models.ToDo;
import models.User;
import models.board.BoardName;
import db.DatabaseConnection;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Implementazione concreta del Data Access Object per la gestione degli utenti e delle relative entità.
 * Questa classe fornisce un'interfaccia per l'accesso ai dati persistenti nel database,
 * gestendo operazioni CRUD per utenti, board e todo con le relative condivisioni.
 */
public class UserDAOImpl implements UserDAO {

    /**
     * Connessione al database utilizzata per tutte le operazioni di persistenza.
     * Viene inizializzata tramite il singleton DatabaseConnection.
     */
    private Connection connection;

    /**
     * Costruisce una nuova istanza di UserDAOImpl inizializzando la connessione al database.
     *
     * @throws SQLException se si verifica un errore durante l'ottenimento della connessione al database
     */
    public UserDAOImpl() throws SQLException {
        this.connection = DatabaseConnection.getInstance();
    }

    /**
     * Salva un nuovo utente nel database con username, password hash e ID univoco.
     *
     * <p>Il metodo gestisce automaticamente i conflitti di username duplicati,
     * restituendo false invece di lanciare un'eccezione per migliorare l'esperienza utente.</p>
     *
     * @param user l'oggetto User da salvare nel database
     * @return true se l'utente è stato salvato con successo, false se esiste già un utente con lo stesso username
     * @throws SQLException se si verifica un errore durante l'operazione di inserimento nel database
     * @see User
     */
    @Override
    public boolean saveUser(User user) throws SQLException {
        String sql = "INSERT INTO users (id, username, password_hash) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getHashedPassword());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("DEBUG: User '" + user.getUsername() + "' saved to DB with ID: " + user.getId());
                return true;
            }
            return false;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.err.println("User with username '" + user.getUsername() + "' already exists.");
                return false;
            }
            throw e;
        }
    }

    /**
     * Salva una nuova board nel database associandola a un utente specifico.
     *
     * <p>Il metodo genera automaticamente un ID per la board e lo assegna all'oggetto
     * passato come parametro, permettendo riferimenti futuri alla board salvata.</p>
     *
     * @param board la board da salvare contenente nome e colore
     * @param userId l'UUID dell'utente proprietario della board
     * @throws SQLException se si verifica un errore durante l'operazione di inserimento
     * @see Board
     * @see BoardName
     */
    @Override
    public void saveBoard(Board board, UUID userId) throws SQLException {
        String sql = "INSERT INTO boards (name, color, user_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, board.getName().getDisplayName());
            pstmt.setString(2, board.getColor());
            pstmt.setObject(3, userId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        board.setId(generatedKeys.getInt(1));
                        System.out.println("DEBUG: Saved board '" + board.getName().getDisplayName() + "' to DB with ID: " + board.getId());
                    }
                }
            }
        }
    }

    /**
     * Aggiorna le proprietà di una board esistente nel database.
     *
     * <p>L'operazione viene eseguita solo se l'utente proprietario esiste nel database
     * e corrisponde al proprietario specificato nella board.</p>
     *
     * @param board la board con le proprietà aggiornate da persistere
     * @throws SQLException se si verifica un errore durante l'aggiornamento o se il proprietario non esiste
     * @see Board
     */
    @Override
    public void updateBoard(Board board) throws SQLException {
        String sql = "UPDATE boards SET name = ?, color = ? WHERE id = ? AND user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, board.getName().getDisplayName());
            pstmt.setString(2, board.getColor());
            pstmt.setInt(3, board.getId());

            Optional<User> ownerUser = getUserByUsername(board.getOwner());
            if (ownerUser.isPresent()) {
                pstmt.setObject(4, ownerUser.get().getId());
            } else {
                throw new SQLException("Cannot update board: Owner user '" + board.getOwner() + "' not found.");
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("DEBUG: Board '" + board.getName().getDisplayName() + "' (ID: " + board.getId() + ") updated in DB with new color: " + board.getColor());
            } else {
                System.out.println("DEBUG: Board '" + board.getName().getDisplayName() + "' (ID: " + board.getId() + ") not found for update or no changes.");
            }
        }
    }

    /**
     * Recupera un utente dal database utilizzando il suo username come chiave di ricerca.
     *
     * @param username l'username dell'utente da recuperare
     * @return Optional contenente l'utente se trovato, Optional vuoto altrimenti
     * @throws SQLException se si verifica un errore durante la query
     * @see User
     * @see Optional
     */
    @Override
    public Optional<User> getUserByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        null,
                        (UUID) rs.getObject("id")
                );
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    /**
     * Carica tutte le board e i todo associati a un utente, inclusi quelli condivisi.
     *
     * <p>Questo metodo esegue un'operazione complessa che:</p>
     * <ul>
     *   <li>Pulisce le board esistenti dell'utente</li>
     *   <li>Carica tutte le board di proprietà dell'utente</li>
     *   <li>Per ogni board, carica tutti i todo di proprietà dell'utente</li>
     *   <li>Carica tutti i todo condivisi con l'utente</li>
     *   <li>Per ogni todo, carica le relative attività e utenti con cui è condiviso</li>
     * </ul>
     *
     * @param user l'utente per cui caricare board e todo; se null, il metodo termina senza operazioni
     * @throws SQLException se si verifica un errore durante il caricamento dei dati
     * @see User
     * @see Board
     * @see ToDo
     */
    @Override
    public void loadUserBoardsAndToDos(User user) throws SQLException {
        if (user == null) {
            return;
        }

        user.clearBoards();

        // Step 1: Load all boards owned by the current user
        String boardSql = "SELECT id, name, color FROM boards WHERE user_id = ?";
        try (PreparedStatement pstmtBoard = connection.prepareStatement(boardSql)) {
            pstmtBoard.setObject(1, user.getId());
            ResultSet rsBoards = pstmtBoard.executeQuery();
            while (rsBoards.next()) {
                String boardDisplayName = rsBoards.getString("name");
                BoardName boardName = null;
                try {
                    boardName = BoardName.fromDisplayName(boardDisplayName);
                } catch (IllegalArgumentException e) {
                    System.err.println("ERROR: Unknown board display name '" + boardDisplayName + "' found in database for user " + user.getUsername() + ". Skipping this board.");
                    continue;
                }

                Board board = new Board(
                        rsBoards.getInt("id"),
                        boardName,
                        user.getUsername(),
                        rsBoards.getString("color")
                );
                user.addBoard(board);
                System.out.println("DEBUG: UserDAOImpl loaded board '" + boardDisplayName + "' (ID: " + board.getId() + ") for user '" + user.getUsername() + "'.");
            }
        }

        for (Board board : user.getBoardList()) {

            String todoSql = "SELECT id, title, description, status, due_date, created_date, position, owner_username, url, image, color FROM todos WHERE board_id = ? AND owner_username = ?";
            try (PreparedStatement pstmtTodo = connection.prepareStatement(todoSql)) {
                pstmtTodo.setInt(1, board.getId());
                pstmtTodo.setString(2, user.getUsername());
                ResultSet rsTodos = pstmtTodo.executeQuery();
                while (rsTodos.next()) {
                    ToDo toDo = new ToDo(
                            (UUID) rsTodos.getObject("id"),
                            rsTodos.getString("title"),
                            rsTodos.getString("owner_username")
                    );
                    toDo.setDescription(rsTodos.getString("description"));
                    toDo.setStatus(rsTodos.getString("status"));
                    java.sql.Date sqlDueDateOwned = rsTodos.getDate("due_date");
                    toDo.setDueDate(sqlDueDateOwned != null ? sqlDueDateOwned.toLocalDate() : null);
                    toDo.setCreatedDate(rsTodos.getDate("created_date").toLocalDate());
                    toDo.setPosition(rsTodos.getInt("position"));
                    toDo.setUrl(rsTodos.getString("url"));   // Load URL
                    toDo.setImage(rsTodos.getString("image")); // Load Image
                    toDo.setColor(rsTodos.getString("color")); // Load Color

                    String activitySql = "SELECT activity_title, completed FROM activities WHERE todo_id = ?";
                    try (PreparedStatement pstmtActivity = connection.prepareStatement(activitySql)) {
                        pstmtActivity.setObject(1, toDo.getId());
                        ResultSet rsActivities = pstmtActivity.executeQuery();
                        Map<String, Boolean> activitiesMap = new HashMap<>();
                        while (rsActivities.next()) {
                            activitiesMap.put(rsActivities.getString("activity_title"), rsActivities.getBoolean("completed"));
                        }
                        toDo.setActivityList(activitiesMap);
                    }

                    List<String> sharedUsernames = getSharedUsernamesForToDo(toDo.getId().toString());
                    for (String sharedUsername : sharedUsernames) {
                        Optional<User> sharedUser = getUserByUsername(sharedUsername);
                        sharedUser.ifPresent(toDo::addSharedUser);
                    }
                    board.addExistingTodo(toDo);
                    System.out.println("DEBUG: UserDAOImpl loaded OWNED ToDo '" + toDo.getTitle() + "' for board '" + board.getName().getDisplayName() + "'.");
                }
            }
        }

        String sharedTodoSql = "SELECT t.id, t.title, t.description, t.status, t.due_date, t.created_date, t.position, t.owner_username, t.url, t.image, t.color, " +
                "b_orig.name AS original_board_name " +
                "FROM todos t " +
                "JOIN shared_todos st ON t.id = st.todo_id " +
                "JOIN boards b_orig ON t.board_id = b_orig.id " +
                "WHERE st.shared_with_username = ?";

        try (PreparedStatement pstmtSharedTodo = connection.prepareStatement(sharedTodoSql)) {
            pstmtSharedTodo.setString(1, user.getUsername());
            ResultSet rsSharedTodos = pstmtSharedTodo.executeQuery();
            while (rsSharedTodos.next()) {
                ToDo sharedToDo = new ToDo(
                        (UUID) rsSharedTodos.getObject("id"),
                        rsSharedTodos.getString("title"),
                        rsSharedTodos.getString("owner_username")
                );
                sharedToDo.setDescription(rsSharedTodos.getString("description"));
                sharedToDo.setStatus(rsSharedTodos.getString("status"));
                java.sql.Date sqlDueDateShared = rsSharedTodos.getDate("due_date");
                sharedToDo.setDueDate(sqlDueDateShared != null ? sqlDueDateShared.toLocalDate() : null);
                sharedToDo.setCreatedDate(rsSharedTodos.getDate("created_date").toLocalDate());
                sharedToDo.setPosition(rsSharedTodos.getInt("position"));
                sharedToDo.setUrl(rsSharedTodos.getString("url"));     // Load URL
                sharedToDo.setImage(rsSharedTodos.getString("image")); // Load Image
                sharedToDo.setColor(rsSharedTodos.getString("color")); // Load Color

                String activitySqlShared = "SELECT activity_title, completed FROM activities WHERE todo_id = ?";
                try (PreparedStatement pstmtActivityShared = connection.prepareStatement(activitySqlShared)) {
                    pstmtActivityShared.setObject(1, sharedToDo.getId());
                    ResultSet rsActivitiesShared = pstmtActivityShared.executeQuery();
                    Map<String, Boolean> activitiesMapShared = new HashMap<>();
                    while (rsActivitiesShared.next()) {
                        activitiesMapShared.put(rsActivitiesShared.getString("activity_title"), rsActivitiesShared.getBoolean("completed"));
                    }
                    sharedToDo.setActivityList(activitiesMapShared);
                }

                List<String> sharedUsernamesForSharedToDo = getSharedUsernamesForToDo(sharedToDo.getId().toString());
                for (String sharedUsername : sharedUsernamesForSharedToDo) {
                    Optional<User> sharedUser = getUserByUsername(sharedUsername);
                    sharedUser.ifPresent(sharedToDo::addSharedUser);
                }

                Optional<User> currentUserOptional = getUserByUsername(user.getUsername());
                currentUserOptional.ifPresent(sharedToDo::addSharedUser);

                String originalBoardNameStr = rsSharedTodos.getString("original_board_name");
                BoardName originalBoardName = null;
                try {
                    originalBoardName = BoardName.fromDisplayName(originalBoardNameStr);
                } catch (IllegalArgumentException e) {
                    System.err.println("ERROR: Il nome della board originale '" + originalBoardNameStr + "' per il ToDo condiviso '" + sharedToDo.getTitle() + "' non è un valore valido per BoardName. Ignoro questo ToDo condiviso per il ricevente '" + user.getUsername() + "'.");
                    continue;
                }

                Board targetBoard = null;
                for (Board b : user.getBoardList()) {
                    if (b.getName().equals(originalBoardName)) {
                        targetBoard = b;
                        break;
                    }
                }

                if (targetBoard != null) {
                    targetBoard.addExistingTodo(sharedToDo);
                    System.out.println("DEBUG: UserDAOImpl ha caricato il ToDo CONDIVISO '" + sharedToDo.getTitle() + "' (Proprietario: " + sharedToDo.getOwner() + ") per la board del ricevente '" + targetBoard.getName().getDisplayName() + "' (ID: " + targetBoard.getId() + ", condiviso con '" + user.getUsername() + "').");
                } else {
                    System.out.println("WARN: Il ToDo condiviso '" + sharedToDo.getTitle() + "' (Proprietario: " + sharedToDo.getOwner() + ") non è stato assegnato a una board esistente con il nome '" + originalBoardNameStr + "' per il ricevente '" + user.getUsername() + "'.");
                }
            }
        }
    }

    /**
     * Recupera l'ID numerico di una board specifica per un utente.
     *
     * @param boardName il nome della board da cercare
     * @param username l'username del proprietario della board
     * @return l'ID della board se trovata, -1 altrimenti
     * @throws SQLException se si verifica un errore durante la query
     * @see BoardName
     */
    @Override
    public int getBoardId(BoardName boardName, String username) throws SQLException {
        String sql = "SELECT b.id FROM boards b JOIN users u ON b.user_id = u.id WHERE b.name = ? AND u.username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, boardName.getDisplayName());
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    /**
     * Salva un nuovo todo nel database associandolo a una board specifica.
     *
     * <p>Il metodo salva sia il todo principale che tutte le sue attività associate
     * in un'operazione atomica per mantenere la consistenza dei dati.</p>
     *
     * @param toDo il todo da salvare con tutte le sue proprietà
     * @param boardId l'ID della board a cui associare il todo
     * @throws SQLException se si verifica un errore durante l'operazione di salvataggio
     * @see ToDo
     */
    @Override
    public void saveToDo(ToDo toDo, int boardId) throws SQLException {

        String sql = "INSERT INTO todos (id, title, description, status, due_date, created_date, position, owner_username, board_id, url, image, color) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, toDo.getId());
            pstmt.setString(2, toDo.getTitle());
            pstmt.setString(3, toDo.getDescription());
            pstmt.setString(4, toDo.getStatus());
            pstmt.setDate(5, toDo.getDueDate() != null ? Date.valueOf(toDo.getDueDate()) : null);
            pstmt.setDate(6, Date.valueOf(toDo.getCreatedDate()));
            pstmt.setInt(7, toDo.getPosition());
            pstmt.setString(8, toDo.getOwner());
            pstmt.setInt(9, boardId);
            pstmt.setString(10, toDo.getUrl());
            pstmt.setString(11, toDo.getImage());
            pstmt.setString(12, toDo.getColor());
            pstmt.executeUpdate();

            saveActivities(toDo.getId().toString(), toDo.getActivityList());
        }
    }

    /**
     * Aggiorna un todo esistente nel database con nuovi valori per tutte le sue proprietà.
     *
     * <p>Il metodo sostituisce completamente le attività esistenti del todo
     * con quelle fornite nell'oggetto todo aggiornato.</p>
     *
     * @param toDo il todo con i valori aggiornati
     * @param boardId l'ID della board di appartenenza del todo
     * @throws SQLException se si verifica un errore durante l'aggiornamento
     * @see ToDo
     */
    @Override
    public void updateToDo(ToDo toDo, int boardId) throws SQLException {

        String sql = "UPDATE todos SET title = ?, description = ?, status = ?, due_date = ?, position = ?, owner_username = ?, url = ?, image = ?, color = ? WHERE id = ? AND board_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, toDo.getTitle());
            pstmt.setString(2, toDo.getDescription());
            pstmt.setString(3, toDo.getStatus());
            pstmt.setDate(4, toDo.getDueDate() != null ? Date.valueOf(toDo.getDueDate()) : null);
            pstmt.setInt(5, toDo.getPosition());
            pstmt.setString(6, toDo.getOwner());
            pstmt.setString(7, toDo.getUrl());
            pstmt.setString(8, toDo.getImage());
            pstmt.setString(9, toDo.getColor());
            pstmt.setObject(10, toDo.getId());
            pstmt.setInt(11, boardId);
            pstmt.executeUpdate();

            clearActivities(toDo.getId().toString());
            saveActivities(toDo.getId().toString(), toDo.getActivityList());
        }
    }

    /**
     * Sposta un todo da una board a un'altra aggiornando il suo board_id.
     *
     * @param toDoId l'ID stringa del todo da spostare
     * @param newBoardId l'ID della board di destinazione
     * @throws SQLException se si verifica un errore durante l'aggiornamento
     */
    @Override
    public void updateToDoBoardId(String toDoId, int newBoardId) throws SQLException {
        String sql = "UPDATE todos SET board_id = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newBoardId);
            pstmt.setObject(2, UUID.fromString(toDoId));
            pstmt.executeUpdate();
        }
    }

    /**
     * Salva tutte le attività associate a un todo nel database.
     *
     * <p>Utilizza batch processing per ottimizzare le performance quando
     * si salvano multiple attività contemporaneamente.</p>
     *
     * @param toDoId l'ID stringa del todo proprietario delle attività
     * @param activities mappa delle attività con titolo come chiave e stato completamento come valore
     * @throws SQLException se si verifica un errore durante il salvataggio delle attività
     */
    private void saveActivities(String toDoId, Map<String, Boolean> activities) throws SQLException {
        String sql = "INSERT INTO activities (todo_id, activity_title, completed) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Map.Entry<String, Boolean> entry : activities.entrySet()) {
                pstmt.setObject(1, UUID.fromString(toDoId));
                pstmt.setString(2, entry.getKey());
                pstmt.setBoolean(3, entry.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    /**
     * Rimuove tutte le attività associate a un todo dal database.
     *
     * @param toDoId l'ID stringa del todo di cui rimuovere le attività
     * @throws SQLException se si verifica un errore durante la rimozione
     */
    private void clearActivities(String toDoId) throws SQLException {
        String sql = "DELETE FROM activities WHERE todo_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(toDoId));
            pstmt.executeUpdate();
        }
    }

    /**
     * Elimina completamente un todo dal database, incluse tutte le sue dipendenze.
     *
     * <p>L'operazione rimuove in sequenza:</p>
     * <ol>
     *   <li>Tutte le attività del todo</li>
     *   <li>Tutte le condivisioni del todo</li>
     *   <li>Il todo stesso</li>
     * </ol>
     *
     * @param toDoId l'ID stringa del todo da eliminare
     * @param username l'username del proprietario del todo (per validazione)
     * @throws SQLException se si verifica un errore durante l'eliminazione
     */
    @Override
    public void deleteToDo(String toDoId, String username) throws SQLException {
        clearActivities(toDoId);
        removeAllToDoSharing(toDoId);

        String sql = "DELETE FROM todos WHERE id = ? AND owner_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(toDoId));
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    /**
     * Condivide un todo con un utente specifico creando un record di condivisione.
     *
     * @param toDoId l'ID stringa del todo da condividere
     * @param sharedWithUsername l'username dell'utente con cui condividere il todo
     * @throws SQLException se si verifica un errore durante la creazione della condivisione
     */
    @Override
    public void shareToDo(String toDoId, String sharedWithUsername) throws SQLException {
        String sql = "INSERT INTO shared_todos (todo_id, shared_with_username) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(toDoId));
            pstmt.setString(2, sharedWithUsername);
            pstmt.executeUpdate();
        }
    }

    /**
     * Rimuove la condivisione di un todo con un utente specifico.
     *
     * @param toDoId l'ID stringa del todo per cui rimuovere la condivisione
     * @param sharedWithUsername l'username dell'utente da cui rimuovere la condivisione
     * @throws SQLException se si verifica un errore durante la rimozione della condivisione
     */
    @Override
    public void removeToDoSharing(String toDoId, String sharedWithUsername) throws SQLException {
        String sql = "DELETE FROM shared_todos WHERE todo_id = ? AND shared_with_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(toDoId));
            pstmt.setString(2, sharedWithUsername);
            pstmt.executeUpdate();
        }
    }

    /**
     * Rimuove tutte le condivisioni di un todo specifico.
     *
     * <p>Utilizzato tipicamente prima dell'eliminazione di un todo per
     * mantenere l'integrità referenziale del database.</p>
     *
     * @param toDoId l'ID stringa del todo per cui rimuovere tutte le condivisioni
     * @throws SQLException se si verifica un errore durante la rimozione delle condivisioni
     */
    @Override
    public void removeAllToDoSharing(String toDoId) throws SQLException {
        String sql = "DELETE FROM shared_todos WHERE todo_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(toDoId));
            pstmt.executeUpdate();
        }
    }

    /**
     * Recupera tutti gli utenti registrati nel sistema.
     *
     * @return Set contenente tutti gli utenti senza duplicati
     * @throws SQLException se si verifica un errore durante il recupero degli utenti
     * @see User
     * @see Set
     */
    @Override
    public Set<User> getAllUsers() throws SQLException {
        Set<User> users = new HashSet<>();
        String sql = "SELECT id, username, password_hash FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        null,
                        (UUID) rs.getObject("id")
                ));
            }
        }
        return users;
    }

    /**
     * Recupera la lista di tutti gli username con cui è condiviso un todo specifico.
     *
     * @param toDoId l'ID stringa del todo per cui recuperare le condivisioni
     * @return Lista degli username che hanno accesso al todo condiviso
     * @throws SQLException se si verifica un errore durante il recupero delle condivisioni
     * @see List
     */
    @Override
    public List<String> getSharedUsernamesForToDo(String toDoId) throws SQLException {
        List<String> sharedUsernames = new ArrayList<>();
        String sql = "SELECT shared_with_username FROM shared_todos WHERE todo_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(toDoId));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sharedUsernames.add(rs.getString("shared_with_username"));
            }
        }
        return sharedUsernames;
    }
}