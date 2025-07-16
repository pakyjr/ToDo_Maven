package dao;

import models.board.Board;
import models.ToDo;
import models.User;
import models.board.BoardName;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set; // Make sure Set is imported

public interface UserDAO {
    boolean saveUser(User user) throws SQLException;
    Optional<User> getUserByUsername(String username) throws SQLException;
    void loadUserBoardsAndToDos(User user) throws SQLException;
    int getBoardId(BoardName boardName, String username) throws SQLException;
    void saveToDo(ToDo toDo, int boardId) throws SQLException;
    void updateToDo(ToDo toDo, int boardId) throws SQLException;
    void deleteToDo(String toDoId, String username) throws SQLException;
    void shareToDo(String toDoId, String sharedWithUsername) throws SQLException;
    void removeToDoSharing(String toDoId, String sharedWithUsername) throws SQLException;
    void removeAllToDoSharing(String toDoId) throws SQLException;
    Set<User> getAllUsers() throws SQLException;
    List<String> getSharedUsernamesForToDo(String toDoId) throws SQLException;

    // New method to update the board_id of a ToDo
    void updateToDoBoardId(String toDoId, int newBoardId) throws SQLException;
}