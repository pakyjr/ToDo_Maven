package dao;

import models.ToDo;
import models.User;
import models.board.Board;
import models.board.BoardName;
import db.DatabaseConnection; // Corrected import to your provided class

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserDAOImpl implements UserDAO {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public boolean saveUser(User user) throws SQLException {
        String insertUserSQL = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        String insertBoardSQL = "INSERT INTO boards (name, owner_username, user_id, description) VALUES (?, ?, ?, ?)"; // Added description
        Connection conn = null;
        PreparedStatement pstmtUser = null;
        PreparedStatement pstmtBoard = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance(); // Use your DatabaseConnection
            conn.setAutoCommit(false); // Start transaction

            // Check if user already exists
            if (getUserByUsername(user.getUsername()).isPresent()) {
                System.err.println("User '" + user.getUsername() + "' already exists in database. Cannot register.");
                conn.rollback(); // Rollback any potential partial changes
                return false;
            }

            // Insert user
            pstmtUser = conn.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS);
            pstmtUser.setString(1, user.getUsername());
            pstmtUser.setString(2, user.getHashedPassword());
            int affectedRows = pstmtUser.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            int userId;
            rs = pstmtUser.getGeneratedKeys();
            if (rs.next()) {
                userId = rs.getInt(1);
            } else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }

            // Insert default boards for the new user
            pstmtBoard = conn.prepareStatement(insertBoardSQL);
            for (Board board : user.getBoardList()) { // Use the boards already created in User constructor
                pstmtBoard.setString(1, board.getName().getDisplayName());
                pstmtBoard.setString(2, user.getUsername()); // Board owner is the user
                pstmtBoard.setInt(3, userId);
                pstmtBoard.setString(4, board.getDescription()); // Assuming Board has a getDescription method
                pstmtBoard.addBatch();
            }
            pstmtBoard.executeBatch();
            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            System.err.println("SQL Exception in saveUser: " + e.getMessage());
            if (conn != null) conn.rollback(); // Rollback on error
            throw e; // Re-throw to be handled by controller
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmtUser != null) pstmtUser.close();
                if (pstmtBoard != null) pstmtBoard.close();
                // Do NOT close conn here if DatabaseConnection.getInstance() manages a single persistent connection.
                // If it creates a new connection each time, then closing is fine.
                // Given your getInstance() creates a new one if null/closed, then it's effectively a "new" connection on first call.
                // For a singleton connection, you'd generally not close it in DAO methods, but let the app manage its lifecycle.
                // For now, let's keep it as is, assuming getInstance() handles connections correctly (e.g., pooling or strict singleton).
                if (conn != null) conn.close(); // Keep this for safety if it's not a true singleton connection pool
            } catch (SQLException e) {
                System.err.println("Error closing resources in saveUser: " + e.getMessage());
            }
        }
    }

    @Override
    public Optional<User> getUserByUsername(String username) throws SQLException {
        String sql = "SELECT username, password_hash FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance(); // Use your DatabaseConnection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedUsername = rs.getString("username");
                    String passwordHash = rs.getString("password_hash");
                    return Optional.of(new User(storedUsername, passwordHash, null));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception in getUserByUsername: " + e.getMessage());
            throw e;
        }
        return Optional.empty();
    }

    @Override
    public ArrayList<User> getAllUsers() throws SQLException {
        ArrayList<User> users = new ArrayList<>();
        String sql = "SELECT username, password_hash FROM users";
        try (Connection conn = DatabaseConnection.getInstance(); // Use your DatabaseConnection
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String username = rs.getString("username");
                String passwordHash = rs.getString("password_hash");
                users.add(new User(username, passwordHash, null)); // Boards loaded separately
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception in getAllUsers: " + e.getMessage());
            throw e;
        }
        return users;
    }

    @Override
    public int getBoardId(BoardName boardName, String username) throws SQLException {
        String sql = "SELECT id FROM boards WHERE name = ? AND owner_username = ?";
        try (Connection conn = DatabaseConnection.getInstance(); // Use your DatabaseConnection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, boardName.getDisplayName());
            pstmt.setString(2, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception in getBoardId: " + e.getMessage());
            throw e;
        }
        return -1; // Board not found
    }

    @Override
    public void saveToDo(ToDo toDo, int boardId) throws SQLException {
        String sql = "INSERT INTO todos (id, title, description, due_date, url, image, color, status, owner_username, board_id, position) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertActivitySQL = "INSERT INTO todo_activities (todo_id, activity_title, completed) VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmtToDo = null;
        PreparedStatement pstmtActivity = null;

        try {
            conn = DatabaseConnection.getInstance(); // Use your DatabaseConnection
            conn.setAutoCommit(false); // Start transaction

            pstmtToDo = conn.prepareStatement(sql);
            pstmtToDo.setString(1, toDo.getId().toString()); // Use UUID as string
            pstmtToDo.setString(2, toDo.getTitle());
            pstmtToDo.setString(3, toDo.getDescription());
            pstmtToDo.setDate(4, toDo.getDueDate() != null ? Date.valueOf(toDo.getDueDate()) : null); // Convert LocalDate to java.sql.Date
            pstmtToDo.setString(5, toDo.getUrl());
            pstmtToDo.setString(6, toDo.getImage());
            pstmtToDo.setString(7, toDo.getColor());
            pstmtToDo.setString(8, toDo.getStatus());
            pstmtToDo.setString(9, toDo.getOwner()); // Save the ToDo's owner (creator)
            pstmtToDo.setInt(10, boardId);
            pstmtToDo.setInt(11, toDo.getPosition());
            pstmtToDo.executeUpdate();

            // Save activities
            if (!toDo.getActivityList().isEmpty()) {
                pstmtActivity = conn.prepareStatement(insertActivitySQL);
                for (Map.Entry<String, Boolean> entry : toDo.getActivityList().entrySet()) {
                    pstmtActivity.setString(1, toDo.getId().toString());
                    pstmtActivity.setString(2, entry.getKey());
                    pstmtActivity.setBoolean(3, entry.getValue());
                    pstmtActivity.addBatch();
                }
                pstmtActivity.executeBatch();
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.err.println("SQL Exception in saveToDo: " + e.getMessage());
            if (conn != null) conn.rollback(); // Rollback on error
            throw e;
        } finally {
            try {
                if (pstmtToDo != null) pstmtToDo.close();
                if (pstmtActivity != null) pstmtActivity.close();
                if (conn != null) conn.close(); // Keep this for safety
            } catch (SQLException e) {
                System.err.println("Error closing resources in saveToDo: " + e.getMessage());
            }
        }
    }

    @Override
    public void updateToDo(ToDo toDo, int boardId) throws SQLException {
        String updateToDoSQL = "UPDATE todos SET title = ?, description = ?, due_date = ?, url = ?, image = ?, color = ?, status = ?, position = ? WHERE id = ?";
        String deleteActivitiesSQL = "DELETE FROM todo_activities WHERE todo_id = ?";
        String insertActivitySQL = "INSERT INTO todo_activities (todo_id, activity_title, completed) VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmtToDo = null;
        PreparedStatement pstmtDeleteActivities = null;
        PreparedStatement pstmtInsertActivity = null;

        try {
            conn = DatabaseConnection.getInstance(); // Use your DatabaseConnection
            conn.setAutoCommit(false); // Start transaction

            pstmtToDo = conn.prepareStatement(updateToDoSQL);
            pstmtToDo.setString(1, toDo.getTitle());
            pstmtToDo.setString(2, toDo.getDescription());
            pstmtToDo.setDate(3, toDo.getDueDate() != null ? Date.valueOf(toDo.getDueDate()) : null);
            pstmtToDo.setString(4, toDo.getUrl());
            pstmtToDo.setString(5, toDo.getImage());
            pstmtToDo.setString(6, toDo.getColor());
            pstmtToDo.setString(7, toDo.getStatus());
            pstmtToDo.setInt(8, toDo.getPosition());
            pstmtToDo.setString(9, toDo.getId().toString()); // Identify by UUID
            pstmtToDo.executeUpdate();

            // Delete existing activities for this ToDo
            pstmtDeleteActivities = conn.prepareStatement(deleteActivitiesSQL);
            pstmtDeleteActivities.setString(1, toDo.getId().toString());
            pstmtDeleteActivities.executeUpdate();

            // Insert updated activities
            if (!toDo.getActivityList().isEmpty()) {
                pstmtInsertActivity = conn.prepareStatement(insertActivitySQL);
                for (Map.Entry<String, Boolean> entry : toDo.getActivityList().entrySet()) {
                    pstmtInsertActivity.setString(1, toDo.getId().toString());
                    pstmtInsertActivity.setString(2, entry.getKey());
                    pstmtInsertActivity.setBoolean(3, entry.getValue());
                    pstmtInsertActivity.addBatch();
                }
                pstmtInsertActivity.executeBatch();
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.err.println("SQL Exception in updateToDo: " + e.getMessage());
            if (conn != null) conn.rollback(); // Rollback on error
            throw e;
        } finally {
            try {
                if (pstmtToDo != null) pstmtToDo.close();
                if (pstmtDeleteActivities != null) pstmtDeleteActivities.close();
                if (pstmtInsertActivity != null) pstmtInsertActivity.close();
                if (conn != null) conn.close(); // Keep this for safety
            } catch (SQLException e) {
                System.err.println("Error closing resources in updateToDo: " + e.getMessage());
            }
        }
    }

    @Override
    public void deleteToDo(UUID toDoId) throws SQLException {
        String deleteToDoSQL = "DELETE FROM todos WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmtDeleteToDo = null;

        try {
            conn = DatabaseConnection.getInstance(); // Use your DatabaseConnection
            conn.setAutoCommit(false); // Start transaction

            pstmtDeleteToDo = conn.prepareStatement(deleteToDoSQL);
            pstmtDeleteToDo.setString(1, toDoId.toString());
            int affectedRows = pstmtDeleteToDo.executeUpdate();

            if (affectedRows == 0) {
                System.out.println("No ToDo found with ID " + toDoId + " for deletion.");
            } else {
                System.out.println("ToDo with ID " + toDoId + " successfully deleted from database.");
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.err.println("SQL Exception in deleteToDo: " + e.getMessage());
            if (conn != null) conn.rollback(); // Rollback on error
            throw e;
        } finally {
            try {
                if (pstmtDeleteToDo != null) pstmtDeleteToDo.close();
                if (conn != null) conn.close(); // Keep this for safety
            } catch (SQLException e) {
                System.err.println("Error closing resources in deleteToDo: " + e.getMessage());
            }
        }
    }

    @Override
    public void updateToDoBoardId(UUID toDoId, int newBoardId) throws SQLException {
        String sql = "UPDATE todos SET board_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance(); // Use your DatabaseConnection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newBoardId);
            pstmt.setString(2, toDoId.toString());
            pstmt.executeUpdate();
            System.out.println("ToDo ID " + toDoId + " moved to board ID " + newBoardId + " in database.");
        } catch (SQLException e) {
            System.err.println("SQL Exception in updateToDoBoardId: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void loadUserBoardsAndToDos(User user) throws SQLException {
        // First, clear existing boards and todos in the user object to avoid duplicates on reload
        user.getBoardList().clear();

        String selectBoardsSQL = "SELECT id, name, description, owner_username FROM boards WHERE owner_username = ?";
        String selectToDosSQL = "SELECT id, title, description, due_date, url, image, color, status, owner_username, position FROM todos WHERE board_id = ? ORDER BY position ASC";
        String selectActivitiesSQL = "SELECT activity_title, completed FROM todo_activities WHERE todo_id = ?";

        Connection conn = null;
        PreparedStatement pstmtBoards = null;
        PreparedStatement pstmtToDos = null;
        PreparedStatement pstmtActivities = null;
        ResultSet rsBoards = null;
        ResultSet rsToDos = null;
        ResultSet rsActivities = null;

        try {
            conn = DatabaseConnection.getInstance(); // Use your DatabaseConnection

            // Load Boards
            pstmtBoards = conn.prepareStatement(selectBoardsSQL);
            pstmtBoards.setString(1, user.getUsername());
            rsBoards = pstmtBoards.executeQuery();

            while (rsBoards.next()) {
                int boardId = rsBoards.getInt("id");
                BoardName boardNameEnum = BoardName.fromDisplayName(rsBoards.getString("name"));
                String description = rsBoards.getString("description");
                String ownerUsername = rsBoards.getString("owner_username");

                Board board = new Board(boardNameEnum, ownerUsername, description);
                user.addBoard(boardNameEnum, ownerUsername);

                Board actualBoard = user.getBoard(boardNameEnum);
                if (actualBoard == null) {
                    System.err.println("Error: Board " + boardNameEnum.getDisplayName() + " was not added to user's list, or not found after adding.");
                    continue;
                }

                // Load ToDos for this board
                pstmtToDos = conn.prepareStatement(selectToDosSQL);
                pstmtToDos.setInt(1, boardId);
                rsToDos = pstmtToDos.executeQuery();

                while (rsToDos.next()) {
                    UUID toDoId = UUID.fromString(rsToDos.getString("id"));
                    String title = rsToDos.getString("title");
                    String desc = rsToDos.getString("description");
                    Date sqlDueDate = rsToDos.getDate("due_date");
                    String url = rsToDos.getString("url");
                    String image = rsToDos.getString("image");
                    String color = rsToDos.getString("color");
                    String status = rsToDos.getString("status");
                    String toDoOwner = rsToDos.getString("owner_username");
                    int position = rsToDos.getInt("position");

                    ToDo toDo = new ToDo(title);
                    toDo.setId(toDoId);
                    toDo.setDescription(desc);
                    toDo.setUrl(url);
                    toDo.setImage(image);
                    toDo.setColor(color);
                    toDo.setStatus(status);
                    toDo.setOwner(toDoOwner);
                    toDo.setPosition(position);

                    if (sqlDueDate != null) {
                        toDo.setDueDate(sqlDueDate.toLocalDate());
                    }

                    // Load Activities for this ToDo
                    Map<String, Boolean> activities = new HashMap<>();
                    pstmtActivities = conn.prepareStatement(selectActivitiesSQL);
                    pstmtActivities.setString(1, toDoId.toString());
                    rsActivities = pstmtActivities.executeQuery();
                    while (rsActivities.next()) {
                        activities.put(rsActivities.getString("activity_title"), rsActivities.getBoolean("completed"));
                    }
                    toDo.setActivityList(activities);

                    actualBoard.addExistingTodo(toDo);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception in loadUserBoardsAndToDos: " + e.getMessage());
            throw e;
        } finally {
            try {
                if (rsActivities != null) rsActivities.close();
                if (pstmtActivities != null) pstmtActivities.close();
                if (rsToDos != null) rsToDos.close();
                if (pstmtToDos != null) pstmtToDos.close();
                if (rsBoards != null) rsBoards.close();
                if (pstmtBoards != null) pstmtBoards.close();
                if (conn != null) conn.close(); // Keep this for safety
            } catch (SQLException e) {
                System.err.println("Error closing resources in loadUserBoardsAndToDos: " + e.getMessage());
            }
        }
    }
}