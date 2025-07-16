package dao;

import models.User;
import models.ToDo;
import models.board.Board; // Import Board
import models.board.BoardName; // Import BoardName

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface UserDAO {
    boolean saveUser(User user) throws SQLException;
    Optional<User> getUserByUsername(String username) throws SQLException;
    void loadUserBoardsAndToDos(User user) throws SQLException;
    int getBoardId(BoardName boardName, String username) throws SQLException;
    void saveToDo(ToDo toDo, int boardId) throws SQLException;
    void updateToDo(ToDo toDo, int boardId) throws SQLException;
    void updateToDoBoardId(String toDoId, int newBoardId) throws SQLException;
    void deleteToDo(String toDoId, String username) throws SQLException;
    void shareToDo(String toDoId, String sharedWithUsername) throws SQLException;
    void removeToDoSharing(String toDoId, String sharedWithUsername) throws SQLException;
    void removeAllToDoSharing(String toDoId) throws SQLException;
    Set<User> getAllUsers() throws SQLException;
    List<String> getSharedUsernamesForToDo(String toDoId) throws SQLException;

    // NEW METHOD: saveBoard
    void saveBoard(Board board, int userId) throws SQLException;
}