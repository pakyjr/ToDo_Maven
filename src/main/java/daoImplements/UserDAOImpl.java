package dao;

import models.board.Board;
import models.ToDo;
import models.User;
import models.board.BoardName;
import db.DatabaseConnection;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UserDAOImpl implements UserDAO {

    private Connection connection;

    public UserDAOImpl() throws SQLException {
        this.connection = DatabaseConnection.getInstance();
    }

    @Override
    public boolean saveUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getHashedPassword());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        System.out.println("DEBUG: User '" + user.getUsername() + "' saved to DB with ID: " + user.getId());
                    }
                }
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
    public void saveBoard(Board board, int userId) throws SQLException {
        String sql = "INSERT INTO boards (name, description, color, user_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, board.getName().getDisplayName());
            pstmt.setString(2, board.getDescription());
            pstmt.setString(3, board.getColor());
            pstmt.setInt(4, userId);
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
    public Optional<User> getUserByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(rs.getString("username"), rs.getString("password_hash"));
                user.setId(rs.getInt("id"));
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

        String boardSql = "SELECT id, name, description, color FROM boards WHERE user_id = ?";
        try (PreparedStatement pstmtBoard = connection.prepareStatement(boardSql)) {
            pstmtBoard.setInt(1, user.getId());
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
                        rsBoards.getString("description"),
                        rsBoards.getString("color")
                );
                user.addBoard(board);
                System.out.println("DEBUG: UserDAOImpl loaded board '" + boardDisplayName + "' (ID: " + board.getId() + ") for user '" + user.getUsername() + "'.");


                String todoSql = "SELECT id, title, description, status, due_date, created_date, position, owner_username FROM todos WHERE board_id = ?";
                try (PreparedStatement pstmtTodo = connection.prepareStatement(todoSql)) {
                    pstmtTodo.setInt(1, board.getId());
                    ResultSet rsTodos = pstmtTodo.executeQuery();
                    while (rsTodos.next()) {
                        // When retrieving from DB, convert UUID string back to UUID object
                        ToDo toDo = new ToDo(
                                UUID.fromString(rsTodos.getString("id")),
                                rsTodos.getString("title"),
                                rsTodos.getString("owner_username")
                        );
                        toDo.setDescription(rsTodos.getString("description"));
                        toDo.setStatus(rsTodos.getString("status"));
                        toDo.setDueDate(rsTodos.getDate("due_date") != null ? rsTodos.getDate("due_date").toLocalDate() : null);
                        toDo.setCreatedDate(rsTodos.getDate("created_date").toLocalDate());
                        toDo.setPosition(rsTodos.getInt("position"));

                        String activitySql = "SELECT activity_title, completed FROM activities WHERE todo_id = ?";
                        try (PreparedStatement pstmtActivity = connection.prepareStatement(activitySql)) {
                            // When querying activities by todo_id, ensure UUID conversion
                            pstmtActivity.setObject(1, toDo.getId()); // Pass UUID object
                            ResultSet rsActivities = pstmtActivity.executeQuery();
                            Map<String, Boolean> activitiesMap = new HashMap<>();
                            while (rsActivities.next()) {
                                activitiesMap.put(rsActivities.getString("activity_title"), rsActivities.getBoolean("completed"));
                            }
                            toDo.setActivityList(activitiesMap);
                        }

                        if (toDo.getOwner().equals(user.getUsername())) {
                            List<String> sharedUsernames = getSharedUsernamesForToDo(toDo.getId().toString()); // getSharedUsernamesForToDo takes String
                            for (String sharedUsername : sharedUsernames) {
                                Optional<User> sharedUser = getUserByUsername(sharedUsername);
                                sharedUser.ifPresent(toDo::addSharedUser);
                            }
                        }
                        board.addExistingTodo(toDo);
                        System.out.println("DEBUG: UserDAOImpl loaded ToDo '" + toDo.getTitle() + "' for board '" + boardDisplayName + "'.");
                    }
                }
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
            pstmt.setObject(1, toDo.getId()); // Correct: Pass UUID object for UUID column
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
            pstmt.setObject(7, toDo.getId()); // Correct: Pass UUID object for UUID column in WHERE clause
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
            pstmt.setObject(2, UUID.fromString(toDoId)); // Correct: Convert string to UUID object
            pstmt.executeUpdate();
        }
    }

    private void saveActivities(String toDoId, Map<String, Boolean> activities) throws SQLException {
        String sql = "INSERT INTO activities (todo_id, activity_title, completed) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Map.Entry<String, Boolean> entry : activities.entrySet()) {
                pstmt.setObject(1, UUID.fromString(toDoId)); // Correct: Convert string to UUID object
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
            pstmt.setObject(1, UUID.fromString(toDoId)); // Correct: Convert string to UUID object
            pstmt.executeUpdate();
        }
    }

    @Override
    public void deleteToDo(String toDoId, String username) throws SQLException {
        clearActivities(toDoId); // Call to clearActivities already handles UUID conversion
        removeAllToDoSharing(toDoId); // Call to removeAllToDoSharing already handles UUID conversion

        String sql = "DELETE FROM todos WHERE id = ? AND owner_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(toDoId)); // Correct: Convert string to UUID object
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void shareToDo(String toDoId, String sharedWithUsername) throws SQLException {
        String sql = "INSERT INTO shared_todos (todo_id, shared_with_username) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(toDoId)); // Correct: Convert string to UUID object
            pstmt.setString(2, sharedWithUsername);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void removeToDoSharing(String toDoId, String sharedWithUsername) throws SQLException {
        String sql = "DELETE FROM shared_todos WHERE todo_id = ? AND shared_with_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(toDoId)); // Correct: Convert string to UUID object
            pstmt.setString(2, sharedWithUsername);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void removeAllToDoSharing(String toDoId) throws SQLException {
        String sql = "DELETE FROM shared_todos WHERE todo_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(toDoId)); // Correct: Convert string to UUID object
            pstmt.executeUpdate();
        }
    }

    @Override
    public Set<User> getAllUsers() throws SQLException {
        Set<User> users = new HashSet<>();
        String sql = "SELECT username FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Optional<User> userOptional = getUserByUsername(rs.getString("username"));
                userOptional.ifPresent(users::add);
            }
        }
        return users;
    }

    @Override
    public List<String> getSharedUsernamesForToDo(String toDoId) throws SQLException {
        List<String> sharedUsernames = new ArrayList<>();
        String sql = "SELECT shared_with_username FROM shared_todos WHERE todo_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(toDoId)); // Correct: Convert string to UUID object
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sharedUsernames.add(rs.getString("shared_with_username"));
            }
        }
        return sharedUsernames;
    }
}