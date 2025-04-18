package models;

import models.User;

import models.board.BoardName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestUsers {

    @Test
    void testUserCreationWithUsername() {
        User user = new User("alice");
        assertNotNull(user.getId());
    }

    @Test
    void testUserCreationWithPassword() {
        User user = new User("bob", "mypassword");
        assertNotNull(user.getId());
    }

    @Test
    void testAddAndDeleteBoard() {
        User user = new User("charlie");
        user.addBoard(BoardName.FREE_TIME);
        user.deleteBoard(BoardName.FREE_TIME);
    }

    @Test
    void testDeleteNonExistentBoard() {
        User user = new User("dave");
        assertDoesNotThrow(() -> user.deleteBoard(BoardName.WORK));
    }
}