package models;

import models.board.Board;
import models.board.BoardName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestUsers {

    private User user;
    private BoardName testBoardName;
    private BoardName secondBoardName;

    @BeforeEach
    public void setUp() {
        user = new User("testUser", "password123");
        testBoardName = BoardName.UNIVERSITY;
        secondBoardName = BoardName.WORK;
    }

    @Test
    public void testUserCreation() {
        assertEquals("testUser", user.getUsername());
        assertTrue(user.getBoardList().isEmpty());
    }

    @Test
    public void testAddBoard() {
        Optional<Board> boardOptional = user.addBoard(testBoardName, user.getUsername());

        assertTrue(boardOptional.isPresent());
        assertEquals(1, user.getBoardList().size());
        assertTrue(user.getBoardList().containsKey(testBoardName.toString()));
    }

    @Test
    public void testAddDuplicateBoard() {
        user.addBoard(testBoardName, user.getUsername());
        Optional<Board> duplicateBoardOptional = user.addBoard(testBoardName, user.getUsername());

        assertFalse(duplicateBoardOptional.isPresent());
        assertEquals(1, user.getBoardList().size());
    }

    @Test
    public void testDeleteBoard() {
        user.addBoard(testBoardName, user.getUsername());
        assertEquals(1, user.getBoardList().size());

        user.deleteBoard(testBoardName);
        assertEquals(0, user.getBoardList().size());
        assertFalse(user.getBoardList().containsKey(testBoardName.toString()));
    }

    @Test
    public void testDeleteNonExistentBoard() {
        user.deleteBoard(testBoardName);
        assertEquals(0, user.getBoardList().size());
    }

    @Test
    public void testGetBoard() {
        user.addBoard(testBoardName, user.getUsername());
        Board board = user.getBoard(testBoardName);

        assertNotNull(board);
        assertEquals(testBoardName, board.getName());
    }

    @Test
    public void testGetNonExistentBoard() {
        Board board = user.getBoard(testBoardName);
        assertNull(board);
    }

    @Test
    public void testGetBoardListReturnsUnmodifiableMap() {
        user.addBoard(testBoardName, user.getUsername());
        Map<String, Board> boardList = user.getBoardList();

        assertThrows(UnsupportedOperationException.class, () ->
                boardList.put("New Board", new Board(BoardName.UNIVERSITY, user.getUsername())));
    }

    @Test
    public void testMoveToDoToAnotherBoard() {
        // Setup boards
        user.addBoard(testBoardName, user.getUsername());
        user.addBoard(secondBoardName, user.getUsername());

        Board sourceBoard = user.getBoard(testBoardName);
        Board targetBoard = user.getBoard(secondBoardName);

        // Add todo to source board
        ToDo todo = sourceBoard.addTodo("Test ToDo");
        assertEquals(1, sourceBoard.getTodoList().size());
        assertEquals(0, targetBoard.getTodoList().size());

        // Move todo
        user.moveToDoToAnotherBoard(testBoardName, secondBoardName, 1);

        // Verify todo moved
        assertEquals(0, sourceBoard.getTodoList().size());
        assertEquals(1, targetBoard.getTodoList().size());
        assertEquals("Test ToDo", targetBoard.getTodoList().getFirst().getTitle());
    }

    @Test
    public void testMoveToDoWithInvalidPositionDoesNothing() {
        // Setup boards
        user.addBoard(testBoardName, user.getUsername());
        user.addBoard(secondBoardName, user.getUsername());

        Board sourceBoard = user.getBoard(testBoardName);
        Board targetBoard = user.getBoard(secondBoardName);

        // Add todo to source board
        sourceBoard.addTodo("Test ToDo");

        // Try to move with invalid position
        user.moveToDoToAnotherBoard(testBoardName, secondBoardName, 2); // Position 2 doesn't exist

        // Verify no changes
        assertEquals(1, sourceBoard.getTodoList().size());
        assertEquals(0, targetBoard.getTodoList().size());
    }

    @Test
    public void testMoveToDoWithNonExistentBoardDoesNothing() {
        // Setup source board only
        user.addBoard(testBoardName, user.getUsername());
        Board sourceBoard = user.getBoard(testBoardName);

        // Add todo to source board
        sourceBoard.addTodo("Test ToDo");

        // Try to move to non-existent board
        user.moveToDoToAnotherBoard(testBoardName, secondBoardName, 1);

        // Verify no changes
        assertEquals(1, sourceBoard.getTodoList().size());
    }
}