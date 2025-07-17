package dao;

import models.board.Board;
import models.ToDo;
import models.User;
import models.board.BoardName;
import db.DatabaseConnection;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class UserDAOImpl implements UserDAO {

    private Connection connection;

    public UserDAOImpl() throws SQLException {
        this.connection = DatabaseConnection.getInstance();
    }

    @Override
    public boolean saveUser(User user) throws SQLException {
        String sql = "INSERT INTO users (id, username, password_hash) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // FIX: Use setObject for UUID to let JDBC handle the type mapping
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

    @Override
    public void saveBoard(Board board, UUID userId) throws SQLException {
        // MODIFIED: Added 'color' to the INSERT statement
        String sql = "INSERT INTO boards (name, description, color, user_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, board.getName().getDisplayName());
            pstmt.setString(2, board.getDescription());
            pstmt.setString(3, board.getColor()); // Get the color from the board object
            // FIX: Use setObject for UUID userId
            pstmt.setObject(4, userId);
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

    @Override
    public void updateBoard(Board board) throws SQLException {
        // NEW METHOD: To update an existing board's properties, including color
        String sql = "UPDATE boards SET name = ?, description = ?, color = ? WHERE id = ? AND user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, board.getName().getDisplayName());
            pstmt.setString(2, board.getDescription());
            pstmt.setString(3, board.getColor()); // Set the updated color
            pstmt.setInt(4, board.getId());

            // Fetch the owner's UUID based on the username stored in the Board object
            Optional<User> ownerUser = getUserByUsername(board.getOwner());
            if (ownerUser.isPresent()) {
                pstmt.setObject(5, ownerUser.get().getId());
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
                        // FIX: Use getObject for UUID
                        (UUID) rs.getObject("id")
                );
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public void loadUserBoardsAndToDos(User user) throws SQLException {
        if (user == null) {
            return;
        }

        user.clearBoards();

        // MODIFIED: Added 'color' to the SELECT statement for boards
        String boardSql = "SELECT id, name, description, color FROM boards WHERE user_id = ?";
        try (PreparedStatement pstmtBoard = connection.prepareStatement(boardSql)) {
            // FIX: Use setObject for UUID user.getId()
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
                        user.getUsername(), // This is the owner of the board
                        rsBoards.getString("description"),
                        rsBoards.getString("color") // Retrieve the color from the result set
                );
                user.addBoard(board);
                System.out.println("DEBUG: UserDAOImpl loaded board '" + boardDisplayName + "' (ID: " + board.getId() + ") for user '" + user.getUsername() + "'.");

                // Carica i ToDo di proprietà di questa board
                String todoSql = "SELECT id, title, description, status, due_date, created_date, position, owner_username FROM todos WHERE board_id = ?";
                try (PreparedStatement pstmtTodo = connection.prepareStatement(todoSql)) {
                    pstmtTodo.setInt(1, board.getId());
                    ResultSet rsTodos = pstmtTodo.executeQuery();
                    while (rsTodos.next()) {
                        ToDo toDo = new ToDo(
                                // FIX: Use getObject for UUID
                                (UUID) rsTodos.getObject("id"),
                                rsTodos.getString("title"),
                                rsTodos.getString("owner_username")
                        );
                        toDo.setDescription(rsTodos.getString("description"));
                        toDo.setStatus(rsTodos.getString("status"));
                        toDo.setDueDate(rsTodos.getDate("due_date") != null ? rsTodos.getDate("due_date").toLocalDate() : null);
                        toDo.setCreatedDate(rsTodos.getDate("created_date").toLocalDate());
                        toDo.setPosition(rsTodos.getInt("position"));

                        // Carica le attività per questo ToDo
                        String activitySql = "SELECT activity_title, completed FROM activities WHERE todo_id = ?";
                        try (PreparedStatement pstmtActivity = connection.prepareStatement(activitySql)) {
                            // FIX: Use setObject for UUID toDo.getId()
                            pstmtActivity.setObject(1, toDo.getId());
                            ResultSet rsActivities = pstmtActivity.executeQuery();
                            Map<String, Boolean> activitiesMap = new HashMap<>();
                            while (rsActivities.next()) {
                                activitiesMap.put(rsActivities.getString("activity_title"), rsActivities.getBoolean("completed"));
                            }
                            toDo.setActivityList(activitiesMap);
                        }

                        // Se l'utente corrente è il proprietario del ToDo, carica anche gli utenti con cui è stato condiviso
                        if (toDo.getOwner().equals(user.getUsername())) {
                            List<String> sharedUsernames = getSharedUsernamesForToDo(toDo.getId().toString());
                            for (String sharedUsername : sharedUsernames) {
                                Optional<User> sharedUser = getUserByUsername(sharedUsername);
                                sharedUser.ifPresent(toDo::addSharedUser);
                            }
                        }
                        board.addExistingTodo(toDo);
                        System.out.println("DEBUG: UserDAOImpl loaded ToDo '" + toDo.getTitle() + "' for board '" + boardDisplayName + "'.");
                    }
                }

                // --- INIZIO LOGICA PER TODO CONDIVISI ---
                // Carica i ToDo che sono stati condivisi CON l'utente corrente
                String sharedTodoSql = "SELECT t.id, t.title, t.description, t.status, t.due_date, t.created_date, t.position, t.owner_username " +
                        "FROM todos t " +
                        "JOIN shared_todos st ON t.id = st.todo_id " +
                        "WHERE st.shared_with_username = ? AND t.board_id = ?";
                try (PreparedStatement pstmtSharedTodo = connection.prepareStatement(sharedTodoSql)) {
                    pstmtSharedTodo.setString(1, user.getUsername());
                    pstmtSharedTodo.setInt(2, board.getId());
                    ResultSet rsSharedTodos = pstmtSharedTodo.executeQuery();
                    while (rsSharedTodos.next()) {
                        ToDo sharedToDo = new ToDo(
                                (UUID) rsSharedTodos.getObject("id"),
                                rsSharedTodos.getString("title"),
                                rsSharedTodos.getString("owner_username")
                        );
                        sharedToDo.setDescription(rsSharedTodos.getString("description"));
                        sharedToDo.setStatus(rsSharedTodos.getString("status"));
                        sharedToDo.setDueDate(rsSharedTodos.getDate("due_date") != null ? rsSharedTodos.getDate("due_date").toLocalDate() : null);
                        sharedToDo.setCreatedDate(rsSharedTodos.getDate("created_date").toLocalDate());
                        sharedToDo.setPosition(rsSharedTodos.getInt("position"));

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

                        Optional<User> currentUserOptional = getUserByUsername(user.getUsername());
                        currentUserOptional.ifPresent(sharedToDo::addSharedUser);

                        board.addExistingTodo(sharedToDo);
                        System.out.println("DEBUG: UserDAOImpl loaded SHARED ToDo '" + sharedToDo.getTitle() + "' (Owner: " + sharedToDo.getOwner() + ") for board '" + boardDisplayName + "' (shared with '" + user.getUsername() + "').");
                    }
                }
                // --- FINE LOGICA PER TODO CONDIVISI ---
            }
        }
    }

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

    @Override
    public void saveToDo(ToDo toDo, int boardId) throws SQLException {
        String sql = "INSERT INTO todos (id, title, description, status, due_date, created_date, position, owner_username, board_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // FIX: Use setObject for UUID toDo.getId()
            pstmt.setObject(1, toDo.getId());
            pstmt.setString(2, toDo.getTitle());
            pstmt.setString(3, toDo.getDescription());
            pstmt.setString(4, toDo.getStatus());
            pstmt.setDate(5, toDo.getDueDate() != null ? Date.valueOf(toDo.getDueDate()) : null);
            pstmt.setDate(6, Date.valueOf(toDo.getCreatedDate()));
            pstmt.setInt(7, toDo.getPosition());
            pstmt.setString(8, toDo.getOwner());
            pstmt.setInt(9, boardId);
            pstmt.executeUpdate();

            saveActivities(toDo.getId().toString(), toDo.getActivityList());
        }
    }

    @Override
    public void updateToDo(ToDo toDo, int boardId) throws SQLException {
        String sql = "UPDATE todos SET title = ?, description = ?, status = ?, due_date = ?, position = ?, owner_username = ? WHERE id = ? AND board_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, toDo.getTitle());
            pstmt.setString(2, toDo.getDescription());
            pstmt.setString(3, toDo.getStatus());
            pstmt.setDate(4, toDo.getDueDate() != null ? Date.valueOf(toDo.getDueDate()) : null);
            pstmt.setInt(5, toDo.getPosition());
            pstmt.setString(6, toDo.getOwner());
            // FIX: Use setObject for UUID toDo.getId()
            pstmt.setObject(7, toDo.getId());
            pstmt.setInt(8, boardId);
            pstmt.executeUpdate();

            clearActivities(toDo.getId().toString());
            saveActivities(toDo.getId().toString(), toDo.getActivityList());
        }
    }

    @Override
    public void updateToDoBoardId(String toDoId, int newBoardId) throws SQLException {
        String sql = "UPDATE todos SET board_id = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newBoardId);
            // FIX: Use setObject for UUID.fromString(toDoId)
            pstmt.setObject(2, UUID.fromString(toDoId));
            pstmt.executeUpdate();
        }
    }

    private void saveActivities(String toDoId, Map<String, Boolean> activities) throws SQLException {
        String sql = "INSERT INTO activities (todo_id, activity_title, completed) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Map.Entry<String, Boolean> entry : activities.entrySet()) {
                // FIX: Use setObject for UUID.fromString(toDoId)
                pstmt.setObject(1, UUID.fromString(toDoId));
                pstmt.setString(2, entry.getKey());
                pstmt.setBoolean(3, entry.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void clearActivities(String toDoId) throws SQLException {
        String sql = "DELETE FROM activities WHERE todo_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // FIX: Use setObject for UUID.fromString(toDoId)
            pstmt.setObject(1, UUID.fromString(toDoId));
            pstmt.executeUpdate();
        }
    }

    @Override
    public void deleteToDo(String toDoId, String username) throws SQLException {
        // Prima di eliminare il ToDo, elimina anche le sue attività e le sue condivisioni
        clearActivities(toDoId);
        removeAllToDoSharing(toDoId); // Aggiunto per pulire le condivisioni quando il ToDo viene eliminato

        String sql = "DELETE FROM todos WHERE id = ? AND owner_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // FIX: Use setObject for UUID.fromString(toDoId)
            pstmt.setObject(1, UUID.fromString(toDoId));
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void shareToDo(String toDoId, String sharedWithUsername) throws SQLException {
        String sql = "INSERT INTO shared_todos (todo_id, shared_with_username) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // FIX: Use setObject for UUID.fromString(toDoId)
            pstmt.setObject(1, UUID.fromString(toDoId));
            pstmt.setString(2, sharedWithUsername);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void removeToDoSharing(String toDoId, String sharedWithUsername) throws SQLException {
        String sql = "DELETE FROM shared_todos WHERE todo_id = ? AND shared_with_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // FIX: Use setObject for UUID.fromString(toDoId)
            pstmt.setObject(1, UUID.fromString(toDoId));
            pstmt.setString(2, sharedWithUsername);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void removeAllToDoSharing(String toDoId) throws SQLException {
        String sql = "DELETE FROM shared_todos WHERE todo_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // FIX: Use setObject for UUID.fromString(toDoId)
            pstmt.setObject(1, UUID.fromString(toDoId));
            pstmt.executeUpdate();
        }
    }

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
                        // FIX: Use getObject for UUID
                        (UUID) rs.getObject("id")
                ));
            }
        }
        return users;
    }

    @Override
    public List<String> getSharedUsernamesForToDo(String toDoId) throws SQLException {
        List<String> sharedUsernames = new ArrayList<>();
        String sql = "SELECT shared_with_username FROM shared_todos WHERE todo_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // FIX: Use setObject for UUID.fromString(toDoId)
            pstmt.setObject(1, UUID.fromString(toDoId));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sharedUsernames.add(rs.getString("shared_with_username"));
            }
        }
        return sharedUsernames;
    }
}