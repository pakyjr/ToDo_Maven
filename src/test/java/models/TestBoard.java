package models;

import models.ToDo;
import models.User;
import models.board.Board;
import models.board.BoardName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestBoard {

    private Board board;
    private BoardName boardName;
    private String owner;

    @BeforeEach
    public void setUp() {
        owner = "testUser";
        boardName = BoardName.FREE_TIME;
        board = new Board(boardName, owner);
    }

    @Test
    public void testBoardCreation() {
        assertEquals(boardName, board.getName());
        assertTrue(board.getTodoList().isEmpty());
    }

    @Test
    public void testBoardCreationWithDescription() {
        String description = "This is a test board";
        Board boardWithDesc = new Board(boardName, owner, description);

        assertEquals(boardName, boardWithDesc.getName());
        assertTrue(boardWithDesc.getTodoList().isEmpty());
    }

    @Test
    public void testAddTodo() {
        ToDo todo = board.addTodo("Test ToDo");

        assertEquals(1, board.getTodoList().size());
        assertEquals("Test ToDo", todo.getTitle());
        assertEquals(owner, todo.getOwner());
        assertEquals(1, todo.getPosition());
    }

    @Test
    public void testAddTodoWithSpecificOwner() {
        String specificOwner = "otherUser";
        ToDo todo = board.addTodo("Test ToDo", specificOwner);

        assertEquals(specificOwner, todo.getOwner());
    }

    @Test
    public void testAddMultipleTodos() {
        ToDo todo1 = board.addTodo("First ToDo");
        ToDo todo2 = board.addTodo("Second ToDo");

        assertEquals(2, board.getTodoList().size());
        assertEquals(1, todo1.getPosition());
        assertEquals(2, todo2.getPosition());
    }

    @Test
    public void testAddExistingTodo() {
        ToDo todo = new ToDo("Existing ToDo");
        board.addExistingTodo(todo);

        assertEquals(1, board.getTodoList().size());
        assertEquals("Existing ToDo", board.getTodoList().get(0).getTitle());
        assertEquals(1, todo.getPosition());
    }

    @Test
    public void testGetTodoList() {
        board.addTodo("Test ToDo");
        List<ToDo> todoList = board.getTodoList();

        assertEquals(1, todoList.size());

        // Verify that modifying the returned list doesn't affect the board's list
        todoList.clear();
        assertEquals(1, board.getTodoList().size());
    }

    @Test
    public void testShareTodo() {
        User boardOwner = new User("boardOwner", "password");
        User guest = new User("guest", "password");

        boardOwner.addBoard(boardName, boardOwner.getUsername());
        guest.addBoard(boardName, guest.getUsername());

        Board ownerBoard = boardOwner.getBoard(boardName);
        Board guestBoard = guest.getBoard(boardName);

        ToDo todo = ownerBoard.addTodo("Shared ToDo");

        ownerBoard.shareTodo(guest, todo);

        // Verify the to do was added to the guest's board
        assertEquals(1, guestBoard.getTodoList().size());
        assertEquals("Shared ToDo", guestBoard.getTodoList().get(0).getTitle());
        assertEquals(boardOwner.getUsername(), guestBoard.getTodoList().get(0).getOwner()); //on the guest board the shared to do should have the original owner as an owner
    }

    @Test
    public void testChangePosition() {
        ToDo todo1 = board.addTodo("First ToDo");
        ToDo todo2 = board.addTodo("Second ToDo");
        ToDo todo3 = board.addTodo("Third ToDo");

        board.changePosition(todo1, 3);

        List<ToDo> todoList = board.getTodoList();
        assertEquals(3, todoList.size());

        assertEquals(todo2, todoList.get(0));
        assertEquals(todo3, todoList.get(1));
        assertEquals(todo1, todoList.get(2));

        assertEquals(1, todo2.getPosition());
        assertEquals(2, todo3.getPosition());
        assertEquals(3, todo1.getPosition());
    }

    @Test
    public void testChangePositionInvalidPosition() {
        ToDo todo = board.addTodo("Test ToDo");

        // Try to change to an invalid position
        board.changePosition(todo, 0);
        assertEquals(1, todo.getPosition());

        board.changePosition(todo, 2);
        assertEquals(1, todo.getPosition());
    }

    @Test
    public void testDeleteTodo() {
        ToDo todo1 = board.addTodo("First ToDo");
        ToDo todo2 = board.addTodo("Second ToDo");
        ToDo todo3 = board.addTodo("Third ToDo");

        board.deleteTodo(todo2);

        List<ToDo> todoList = board.getTodoList();
        assertEquals(2, todoList.size());
        assertEquals(todo1, todoList.get(0));
        assertEquals(todo3, todoList.get(1));

        assertEquals(1, todo1.getPosition());
        assertEquals(2, todo3.getPosition());
    }

    //TODO TEST SHARED BOARDS ON DELETE

//    @Test
//    public void testGetTodosDueOn() {
//        ToDo todo1 = board.addTodo("Todo Today");
//        ToDo todo2 = board.addTodo("Todo Tomorrow");
//        ToDo todo3 = board.addTodo("Todo No Date");
//
//        Calendar cal = Calendar.getInstance();
//        Date today = cal.getTime();
//
//        todo1.setDueDate(today);
//
//        cal.add(Calendar.DAY_OF_MONTH, 1);
//        Date tomorrow = cal.getTime();
//        todo2.setDueDate(tomorrow);
//
//        List<ToDo> todosToday = board.getTodosDueOn(today);
//        assertEquals(1, todosToday.size());
//        assertEquals(todo1, todosToday.get(0));
//
//        List<ToDo> todosTomorrow = board.getTodosDueOn(tomorrow);
//        assertEquals(1, todosTomorrow.size());
//        assertEquals(todo2, todosTomorrow.get(0));
//    }
//
//    @Test
//    public void testGetTodosDueToday() {
//        // Create a todo due today
//        ToDo todo = board.addTodo("Todo Today");
//        todo.setDueDate(new Date());
//
//        List<ToDo> todosToday = board.getTodosDueToday();
//        assertEquals(1, todosToday.size());
//        assertEquals(todo, todosToday.get(0));
//    }
}