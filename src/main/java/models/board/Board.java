package models.board;

import models.ToDo;
import models.User;
import java.util.*;


public class Board {
    private final BoardName name;
    private String description;
    private final String owner; // This is the owner of the board
    private final List<ToDo> todoList; // List of ToDos on this specific board

    public Board(BoardName name, String owner) {
        this.name = name;
        this.owner = owner;
        this.todoList = new ArrayList<>();
    }

    public Board(BoardName name, String owner, String description) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.todoList = new ArrayList<>();
    }

    public ToDo addTodo(String title) {
        // When adding a ToDo without specifying an owner, the board's owner is the ToDo's owner
        return addTodo(title, this.owner);
    }

    public ToDo addTodo(String title, String owner) {
        if (todoList.stream().anyMatch(todo -> todo.getTitle().equals(title) && todo.getOwner().equals(owner))) {
            System.err.println("Error: A ToDo with title '" + title + "' by owner '" + owner + "' already exists on board " + this.name);
            return null;
        }

        ToDo todo = new ToDo(title);
        todo.setOwner(owner); // Set the owner (creator) of the ToDo
        todoList.add(todo);
        todo.setPosition(todoList.size()); // Assign position based on current list size
        return todo;
    }

    /**
     * Adds an existing ToDo object to this board.
     * This is used when loading ToDos from the database or when a ToDo is shared.
     * It prevents adding a duplicate ToDo based on its UUID to ensure each specific
     * instance on a board is unique.
     *
     * @param todo The ToDo object to add.
     */
    public void addExistingTodo(ToDo todo) {
        // Check for duplicate based on ID, not just title, as shared todos will have new IDs
        if (todoList.stream().anyMatch(existingTodo -> existingTodo.getId().equals(todo.getId()))) {
            System.err.println("Warning: ToDo with ID '" + todo.getId() + "' already exists on board " + this.name + ". Not adding duplicate.");
            return;
        }
        todoList.add(todo);
        todo.setPosition(todoList.size()); // Assign position
    }

    public ArrayList<ToDo> getTodoList() {
        // Return a new ArrayList to prevent external modification of the internal list
        return new ArrayList<>(todoList);
    }

    /**
     * Shares a ToDo with a guest user.
     * A *copy* of the original ToDo is created for the guest, with a new UUID.
     * The original ToDo maintains a reference to the guest (the user it was shared with).
     *
     * @param guest The User object to share the ToDo with.
     * @param originalToDo The original ToDo object created by the owner.
     */
    public void shareTodo(User guest, ToDo originalToDo) {
        if (guest == null || originalToDo == null) {
            System.err.println("Error: Cannot share null ToDo or with null guest.");
            return;
        }

        // Add the guest to the original ToDo's 'users' set
        // This set tracks who the *original* ToDo has been shared *to*.
        originalToDo.addUser(guest);

        // Get the guest's board instance of the same name
        Board guestBoard = guest.getBoard(this.name); // 'this.name' is the board name being shared

        if (guestBoard != null) {
            // Create a COPY of the original ToDo for the guest
            // The ToDo copy constructor generates a new UUID.
            ToDo sharedCopy = new ToDo(originalToDo);

            // Add the copied ToDo to the guest's board
            // The addExistingTodo method will handle position and check for duplicates by ID
            guestBoard.addExistingTodo(sharedCopy);
            System.out.println("ToDo '" + originalToDo.getTitle() + "' copied and shared with user '" + guest.getUsername() + "' on their board '" + this.name + "'.");
        } else {
            System.err.println("Error: Guest user '" + guest.getUsername() + "' does not have a board named '" + this.name + "'. ToDo not shared.");
        }
    }

    public void changePosition(ToDo todo, int newPosition) {
        // Find the actual index of the todo in the list
        int oldIndex = -1;
        for (int i = 0; i < todoList.size(); i++) {
            if (todoList.get(i).getId().equals(todo.getId())) { // Compare by ID for robustness
                oldIndex = i;
                break;
            }
        }

        if (oldIndex == -1) {
            System.out.println("ToDo not found in this board's list: " + todo.getTitle());
            return;
        }

        if (newPosition < 1 || newPosition > todoList.size()) {
            System.out.println("Invalid position: " + newPosition + ". Must be between 1 and " + todoList.size());
            return;
        }

        int oldPosition = oldIndex + 1; // Convert 0-based index to 1-based position
        if (oldPosition == newPosition) {
            return; // No change needed
        }

        ToDo tempTodo = todoList.remove(oldIndex); // Remove the ToDo
        todoList.add(newPosition - 1, tempTodo); // Add it at the new desired index

        // Re-calculate and set positions for all ToDos in the list
        for (int i = 0; i < todoList.size(); i++) {
            todoList.get(i).setPosition(i + 1);
        }
        System.out.println("Changed position of ToDo '" + todo.getTitle() + "' from " + oldPosition + " to " + newPosition + " on board '" + this.name + "'.");
    }

    /**
     * Removes a ToDo from this board.
     * This method handles both original ToDos and shared copies.
     * If the removed ToDo is an original (i.e., this board's owner is the ToDo's owner),
     * it also ensures that all shared copies are removed from other users' boards.
     * If the removed ToDo is a shared copy, it only removes it from the current board.
     *
     * @param todoToRemove The ToDo object to remove.
     */
    public void removeToDo(ToDo todoToRemove) {
        // Find the ToDo by its ID to ensure we're removing the exact instance
        Optional<ToDo> foundToDoOpt = todoList.stream()
                .filter(t -> t.getId().equals(todoToRemove.getId()))
                .findFirst();

        if (foundToDoOpt.isEmpty()) {
            System.err.println("Error: ToDo '" + todoToRemove.getTitle() + "' (ID: " + todoToRemove.getId() + ") not found on board " + this.name + " for removal.");
            return;
        }

        ToDo actualToDoRemoved = foundToDoOpt.get(); // The actual instance from our list

        int positionRemoved = actualToDoRemoved.getPosition();
        todoList.remove(actualToDoRemoved); // Remove the ToDo instance from this board's list

        // Re-index remaining ToDos
        for (int i = 0; i < todoList.size(); i++) {
            ToDo item = todoList.get(i);
            if (item.getPosition() > positionRemoved) { // Only update if position was affected
                item.setPosition(item.getPosition() - 1);
            }
        }
        System.out.println("Removed ToDo '" + actualToDoRemoved.getTitle() + "' from board '" + this.name + "'.");


        // --- Handling Shared ToDos after removal ---
        // This part only applies if the ToDo being removed is the original one
        // (i.e., the owner of this board is the creator of the ToDo).
        if (this.owner.equals(actualToDoRemoved.getOwner())) { // If this is the *original* ToDo
            Set<User> usersWithSharedTodo = actualToDoRemoved.getUsers(); // Get users it was shared with

            if (usersWithSharedTodo != null && !usersWithSharedTodo.isEmpty()) {
                System.out.println("Attempting to remove shared copies of '" + actualToDoRemoved.getTitle() + "' from other users...");
                for (User user : new HashSet<>(usersWithSharedTodo)) { // Iterate over a copy to avoid ConcurrentModificationException
                    Board userBoard = user.getBoard(this.name); // Get the same board from the shared user
                    if (userBoard != null && !userBoard.equals(this)) { // Ensure it's not the same board and is a valid board
                        // Find the specific shared copy on the user's board by checking if it originated from this ToDo
                        // NOTE: This assumes 'ToDo' has a way to identify its 'origin'.
                        // Currently, the ToDo copy constructor just creates a new UUID.
                        // For a robust system, you'd add an `originalId` field to ToDo.
                        // For now, we'll iterate and compare by title and owner of the original.
                        // THIS IS A POTENTIAL WEAK SPOT. If multiple shared ToDos have the same title, it's ambiguous.
                        // A `UUID originalId` field in `ToDo` is highly recommended.

                        // Placeholder for a robust solution:
                        // userBoard.removeToDoByOriginalId(actualToDoRemoved.getId());

                        // For now, let's remove by original title and owner in the shared board
                        // This relies on the assumption that only one shared copy of 'actualToDoRemoved'
                        // with the same title exists on the guest's board from this specific owner.
                        Optional<ToDo> sharedCopyOnGuestBoard = userBoard.getTodoList().stream()
                                .filter(t -> t.getTitle().equals(actualToDoRemoved.getTitle()) && t.getOwner().equals(actualToDoRemoved.getOwner()))
                                .findFirst();

                        if (sharedCopyOnGuestBoard.isPresent()) {
                            userBoard.removeToDo(sharedCopyOnGuestBoard.get()); // Recursively call removeToDo on the guest's board
                        } else {
                            System.out.println("Shared copy of '" + actualToDoRemoved.getTitle() + "' not found on board '" + userBoard.getName() + "' for user '" + user.getUsername() + "'.");
                        }
                    }
                    // Remove the user from the original ToDo's shared list after attempting removal
                    actualToDoRemoved.removeUser(user);
                }
            }
            actualToDoRemoved.clearUsers(); // Clear all shared users from the original ToDo
        }
        // If the removed ToDo was a shared copy (this.owner != actualToDoRemoved.getOwner()),
        // no further action is needed as only this specific copy is removed.
    }

    public BoardName getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        // A board is uniquely identified by its name AND its owner.
        // Two users can have boards with the same name (e.g., "Work"), but they are distinct boards.
        return name == board.name && Objects.equals(owner, board.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, owner);
    }
}