package dao;

import db.DatabaseConnection;
import models.User;
import models.ToDo;
import models.board.Board;
import models.board.BoardName;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class UserDAOImpl implements UserDAO {

    @Override
    public boolean saveUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, hashed_password) VALUES (?, ?) ON CONFLICT (username) DO NOTHING;";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getHashedPassword());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                for (Board board : user.getBoardList()) {
                    saveBoard(board, user.getUsername());
                }
            }
            return affectedRows > 0;
        }
    }

    @Override
    public Optional<User> getUserByUsername(String username) throws SQLException {
        String sql = "SELECT username, hashed_password FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHashedPassword = rs.getString("hashed_password");
                    // Load boards for this user
                    List<Board> boardsLoaded = loadBoardsForUser(username);
                    ArrayList<Board> userBoards = new ArrayList<>(boardsLoaded);

                    User user = new User(username, storedHashedPassword, userBoards);


                    for (Board board : userBoards) {
                        int boardId = getBoardId(board.getName(), user.getUsername());
                        if (boardId != -1) {
                            List<ToDo> todos = loadToDosForBoard(boardId);
                            for (ToDo todo : todos) {
                                board.addExistingTodo(todo);
                            }
                        }
                    }
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteUser(String username) throws SQLException {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public List<Board> loadBoardsForUser(String username) throws SQLException {
        List<Board> boards = new ArrayList<>();
        String sql = "SELECT board_id, board_name FROM boards WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {

                    String dbBoardName = rs.getString("board_name");
                    BoardName boardName = null;
                    try {
                        boardName = BoardName.fromDisplayName(dbBoardName);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Warning: Unknown board name '" + dbBoardName + "' found in DB for user '" + username + "'. Skipping board. Error: " + e.getMessage());
                        continue; // Skip this board if its name is not recognized
                    }
                    Board board = new Board(boardName, username);
                    boards.add(board);
                }
            }
        }
        return boards;
    }

    @Override
    public boolean saveBoard(Board board, String username) throws SQLException {
        String sql = "INSERT INTO boards (board_name, username) VALUES (?, ?) ON CONFLICT (board_name, username) DO NOTHING;";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Save the displayName to the database
            pstmt.setString(1, board.getName().getDisplayName()); // <-- Use getDisplayName()
            pstmt.setString(2, username);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }


    @Override
    public boolean saveToDo(ToDo toDo, int boardId) throws SQLException {
        String sql = "INSERT INTO todos (todo_id, title, description, due_date, url, image, color, done, owner, status, board_id, position) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, toDo.getId());
            pstmt.setString(2, toDo.getTitle());
            pstmt.setString(3, toDo.getDescription());
            pstmt.setDate(4, toDo.getDueDate() != null ? Date.valueOf(toDo.getDueDate()) : null);
            pstmt.setString(5, toDo.getUrl());
            pstmt.setString(6, toDo.getImage());
            pstmt.setString(7, toDo.getColor());
            pstmt.setBoolean(8, toDo.getDone());
            pstmt.setString(9, toDo.getOwner());
            pstmt.setString(10, toDo.getStatus());
            pstmt.setInt(11, boardId);
            pstmt.setInt(12, toDo.getPosition());

            int affectedRows = pstmt.executeUpdate();


            if (affectedRows > 0 && !toDo.getActivityList().isEmpty()) {
                saveToDoActivities(toDo.getId(), toDo.getActivityList());
            }


            if (affectedRows > 0 && !toDo.getUsers().isEmpty()) {
                saveToDoSharedUsers(toDo.getId(), toDo.getUsers());
            }

            return affectedRows > 0;
        }
    }

    // Rimosso @Override
    private void saveToDoActivities(UUID todoId, Map<String, Boolean> activityList) throws SQLException {
        String sql = "INSERT INTO todo_activities (todo_id, activity_title, completed) VALUES (?, ?, ?);";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, Boolean> entry : activityList.entrySet()) {
                pstmt.setObject(1, todoId);
                pstmt.setString(2, entry.getKey());
                pstmt.setBoolean(3, entry.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    // Rimosso @Override
    private void saveToDoSharedUsers(UUID todoId, Set<User> users) throws SQLException {
        String sql = "INSERT INTO todo_shared_users (todo_id, username) VALUES (?, ?);";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (User user : users) {
                pstmt.setObject(1, todoId);
                pstmt.setString(2, user.getUsername());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    @Override
    public boolean updateToDo(ToDo toDo, int boardId) throws SQLException {
        String sql = "UPDATE todos SET title = ?, description = ?, due_date = ?, url = ?, image = ?, color = ?, done = ?, owner = ?, status = ?, position = ? WHERE todo_id = ? AND board_id = ?;";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, toDo.getTitle());
            pstmt.setString(2, toDo.getDescription());
            pstmt.setDate(3, toDo.getDueDate() != null ? Date.valueOf(toDo.getDueDate()) : null);
            pstmt.setString(4, toDo.getUrl());
            pstmt.setString(5, toDo.getImage());
            pstmt.setString(6, toDo.getColor());
            pstmt.setBoolean(7, toDo.getDone());
            pstmt.setString(8, toDo.getOwner());
            pstmt.setString(9, toDo.getStatus());
            pstmt.setInt(10, toDo.getPosition());
            pstmt.setObject(11, toDo.getId());
            pstmt.setInt(12, boardId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {

                String deleteActivitiesSql = "DELETE FROM todo_activities WHERE todo_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteActivitiesSql)) {
                    ps.setObject(1, toDo.getId());
                    ps.executeUpdate();
                }
                if (!toDo.getActivityList().isEmpty()) {
                    saveToDoActivities(toDo.getId(), toDo.getActivityList());
                }


                String deleteSharedUsersSql = "DELETE FROM todo_shared_users WHERE todo_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteSharedUsersSql)) {
                    ps.setObject(1, toDo.getId());
                    ps.executeUpdate();
                }
                if (!toDo.getUsers().isEmpty()) {
                    saveToDoSharedUsers(toDo.getId(), toDo.getUsers());
                }
            }

            return affectedRows > 0;
        }
    }

    @Override
    public boolean deleteToDo(UUID todoId) throws SQLException {
        String sql = "DELETE FROM todos WHERE todo_id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, todoId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public List<ToDo> loadToDosForBoard(int boardId) throws SQLException {
        List<ToDo> toDos = new ArrayList<>();
        String sql = "SELECT todo_id, title, description, due_date, url, image, color, done, owner, status, position FROM todos WHERE board_id = ? ORDER BY position;";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, boardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ToDo toDo = new ToDo(rs.getString("title"));
                    toDo.setId(UUID.fromString(rs.getString("todo_id")));
                    toDo.setDescription(rs.getString("description"));
                    Date dueDateSql = rs.getDate("due_date");
                    toDo.setDueDate(dueDateSql != null ? dueDateSql.toLocalDate() : null);
                    toDo.setUrl(rs.getString("url"));
                    toDo.setImage(rs.getString("image"));
                    toDo.setColor(rs.getString("color"));
                    toDo.setStatus(rs.getString("status"));
                    toDo.setDone(rs.getBoolean("done"));
                    toDo.setOwner(rs.getString("owner"));
                    toDo.setPosition(rs.getInt("position"));
                    toDo.setActivityList(loadToDoActivities(toDo.getId()));
                    loadToDoSharedUsers(toDo.getId()).forEach(toDo::addUser);
                    toDos.add(toDo);
                }
            }
        }
        return toDos;
    }

    // Rimosso @Override
    private Map<String, Boolean> loadToDoActivities(UUID todoId) throws SQLException {
        Map<String, Boolean> activities = new LinkedHashMap<>();
        String sql = "SELECT activity_title, completed FROM todo_activities WHERE todo_id = ? ORDER BY activity_id;";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, todoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    activities.put(rs.getString("activity_title"), rs.getBoolean("completed"));
                }
            }
        }
        return activities;
    }

    // Rimosso @Override
    private Set<User> loadToDoSharedUsers(UUID todoId) throws SQLException {
        Set<User> sharedUsers = new HashSet<>();
        String sql = "SELECT username FROM todo_shared_users WHERE todo_id = ?;";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, todoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sharedUsers.add(new User(rs.getString("username"), ""));
                }
            }
        }
        return sharedUsers;
    }

    @Override
    public int getBoardId(BoardName boardName, String username) throws SQLException {
        String sql = "SELECT board_id FROM boards WHERE board_name = ? AND username = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, boardName.getDisplayName());
            pstmt.setString(2, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("board_id");
                }
            }
        }
        return -1;
    }
}