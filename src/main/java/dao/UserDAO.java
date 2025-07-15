package dao;

import models.ToDo;
import models.User;
import models.board.BoardName;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID; // Import UUID

public interface UserDAO {
    // User Operations
    boolean saveUser(User user) throws SQLException;
    Optional<User> getUserByUsername(String username) throws SQLException;
    ArrayList<User> getAllUsers() throws SQLException; // For sharing feature

    // Board Operations
    int getBoardId(BoardName boardName, String username) throws SQLException; // Get board ID by name and owner
    // No direct save/update/delete board methods here, as boards are managed with user creation/deletion

    // ToDo Operations
    void saveToDo(ToDo toDo, int boardId) throws SQLException;
    void updateToDo(ToDo toDo, int boardId) throws SQLException; // Updates all properties except ID, owner, board_id
    void deleteToDo(UUID toDoId) throws SQLException; // Delete a specific ToDo instance by its UUID
    void updateToDoBoardId(UUID toDoId, int newBoardId) throws SQLException; // To move a ToDo between boards
    void loadUserBoardsAndToDos(User user) throws SQLException; // Hydrates a User object with its boards and ToDos
}