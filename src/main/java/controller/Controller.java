package controller;

import models.*;
import models.board.Board;
import models.board.BoardName;
import dao.UserDAO;
import dao.UserDAOImpl;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.SQLException; // Ensure SQLException is imported
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Controller {
    public User user;
    private UserDAO userDAO;

    // MODIFIED: Declare that the constructor can throw SQLException
    public Controller() throws SQLException {
        this.userDAO = new UserDAOImpl();
    }

    public void register(String username, String plainPassword){
        User newUser = new User(username, plainPassword);

        try {
            boolean success = userDAO.saveUser(newUser); // Save user to DB, newUser's ID is set by DAO
            if (success) {
                this.user = newUser; // Set the newly registered user as the current user

                // NEW: Populate in-memory boards for the new user
                newUser.fillBoard(newUser.getUsername());

                // NEW: Save each of these newly created boards to the database
                for (Board board : newUser.getBoardList()) {
                    userDAO.saveBoard(board, newUser.getId()); // Pass the User's ID
                }

                System.out.println("User '" + username + "' registered successfully and default boards created.");
            } else {
                System.err.println("Registration failed: User '" + username + "' might already exist.");
                this.user = null; // Registration failed, no user logged in
            }
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
            this.user = null; // Database error, no user logged in
        }
    }

    public User login(String username, String plainPassword) throws SQLException {
        Optional<User> optionalUser = userDAO.getUserByUsername(username);

        if (optionalUser.isPresent()) {
            User foundUser = optionalUser.get();
            // Using the checkPassword method (which uses BCrypt)
            if (foundUser.checkPassword(plainPassword)) {
                this.user = foundUser; // Set the logged-in user as the current user
                // Load user's boards and their ToDos from the database upon successful login
                userDAO.loadUserBoardsAndToDos(this.user);
                System.out.println("User '" + username + "' logged in successfully.");
                return foundUser;
            } else {
                System.out.println("Login failed: Incorrect password for user '" + username + "'.");
                this.user = null;
                return null;
            }
        } else {
            System.out.println("Login failed: User '" + username + "' not found.");
            this.user = null;
            return null;
        }
    }

    /**
     * Adds a new ToDo to the specified board for the current user.
     * The 'owner' of the ToDo is the currently logged-in user.
     *
     * @param boardNameStr The display name of the board.
     * @param toDoName The title of the ToDo.
     * @param description The description of the ToDo.
     * @param date The due date string (dd/MM/yyyy).
     * @param url A URL associated with the ToDo.
     * @param color The color of the ToDo.
     * @param image The image filename for the ToDo.
     * @param activities A map of activities and their completion status.
     * @param status The overall status of the ToDo.
     * @param owner The username of the ToDo's creator (should be current user's username).
     * @return The UUID string of the newly added ToDo, or null if creation failed.
     */
    public String addToDo(String boardNameStr, String toDoName, String description, String date, String url, String color, String image, Map<String, Boolean> activities, String status, String owner){
        if (this.user == null) {
            System.err.println("Error: No user is logged in to add a ToDo.");
            return null;
        }

        // Validate that the provided owner matches the current logged-in user
        if (!this.user.getUsername().equals(owner)) {
            System.err.println("Error: Attempted to create ToDo with owner '" + owner + "' but current user is '" + this.user.getUsername() + "'. ToDo owner must match current user.");
            return null;
        }

        BoardName boardEnumName;
        try {
            boardEnumName = BoardName.fromDisplayName(boardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name '" + boardNameStr + "'. " + e.getMessage());
            return null;
        }

        Board board = user.getBoard(boardEnumName);

        if (board == null) {
            System.err.println("Error: Board '" + boardNameStr + "' not found for user.");
            return null;
        }

        // When adding a new ToDo, its owner is always the current user
        ToDo toDo = board.addTodo(toDoName, owner); // Pass owner to board.addTodo
        if (toDo == null) {
            return null;
        }

        toDo.setDescription(description);
        toDo.setUrl(url);
        toDo.setColor(color);
        toDo.setImage(image);
        toDo.setActivityList(activities);
        toDo.setStatus(status);

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(date, formatter);
            toDo.setDueDate(localDate);
        } catch (java.time.format.DateTimeParseException e) {
            System.err.println("Error parsing due date: " + date + " - " + e.getMessage());
            return null; // Or handle as needed
        }

        try {
            int boardId = userDAO.getBoardId(boardEnumName, user.getUsername());
            if (boardId != -1) {
                userDAO.saveToDo(toDo, boardId);
                System.out.println("ToDo '" + toDoName + "' added successfully to board '" + boardNameStr + "'.");
            } else {
                System.err.println("Board not found in database for saving ToDo.");
                // Revert in-memory addition if DB save fails
                board.removeToDo(toDo); // This needs to remove by ID for robustness
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Database error saving ToDo: " + e.getMessage());
            e.printStackTrace();
            // Revert in-memory addition if DB save fails
            board.removeToDo(toDo); // This needs to remove by ID for robustness
            return null;
        }

        return toDo.getId().toString();
    }

    /**
     * Updates an existing ToDo on the specified board for the current user.
     * Only the ToDo's owner (creator) can update its properties.
     *
     * @param boardNameStr The display name of the board.
     * @param oldToDoTitle The original title of the ToDo to be updated.
     * @param newToDoTitle The new title for the ToDo.
     * @param description The new description.
     * @param date The new due date string (dd/MM/yyyy).
     * @param url The new URL.
     * @param color The new color.
     * @param image The new image filename.
     * @param activities The updated map of activities.
     * @param status The new overall status.
     * @param owner The username of the ToDo's creator (should be current user's username).
     */
    public void updateToDo(String boardNameStr, String oldToDoTitle, String newToDoTitle, String description, String date, String url, String color, String image, Map<String, Boolean> activities, String status, String owner) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to update a ToDo.");
            return;
        }

        BoardName boardEnumName;
        try {
            boardEnumName = BoardName.fromDisplayName(boardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name '" + boardNameStr + "'. " + e.getMessage());
            return;
        }

        Board board = user.getBoard(boardEnumName);

        if (board == null) {
            System.err.println("Error: Board '" + boardNameStr + "' not found for user.");
            return;
        }

        Optional<ToDo> optionalToDo = board.getTodoList().stream()
                .filter(t -> t.getTitle().equals(oldToDoTitle))
                .findFirst();

        if (optionalToDo.isPresent()) {
            ToDo toDoToUpdate = optionalToDo.get();

            // Permission check: Only the owner can update the ToDo
            if (!isCurrentUserToDoCreator(toDoToUpdate)) {
                System.err.println("Permission Denied: User '" + this.user.getUsername() + "' is not the owner of ToDo '" + oldToDoTitle + "'. Update aborted.");
                return;
            }

            // If the title is changed, ensure the new title doesn't create a duplicate on the board
            if (!oldToDoTitle.equals(newToDoTitle) &&
                    board.getTodoList().stream().anyMatch(t -> t.getTitle().equals(newToDoTitle) && t.getOwner().equals(toDoToUpdate.getOwner()))) {
                System.err.println("Error: A ToDo with title '" + newToDoTitle + "' and same owner already exists on board '" + boardNameStr + "'. Update aborted.");
                return;
            }

            toDoToUpdate.setTitle(newToDoTitle);
            toDoToUpdate.setDescription(description);
            toDoToUpdate.setUrl(url);
            toDoToUpdate.setColor(color);
            toDoToUpdate.setImage(image);
            toDoToUpdate.setActivityList(activities);
            toDoToUpdate.setStatus(status);

            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate localDate = LocalDate.parse(date, formatter);
                toDoToUpdate.setDueDate(localDate);
            } catch (java.time.format.DateTimeParseException e) {
                System.err.println("Error parsing due date: " + date + " - " + e.getMessage());
                return; // Or handle as needed
            }

            try {
                int boardId = userDAO.getBoardId(boardEnumName, user.getUsername());
                if (boardId != -1) {
                    userDAO.updateToDo(toDoToUpdate, boardId); // DAO will use toDoToUpdate.getId() and toDoToUpdate.getOwner()
                    System.out.println("ToDo '" + oldToDoTitle + "' updated successfully to '" + newToDoTitle + "' on board '" + boardNameStr + "'.");
                } else {
                    System.err.println("Board not found in database for updating ToDo.");
                }
            } catch (SQLException e) {
                System.err.println("Database error updating ToDo: " + e.getMessage());
                e.printStackTrace();
            }

        } else {
            System.err.println("ToDo with title '" + oldToDoTitle + "' not found on board " + boardNameStr);
        }
    }

    /**
     * Retrieves a ToDo object from the current user's board by its title.
     * Note: This method returns the actual ToDo object from the in-memory list,
     * not a copy, so be careful with direct modifications if that's not intended.
     *
     * @param title The title of the ToDo.
     * @param boardNameStr The display name of the board.
     * @return The ToDo object, or null if not found or an error occurs.
     */
    public ToDo getToDoByTitle(String title, String boardNameStr){
        if (this.user == null) {
            System.err.println("Error: No user is logged in to get a ToDo.");
            return null;
        }

        BoardName boardEnumName;
        try {
            boardEnumName = BoardName.fromDisplayName(boardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name '" + boardNameStr + "'. " + e.getMessage());
            return null;
        }

        Board board = user.getBoard(boardEnumName);
        if (board == null) {
            System.err.println("Board " + boardNameStr + " not found for user '" + user.getUsername() + "'.");
            return null;
        }

        return board.getTodoList().stream()
                .filter(toDo -> toDo.getTitle().equals(title))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves a list of ToDo titles for a given board of the current user.
     *
     * @param boardNameStr The display name of the board.
     * @return An ArrayList of ToDo titles (Strings).
     */
    public ArrayList<String> getToDoListString(String boardNameStr){
        if (this.user == null) {
            System.err.println("Error: No user is logged in to get ToDo list.");
            return new ArrayList<>();
        }

        BoardName boardEnumName;
        try {
            boardEnumName = BoardName.fromDisplayName(boardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name '" + boardNameStr + "'. " + e.getMessage());
            return new ArrayList<>();
        }

        Board board = user.getBoard(boardEnumName);
        ArrayList<String> resultTitles = new ArrayList<>();
        if (board != null) {
            for(ToDo toDo : board.getTodoList()) {
                resultTitles.add(toDo.getTitle());
            }
        }
        return resultTitles;
    }

    /**
     * Deletes a ToDo from a specific board of the current user.
     * If the deleted ToDo is an original (i.e., created by this user),
     * it also attempts to remove all its shared copies from other users' boards.
     *
     * @param boardNameStr The display name of the board from which to delete the ToDo.
     * @param toDoTitle The title of the ToDo to delete.
     */
    public void deleteToDo(String boardNameStr, String toDoTitle) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to delete a ToDo.");
            return;
        }

        BoardName boardEnumName;
        try {
            boardEnumName = BoardName.fromDisplayName(boardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name '" + boardNameStr + "'. " + e.getMessage());
            return;
        }

        Board board = user.getBoard(boardEnumName);
        if (board == null) {
            System.err.println("Board " + boardNameStr + " not found for user.");
            return;
        }

        Optional<ToDo> toDoToRemoveOptional = board.getTodoList().stream()
                .filter(toDo -> toDo.getTitle().equals(toDoTitle))
                .findFirst();

        if (toDoToRemoveOptional.isPresent()) {
            ToDo toDoToRemove = toDoToRemoveOptional.get();

            // Permission check: Only the owner can delete the original ToDo.
            // If it's a shared copy, the current user (recipient) can delete it from their board.
            boolean isCreator = isCurrentUserToDoCreator(toDoToRemove);
            boolean isRecipientDeleting = !isCreator && this.user.getBoard(boardEnumName).getTodoList().contains(toDoToRemove);

            if (!isCreator && !isRecipientDeleting) {
                System.err.println("Permission Denied: You cannot delete this ToDo.");
                return;
            }

            try {
                // If the current user is the creator, remove all shared instances from DB
                if (isCreator) {
                    userDAO.removeAllToDoSharing(toDoToRemove.getId().toString());
                    System.out.println("Removed all shared instances of ToDo '" + toDoTitle + "'.");
                }
                // Delete from the current user's board (in-memory and DB)
                userDAO.deleteToDo(toDoToRemove.getId().toString(), user.getUsername()); // Ensure DAO handles deletion by creator or recipient
                board.removeToDo(toDoToRemove);
                System.out.println("ToDo '" + toDoTitle + "' deleted successfully from board '" + boardNameStr + "'.");

                // If a recipient deleted a shared ToDo, refresh creator's in-memory users list for that ToDo
                if (isRecipientDeleting) {
                    // This is a complex scenario. Ideally, the creator's ToDo object should be updated.
                    // For simplicity, we assume the shared ToDo is a distinct entry for the recipient.
                    // If a true "shared object" model is needed, a more robust synchronization is required.
                }

            } catch (SQLException e) {
                System.err.println("Database error deleting ToDo: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("ToDo with title '" + toDoTitle + "' not found on board " + boardNameStr);
        }
    }

    /**
     * Moves a ToDo from one board to another for the current user.
     * Only the creator (owner) of the ToDo can move it.
     *
     * @param toDoTitle The title of the ToDo to move.
     * @param currentBoardDisplayName The display name of the board the ToDo is currently on.
     * @param destinationBoardDisplayName The display name of the board to move the ToDo to.
     * @return true if the ToDo was successfully moved, false otherwise.
     */
    public boolean moveToDo(String toDoTitle, String currentBoardDisplayName, String destinationBoardDisplayName) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to move a ToDo.");
            return false;
        }

        // Get BoardName enums from display names
        BoardName currentBoardEnum;
        BoardName destinationBoardEnum;
        try {
            currentBoardEnum = BoardName.fromDisplayName(currentBoardDisplayName);
            destinationBoardEnum = BoardName.fromDisplayName(destinationBoardDisplayName);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name provided. " + e.getMessage());
            return false;
        }

        // Get board objects from user's boards
        Board currentBoard = user.getBoard(currentBoardEnum);
        Board destinationBoard = user.getBoard(destinationBoardEnum);

        if (currentBoard == null) {
            System.err.println("Error: Source board '" + currentBoardDisplayName + "' not found for current user.");
            return false;
        }
        if (destinationBoard == null) {
            System.err.println("Error: Destination board '" + destinationBoardDisplayName + "' not found for current user.");
            return false;
        }

        // Find the ToDo in the current board
        Optional<ToDo> optionalToDo = currentBoard.getTodoList().stream()
                .filter(t -> t.getTitle().equals(toDoTitle))
                .findFirst();

        if (!optionalToDo.isPresent()) {
            System.err.println("Error: ToDo '" + toDoTitle + "' not found on board '" + currentBoardDisplayName + "'.");
            return false;
        }
        ToDo toDoToMove = optionalToDo.get();

        // Permission check: Only the creator can move the ToDo
        if (!isCurrentUserToDoCreator(toDoToMove)) {
            System.err.println("Permission Denied: Only the creator can move this ToDo.");
            return false;
        }

        // Check for duplicate title in destination board (optional but good practice)
        if (destinationBoard.getTodoList().stream().anyMatch(t -> t.getTitle().equals(toDoTitle) && t.getOwner().equals(toDoToMove.getOwner()))) {
            System.err.println("Error: A ToDo with title '" + toDoTitle + "' and same owner already exists on destination board '" + destinationBoardDisplayName + "'. Move aborted.");
            return false;
        }

        try {
            // Get database IDs for current and destination boards
            int currentBoardId = userDAO.getBoardId(currentBoardEnum, user.getUsername()); // Not strictly needed for moving, but good for consistency check
            int destinationBoardId = userDAO.getBoardId(destinationBoardEnum, user.getUsername());

            if (currentBoardId == -1 || destinationBoardId == -1) {
                System.err.println("Database error: Could not find board IDs for move operation.");
                return false;
            }

            // Update the ToDo's board_id in the database
            userDAO.updateToDoBoardId(toDoToMove.getId().toString(), destinationBoardId);

            // Update in-memory model
            currentBoard.removeToDo(toDoToMove); // Remove from source board's in-memory list
            destinationBoard.addExistingTodo(toDoToMove); // Add to destination board's in-memory list (without creating new ID)

            System.out.println("ToDo '" + toDoTitle + "' successfully moved from '" + currentBoardDisplayName + "' to '" + destinationBoardDisplayName + "'.");
            return true;

        } catch (SQLException e) {
            System.err.println("Database error moving ToDo '" + toDoTitle + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Shares a ToDo with a list of specified users.
     * This method adds entries to the shared_todos table and updates the in-memory ToDo object.
     *
     * @param toDo The ToDo object to share.
     * @param usernamesToShareWith A list of usernames to share the ToDo with.
     * @param boardNameStr The display name of the board the ToDo belongs to. (Needed for context, though not directly used in DAO calls for sharing itself)
     * @return true if sharing was successful for all users, false otherwise.
     */
    public boolean shareToDoWithUsers(ToDo toDo, List<String> usernamesToShareWith, String boardNameStr) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in.");
            return false;
        }
        if (toDo == null) {
            System.err.println("Error: ToDo to share cannot be null.");
            return false;
        }
        if (!isCurrentUserToDoCreator(toDo)) {
            System.err.println("Permission Denied: Only the creator can share this ToDo.");
            return false;
        }
        if (usernamesToShareWith == null || usernamesToShareWith.isEmpty()) {
            System.out.println("No users selected to share with.");
            return true; // No action needed, consider it successful
        }

        boolean allSuccess = true;
        for (String username : usernamesToShareWith) {
            try {
                // Add to shared_todos table
                userDAO.shareToDo(toDo.getId().toString(), username);

                // Add to in-memory ToDo object's shared users list
                Optional<User> sharedUserOptional = userDAO.getUserByUsername(username);
                if (sharedUserOptional.isPresent()) {
                    toDo.addSharedUser(sharedUserOptional.get());
                    System.out.println("ToDo '" + toDo.getTitle() + "' shared with user '" + username + "'.");
                } else {
                    System.err.println("User '" + username + "' not found to add to in-memory ToDo object.");
                    allSuccess = false; // Even if DB write was successful, in-memory is inconsistent
                }

            } catch (SQLException e) {
                System.err.println("Database error sharing ToDo '" + toDo.getTitle() + "' with '" + username + "': " + e.getMessage());
                e.printStackTrace();
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    /**
     * Removes sharing of a ToDo from a list of specified users.
     * This method deletes entries from the shared_todos table and updates the in-memory ToDo object.
     *
     * @param toDo The ToDo object to remove sharing from.
     * @param usernamesToRemoveSharing A list of usernames to remove sharing from.
     * @return true if removal was successful for all users, false otherwise.
     */
    public boolean removeToDoSharing(ToDo toDo, List<String> usernamesToRemoveSharing) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in.");
            return false;
        }
        if (toDo == null) {
            System.err.println("Error: ToDo cannot be null.");
            return false;
        }
        if (!isCurrentUserToDoCreator(toDo)) {
            System.err.println("Permission Denied: Only the creator can manage sharing for this ToDo.");
            return false;
        }
        if (usernamesToRemoveSharing == null || usernamesToRemoveSharing.isEmpty()) {
            System.out.println("No users selected to remove sharing from.");
            return true; // No action needed, consider it successful
        }

        boolean allSuccess = true;
        for (String username : usernamesToRemoveSharing) {
            try {
                // Remove from shared_todos table
                userDAO.removeToDoSharing(toDo.getId().toString(), username);

                // Remove from in-memory ToDo object's shared users list
                toDo.removeSharedUser(username);
                System.out.println("Removed sharing of ToDo '" + toDo.getTitle() + "' from user '" + username + "'.");

            } catch (SQLException e) {
                System.err.println("Database error removing sharing of ToDo '" + toDo.getTitle() + "' from '" + username + "': " + e.getMessage());
                e.printStackTrace();
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    /**
     * Retrieves all registered users from the database.
     *
     * @return A Set of User objects, or an empty set if no users are found or an error occurs.
     */
    public Set<User> getAllUsers() {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to retrieve all users.");
            return java.util.Collections.emptySet();
        }
        try {
            return userDAO.getAllUsers();
        } catch (SQLException e) {
            System.err.println("Database error retrieving all users: " + e.getMessage());
            e.printStackTrace();
            return java.util.Collections.emptySet();
        }
    }

    /**
     * Checks if the currently logged-in user is the creator (owner) of a given ToDo.
     *
     * @param toDo The ToDo object to check.
     * @return true if the current user is the owner, false otherwise.
     */
    public boolean isCurrentUserToDoCreator(ToDo toDo) {
        return this.user != null && toDo != null && this.user.getUsername().equals(toDo.getOwner());
    }


    public ArrayList<String> getSharedUsersForToDo(String boardNameStr, String toDoTitle) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in.");
            return new ArrayList<>();
        }

        BoardName boardEnumName;
        try {
            boardEnumName = BoardName.fromDisplayName(boardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name '" + boardNameStr + "'. " + e.getMessage());
            return new ArrayList<>();
        }

        Board board = user.getBoard(boardEnumName);
        if (board == null) {
            System.err.println("Error: Board \"" + boardNameStr + "\" not found for current user.");
            return new ArrayList<>();
        }

        Optional<ToDo> optionalToDo = board.getTodoList().stream()
                .filter(t -> t.getTitle().equals(toDoTitle))
                .findFirst();

        if (optionalToDo.isPresent()) {
            ToDo originalToDo = optionalToDo.get();
            // Only the creator can see who it's shared with.
            if (!isCurrentUserToDoCreator(originalToDo)) {
                System.err.println("Permission Denied: Only the creator can see who this ToDo is shared with.");
                return new ArrayList<>();
            }
            return originalToDo.getUsers().stream()
                    .map(User::getUsername)
                    .collect(Collectors.toCollection(ArrayList::new));
        } else {
            System.err.println("Error: ToDo '" + toDoTitle + "' not found in board '" + boardNameStr + "'.");
            return new ArrayList<>();
        }
    }
}