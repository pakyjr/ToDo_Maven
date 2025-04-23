package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestToDo {

    private ToDo todo;

    @BeforeEach
    public void setUp() {
        todo = new ToDo("Test ToDo");
    }

    @Test
    public void testToDoCreation() {
        assertEquals("Test ToDo", todo.getTitle());
        assertEquals("white", todo.getColor());
        assertFalse(todo.isDone());
        assertTrue(todo.getActivityList().isEmpty());
        assertTrue(todo.getUsers().isEmpty());
    }

    @Test
    public void testSetPosition() {
        todo.setPosition(3);
        assertEquals(3, todo.getPosition());
    }

    @Test
    public void testSetDueDate() {
        Date dueDate = new Date();
        todo.setDueDate(dueDate);
        assertEquals(dueDate, todo.getDueDate());
    }

    @Test
    public void testSetOwner() {
        todo.setOwner("testUser");
        assertEquals("testUser", todo.getOwner());
    }

    @Test
    public void testSetUrl() {
        String url = "https://example.com";
        todo.setUrl(url);
        assertEquals(url, todo.getUrl());
    }

    @Test
    public void testSetImage() {
        String image = "image.jpg";
        todo.setImage(image);
        assertEquals(image, todo.getImage());
    }

    @Test
    public void testSetTitle() {
        todo.setTitle("New Title");
        assertEquals("New Title", todo.getTitle());
    }

    @Test
    public void testSetDescription() {
        String description = "This is a test description";
        todo.setDescription(description);
        assertEquals(description, todo.getDescription());
    }

    @Test
    public void testSetColor() {
        todo.setColor("blue");
        assertEquals("blue", todo.getColor());
    }

    @Test
    public void testAddActivity() {
        todo.addActivity("Task 1");

        Map<String, Boolean> activities = todo.getActivityList();
        assertEquals(1, activities.size());
        assertTrue(activities.containsKey("Task 1"));
        assertFalse(activities.get("Task 1"));
    }

    @Test
    public void testDeleteActivity() {
        todo.addActivity("Task 1");
        todo.addActivity("Task 2");

        todo.deleteActivity("Task 1");

        Map<String, Boolean> activities = todo.getActivityList();
        assertEquals(1, activities.size());
        assertFalse(activities.containsKey("Task 1"));
        assertTrue(activities.containsKey("Task 2"));
    }

    @Test
    public void testSetActivityTrue() {
        todo.addActivity("Task 1");
        todo.setActivityTrue("Task 1");

        Map<String, Boolean> activities = todo.getActivityList();
        assertTrue(activities.get("Task 1"));
        assertTrue(todo.isDone()); // All activities are complete, so todo should be done
    }

    @Test
    public void testSetActivityFalse() {
        todo.addActivity("Task 1");
        todo.setActivityTrue("Task 1");
        assertTrue(todo.isDone());

        todo.setActivityFalse("Task 1");

        Map<String, Boolean> activities = todo.getActivityList();
        assertFalse(activities.get("Task 1"));
        assertFalse(todo.isDone());
    }

    @Test
    public void testSetActivityWithInvalidActivity() {
        todo.setActivityTrue("NonExistentTask");
        assertEquals(0, todo.getActivityList().size());
    }

    @Test
    public void testAllActivitiesCompleteSetsDoneToTrue() {
        todo.addActivity("Task 1");
        todo.addActivity("Task 2");

        todo.setActivityTrue("Task 1");
        assertFalse(todo.isDone()); // Not all activities complete

        todo.setActivityTrue("Task 2");
        assertTrue(todo.isDone()); // All activities complete
    }

    @Test
    public void testToggle() {
        assertFalse(todo.isDone());

        todo.toggle();
        assertTrue(todo.isDone());

        todo.toggle();
        assertFalse(todo.isDone());
    }

    @Test
    public void testGetActivityListReturnsDefensiveCopy() {
        todo.addActivity("Task 1");
        Map<String, Boolean> activities = todo.getActivityList();

        // Modify the returned map
        activities.put("New Task", true);

        // Verify the todo's activity list is unchanged
        assertEquals(1, todo.getActivityList().size());
        assertFalse(todo.getActivityList().containsKey("New Task"));
    }

    @Test
    public void testAddUser() {
        User user = new User("user1", "password1");
        todo.addUser(user);

        assertEquals(1, todo.getUsers().size());
        assertTrue(todo.getUsers().contains(user));
    }

    @Test
    public void testAddDuplicateUser() {
        User user = new User("user1", "password");
        todo.addUser(user);
        todo.addUser(user);

        assertEquals(1, todo.getUsers().size());
    }

    @Test
    public void testRemoveUser() {
        User user = new User("user1", "password");
        User user2 = new User("user2", "password2");
        todo.addUser(user);
        todo.addUser(user2);

        todo.removeUser(user);

        assertEquals(1, todo.getUsers().size());
        assertTrue(todo.getUsers().contains(user2));
        assertFalse(todo.getUsers().contains(user));
    }

    @Test
    public void testGetUsersReturnsDefensiveCopy() {
        User user = new User("user1", "password");
        User user2 = new User("user2", "password2");
        todo.addUser(user);

        // Modify the returned list
        todo.getUsers().add(user2);

        // Verify the todo's user list is unchanged
        assertEquals(1, todo.getUsers().size());
        assertFalse(todo.getUsers().contains(user2));
    }
}