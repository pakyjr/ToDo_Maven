package models;

import models.ToDo;
import models.board.Board;
import models.board.BoardName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestBoard {

    @Test
    void testBoardCreationWithOwner() {
        Board board = new Board(BoardName.WORK, "user1");
        assertNotNull(board);
    }

    @Test
    void testBoardCreationWithDescription() {
        Board board = new Board(BoardName.WORK, "user1", "Project Board");
        assertEquals(Set.of("user1"), board.getAllowedUsers());
    }

    @Test
    void testAddUser() {
        Board board = new Board(BoardName.WORK, "user1", "Board");
        board.addUser("user2");
        assertTrue(board.getAllowedUsers().contains("user2"));
    }

    @Test
    void testAddTodo() {
        Board board = new Board(BoardName.WORK, "user1");
        ToDo todo = new ToDo("Task 1");
        board.addTodo(todo);
        assertEquals(1, todo.getPosition());
    }

    @Test
    void testDeleteTodo() {
        Board board = new Board(BoardName.WORK, "user1");
        ToDo todo1 = new ToDo("Task 1");
        ToDo todo2 = new ToDo("Task 2");

        board.addTodo(todo1);
        board.addTodo(todo2);
        board.deleteTodo(todo1);

        assertEquals(2, todo2.getPosition()); // note: this may need manual position fix if not updated
    }
}