package models;

import models.User;

import models.board.Board;
import models.board.BoardName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestUsers {

    @Test
    void testUserCreationWithPassword() {
        User user = new User("bob", "mypassword");
        assertNotNull(user.getUsername());
    }

    @Test
    void testAddAndDeleteBoard() {
        User user = new User("charlie", "mypassword");
        user.addBoard(BoardName.FREE_TIME, user.getUsername());
        user.deleteBoard(BoardName.FREE_TIME);
    }

    @Test
    void testDeleteNonExistentBoard() {
        User user = new User("dave", "mypassword");
        assertDoesNotThrow(() -> user.deleteBoard(BoardName.WORK));
    }

    @Test
    void testSharedBoard() {
        User user = new User("dave", "mypassword");
        User user2 = new User("charlie", "mypassword");

        Optional<Board> optionalBoard = user.addBoard(BoardName.UNIVERSITY, user.getUsername());

        if(optionalBoard.isPresent()) {
            ToDo todo = new ToDo("esame");
            Board userBoard = optionalBoard.get(); //optional unwrapping
            userBoard.addTodo(todo);
            userBoard.shareTodo(user2, todo);

            //controllare che user2 ha una board nuova
            //controllare che nella board nuyova ci sia il todo che abbiuamo aggiunto

            assertEquals(1, user2.getBoardList().size());
        }



    }
}