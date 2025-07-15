package dao;

import models.User;
import models.ToDo;
import models.board.Board;
import models.board.BoardName;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserDAO {

    boolean saveUser(User user) throws SQLException;

    Optional<User> getUserByUsername(String username) throws SQLException;

    List<Board> loadBoardsForUser(String username) throws SQLException;

    boolean saveBoard(Board board, String username) throws SQLException;

    boolean saveToDo(ToDo toDo, int boardId) throws SQLException;

    boolean updateToDo(ToDo toDo, int boardId) throws SQLException;

    boolean deleteToDo(java.util.UUID todoId) throws SQLException;

    List<ToDo> loadToDosForBoard(int boardId) throws SQLException;

    int getBoardId(BoardName boardName, String username) throws SQLException;
}