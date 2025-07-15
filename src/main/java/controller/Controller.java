package controller;

import models.*;
import models.board.Board;
import models.board.BoardName;
import dao.UserDAO;
import dao.UserDAOImpl;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Controller {
    public User user;
    private UserDAO userDAO;

    public Controller() {
        this.userDAO = new UserDAOImpl();
    }

    public void register(String username, String plainPassword){
        User newUser = new User(username, plainPassword);

        try {
            boolean success = userDAO.saveUser(newUser);
            if (success) {
                this.user = newUser; // Set the newly registered user as the current user
                System.out.println("User '" + username + "' registered successfully.");
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
            String hashedPasswordAttempt = User.hashPassword(plainPassword);

            if (foundUser.getHashedPassword().equals(hashedPasswordAttempt)) {
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
    public String addToDo(String boardNameStr, String toDoName, String description, String date, String url, String color, String image, Map<String, Boolean> activities, String status, String owner){ // ADDED 'owner' PARAMETER
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
    public void updateToDo(String boardNameStr, String oldToDoTitle, String newToDoTitle, String description, String date, String url, String color, String image, Map<String, Boolean> activities, String status, String owner) { // ADDED 'owner' PARAMETER
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
            // The owner should not be changed during an update. It's the creator.
            // toDoToUpdate.setOwner(owner); // This line should NOT be here.

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

        // Return the actual ToDo object from the board's list
        // Filter by title and also by owner if you want to distinguish between original and shared with same title
        // For UI purposes, usually, you want the one displayed on *this* board.
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
                System.err.println("Permission Denied: You cannot delete this ToDo. Only the creator can delete the original, or you can delete your shared copy.");
                return;
            }

            // Remove from the current user's board (in-memory)
            board.removeToDo(toDoToRemove);
            System.out.println("ToDo '" + toDoTitle + "' removed from board " + boardNameStr + " for user '" + this.user.getUsername() + "'.");

            try {
                // Delete from the database
                // If it's the original ToDo, delete all instances (original + shared copies)
                // This logic is tricky. The `Board.removeToDo` method handles removing shared
                // copies from *other users' in-memory boards*.
                // Here, we need to handle the database removal.

                if (isCreator) {
                    // If the current user is the owner, delete the ToDo and all its shared copies from DB.
                    // This implies a mechanism in UserDAO to find and delete all copies based on original owner and title,
                    // or better, by an 'original_todo_id' if you add one to ToDo.
                    // For now, let's assume `deleteToDo` in DAO intelligently handles this based on UUID.
                    userDAO.deleteToDo(toDoToRemove.getId()); // Deletes the specific instance and potentially others based on owner/title in DAO
                    System.out.println("Original ToDo '" + toDoTitle + "' and its shared copies (if any) deleted from database.");

                    // After deleting the original, also ensure it's removed from any user's `users` set that it was shared with.
                    // The `Board.removeToDo` handles this for in-memory.
                    // For DB, we need to update records that represent shared ToDos no longer existing.
                    // This often means removing rows from a "shared_todos" table, or deleting actual ToDo entries.

                } else { // It's a shared copy being deleted by a recipient
                    userDAO.deleteToDo(toDoToRemove.getId()); // Delete only this specific shared instance from DB
                    System.out.println("Shared ToDo copy '" + toDoTitle + "' deleted from database for user '" + this.user.getUsername() + "'.");
                }
            } catch (SQLException e) {
                System.err.println("Database error deleting ToDo: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("ToDo '" + toDoTitle + "' not found on board " + boardNameStr + " for deletion.");
        }
    }

    /**
     * Moves a ToDo between boards for the current user.
     * This operation moves the *single instance* of the ToDo.
     *
     * @param toDoTitle The title of the ToDo to move.
     * @param sourceBoardNameStr The display name of the source board.
     * @param destinationBoardNameStr The display name of the destination board.
     * @return true if the ToDo was successfully moved, false otherwise.
     */
    public boolean moveToDo(String toDoTitle, String sourceBoardNameStr, String destinationBoardNameStr) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to move a ToDo.");
            return false;
        }

        BoardName sourceBoardEnumName;
        BoardName destinationBoardEnumName;
        try {
            sourceBoardEnumName = BoardName.fromDisplayName(sourceBoardNameStr);
            destinationBoardEnumName = BoardName.fromDisplayName(destinationBoardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name for move operation: " + e.getMessage());
            return false;
        }

        Board sourceBoard = user.getBoard(sourceBoardEnumName);
        Board destinationBoard = user.getBoard(destinationBoardEnumName);

        if (sourceBoard == null) {
            System.err.println("Error: Source Board '" + sourceBoardNameStr + "' not found.");
            return false;
        }
        if (destinationBoard == null) {
            System.err.println("Error: Destination Board '" + destinationBoardNameStr + "' not found.");
            return false;
        }
        if (sourceBoardEnumName.equals(destinationBoardEnumName)) {
            System.out.println("Cannot move ToDo to the same board.");
            return false;
        }

        Optional<ToDo> optionalToDo = sourceBoard.getTodoList().stream()
                .filter(t -> t.getTitle().equals(toDoTitle))
                .findFirst();

        if (optionalToDo.isPresent()) {
            ToDo toDoToMove = optionalToDo.get();

            // Check if a ToDo with the same title AND owner already exists in the destination board
            // This is crucial to prevent internal duplicates for the same *logical* ToDo.
            if (destinationBoard.getTodoList().stream().anyMatch(t -> t.getTitle().equals(toDoTitle) && t.getOwner().equals(toDoToMove.getOwner()))) {
                System.err.println("Error: A ToDo with title '" + toDoTitle + "' and same owner already exists in the destination board '" + destinationBoardNameStr + "'.");
                return false;
            }

            // In-memory move
            sourceBoard.removeToDo(toDoToMove); // Removes from source, re-indexes
            destinationBoard.addExistingTodo(toDoToMove); // Adds to destination, re-indexes

            System.out.println("ToDo '" + toDoTitle + "' moved from " + sourceBoardNameStr + " to " + destinationBoardNameStr + " for user '" + this.user.getUsername() + "'.");

            try {
                int sourceBoardId = userDAO.getBoardId(sourceBoardEnumName, user.getUsername());
                int destBoardId = userDAO.getBoardId(destinationBoardEnumName, user.getUsername());

                if (sourceBoardId != -1 && destBoardId != -1) {
                    // Update the board_id of the existing ToDo in the database
                    userDAO.updateToDoBoardId(toDoToMove.getId(), destBoardId);
                } else {
                    System.err.println("Error: Source or Destination board not found in database for moving ToDo.");
                    return false;
                }
            } catch (SQLException e) {
                System.err.println("Database error moving ToDo: " + e.getMessage());
                e.printStackTrace();
                return false;
            }

            return true;
        } else {
            System.err.println("ToDo '" + toDoTitle + "' not found on board " + sourceBoardNameStr + " for moving.");
            return false;
        }
    }


    /**
     * Checks if the currently logged-in user is the creator (owner) of the given ToDo.
     * This is essential for controlling edit/delete permissions.
     * @param toDo The ToDo object to check.
     * @return true if the current user created the ToDo, false otherwise.
     */
    public boolean isCurrentUserToDoCreator(ToDo toDo) {
        if (this.user == null || toDo == null || toDo.getOwner() == null) {
            return false;
        }
        return this.user.getUsername().equals(toDo.getOwner());
    }

    /**
     * Retrieves a list of all registered usernames, excluding the currently logged-in user.
     * This method interacts with the UserDAO to get all users from the database.
     * @return An ArrayList of usernames (Strings).
     */
    public ArrayList<String> getAllUsernamesExcludingCurrentUser() {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to get other usernames.");
            return new ArrayList<>();
        }

        try {
            // Fetch all users from the database using UserDAO
            ArrayList<User> allUsers = userDAO.getAllUsers();

            // Filter out the current user's username
            return allUsers.stream()
                    .filter(u -> !u.getUsername().equals(user.getUsername()))
                    .map(User::getUsername)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (SQLException e) {
            System.err.println("Database error retrieving all users: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Shares a ToDo item from the current user's board to another user's board.
     * A *copy* of the ToDo will be created and added to the recipient's board.
     * The original ToDo (on the creator's board) will keep track of who it was shared with.
     *
     * @param toDoTitle The title of the ToDo to share.
     * @param currentBoardDisplayName The display name of the board where the ToDo currently resides (source board for the creator).
     * @param recipientUsername The username of the user to share the ToDo with.
     * @return true if the ToDo was successfully shared, false otherwise.
     */
    public boolean shareToDo(String toDoTitle, String currentBoardDisplayName, String recipientUsername) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to share a ToDo.");
            return false;
        }

        // 1. Get the original ToDo from the current user's board (the creator's board)
        BoardName sourceBoardEnumName;
        try {
            sourceBoardEnumName = BoardName.fromDisplayName(currentBoardDisplayName);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid source board name '" + currentBoardDisplayName + "'. " + e.getMessage());
            return false;
        }

        Board sourceBoard = user.getBoard(sourceBoardEnumName);
        if (sourceBoard == null) {
            System.err.println("Error: Source Board '" + currentBoardDisplayName + "' not found for current user.");
            return false;
        }

        Optional<ToDo> optionalOriginalToDoToShare = sourceBoard.getTodoList().stream()
                .filter(t -> t.getTitle().equals(toDoTitle))
                .findFirst();

        if (optionalOriginalToDoToShare.isEmpty()) {
            System.err.println("Error: ToDo '" + toDoTitle + "' not found in board '" + currentBoardDisplayName + "' for sharing.");
            return false;
        }
        ToDo originalToDoToShare = optionalOriginalToDoToShare.get();

        // Critical check: Only the creator can share their ToDo.
        if (!isCurrentUserToDoCreator(originalToDoToShare)) {
            System.err.println("Permission Denied: Only the creator of a ToDo can share it. You are not the owner of '" + toDoTitle + "'.");
            return false;
        }


        // 2. Get the recipient user from the database and ensure their boards are loaded
        Optional<User> optionalRecipientUser;
        try {
            optionalRecipientUser = userDAO.getUserByUsername(recipientUsername);
        } catch (SQLException e) {
            System.err.println("Database error fetching recipient user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        if (optionalRecipientUser.isEmpty()) {
            System.err.println("Error: Recipient user '" + recipientUsername + "' not found.");
            return false;
        }
        User recipientUser = optionalRecipientUser.get();

        // Load the recipient's boards and their ToDos from the database if not already loaded
        try {
            userDAO.loadUserBoardsAndToDos(recipientUser);
        } catch (SQLException e) {
            System.err.println("Database error loading recipient's boards and ToDos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        // 3. Determine the target board for the recipient (same board name as source)
        BoardName targetBoardEnumName = sourceBoardEnumName; // Shared to the same named board in recipient's account

        Board recipientBoard = recipientUser.getBoard(targetBoardEnumName);
        if (recipientBoard == null) {
            System.err.println("Error: Recipient user '" + recipientUsername + "' does not have a board named '" + currentBoardDisplayName + "'. Cannot share ToDo.");
            // Optionally, you might create the board for them here if it doesn't exist.
            // For now, let's assume boards exist.
            return false;
        }

        // 4. Check if a ToDo with the *same original ID* or *same title/owner* already exists in the recipient's target board.
        // This prevents multiple identical shared copies of the *same logical ToDo* on the recipient's board.
        // If you had an `originalId` field in ToDo, this would be `t.getOriginalId().equals(originalToDoToShare.getId())`.
        // Since we don't, we check by title and original owner.
        if (recipientBoard.getTodoList().stream().anyMatch(t -> t.getTitle().equals(originalToDoToShare.getTitle()) && t.getOwner().equals(originalToDoToShare.getOwner()))) {
            System.out.println("ToDo '" + toDoTitle + "' from '" + originalToDoToShare.getOwner() + "' already exists in '" + recipientUsername + "'s board '" + currentBoardDisplayName + "'. Sharing aborted.");
            return false;
        }

        // 5. Use the Board class's shareTodo method to handle in-memory logic
        // This method creates a COPY of the ToDo for the recipient, adds it to their board,
        // and updates the original ToDo's 'users' set.
        sourceBoard.shareTodo(recipientUser, originalToDoToShare); // Pass the ORIGINAL ToDo

        // 6. Persist the new ToDo in the database for the recipient
        // The `Board.shareTodo` method added `newSharedToDo` to `recipientBoard` in memory.
        // Now find that new shared ToDo instance to save it to DB.
        Optional<ToDo> newlySharedToDoOptional = recipientBoard.getTodoList().stream()
                .filter(t -> t.getTitle().equals(originalToDoToShare.getTitle()) &&
                        t.getOwner().equals(originalToDoToShare.getOwner()) &&
                        !t.getId().equals(originalToDoToShare.getId())) // Ensure it's the newly copied instance
                .findFirst();

        if (newlySharedToDoOptional.isPresent()) {
            ToDo newlySharedToDo = newlySharedToDoOptional.get();
            try {
                int recipientBoardId = userDAO.getBoardId(targetBoardEnumName, recipientUser.getUsername());
                if (recipientBoardId != -1) {
                    userDAO.saveToDo(newlySharedToDo, recipientBoardId); // Save the new, unique instance
                    System.out.println("ToDo '" + toDoTitle + "' (ID: " + newlySharedToDo.getId() + ") successfully saved to database for '" + recipientUsername + "' in board '" + currentBoardDisplayName + "'.");

                    // Update the original ToDo's shared_with relationship in DB (if you have one)
                    // For now, assume this is handled by UserDAO.saveToDo, or needs a separate method.
                    // If you add a dedicated 'shared_todos' table, you'd insert a record here.

                    return true;
                } else {
                    System.err.println("Error: Recipient board not found in database for saving shared ToDo.");
                    // Revert in-memory addition if DB save fails
                    recipientBoard.removeToDo(newlySharedToDo);
                    originalToDoToShare.removeUser(recipientUser); // Also remove from original ToDo's tracking
                    return false;
                }
            } catch (SQLException e) {
                System.err.println("Database error sharing ToDo with '" + recipientUsername + "': " + e.getMessage());
                e.printStackTrace();
                // Revert in-memory addition if DB save fails
                recipientBoard.removeToDo(newlySharedToDo);
                originalToDoToShare.removeUser(recipientUser); // Also remove from original ToDo's tracking
                return false;
            }
        } else {
            System.err.println("Internal Error: Could not find the newly shared ToDo in recipient's board after in-memory addition.");
            return false;
        }
    }


    /**
     * Unshares a ToDo by removing it from a specific recipient's board.
     * This only removes the shared copy, it does not affect the original ToDo.
     * This method can only be called by the creator of the original ToDo.
     *
     * @param toDoTitle The title of the original ToDo that was shared.
     * @param boardNameStr The display name of the board from which the ToDo was originally shared.
     * @param recipientUsername The username of the user from whom to unshare the ToDo.
     * @return true if the shared ToDo was successfully removed, false otherwise.
     */
    public boolean unshareToDo(String toDoTitle, String boardNameStr, String recipientUsername) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to unshare a ToDo.");
            return false;
        }

        // 1. Get the original ToDo from the current user's board (the creator's board)
        BoardName sourceBoardEnumName;
        try {
            sourceBoardEnumName = BoardName.fromDisplayName(boardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid source board name '" + boardNameStr + "'. " + e.getMessage());
            return false;
        }

        Board sourceBoard = user.getBoard(sourceBoardEnumName);
        if (sourceBoard == null) {
            System.err.println("Error: Source Board '" + boardNameStr + "' not found for current user.");
            return false;
        }

        Optional<ToDo> optionalOriginalToDo = sourceBoard.getTodoList().stream()
                .filter(t -> t.getTitle().equals(toDoTitle))
                .findFirst();

        if (optionalOriginalToDo.isEmpty()) {
            System.err.println("Error: Original ToDo '" + toDoTitle + "' not found in your board '" + boardNameStr + "'.");
            return false;
        }
        ToDo originalToDo = optionalOriginalToDo.get();

        // Permission check: Only the creator can unshare their ToDo.
        if (!isCurrentUserToDoCreator(originalToDo)) {
            System.err.println("Permission Denied: Only the creator of a ToDo can unshare it. You are not the owner of '" + toDoTitle + "'.");
            return false;
        }


        // 2. Get the recipient user from the database and ensure their boards are loaded
        Optional<User> optionalRecipientUser;
        try {
            optionalRecipientUser = userDAO.getUserByUsername(recipientUsername);
        } catch (SQLException e) {
            System.err.println("Database error fetching recipient user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        if (optionalRecipientUser.isEmpty()) {
            System.err.println("Error: Recipient user '" + recipientUsername + "' not found.");
            return false;
        }
        User recipientUser = optionalRecipientUser.get();

        try {
            userDAO.loadUserBoardsAndToDos(recipientUser); // Ensure recipient's boards are loaded
        } catch (SQLException e) {
            System.err.println("Database error loading recipient's boards and ToDos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        // 3. Get the recipient's board (same name as the original board)
        Board recipientBoard = recipientUser.getBoard(sourceBoardEnumName);
        if (recipientBoard == null) {
            System.out.println("Recipient user '" + recipientUsername + "' does not have a board named '" + boardNameStr + "'. ToDo already unshared or never shared to this board.");
            originalToDo.removeUser(recipientUser); // Clean up the original ToDo's tracking if the board is gone
            return true; // Consider it successfully "unshared" if there's no board
        }

        // 4. Find the specific shared copy on the recipient's board
        // This is where having an `original_todo_id` in `ToDo` would make this much more robust.
        // Without it, we have to rely on title and original owner matching.
        Optional<ToDo> sharedCopyToRemoveOptional = recipientBoard.getTodoList().stream()
                .filter(t -> t.getTitle().equals(toDoTitle) && t.getOwner().equals(originalToDo.getOwner()))
                .findFirst();

        if (sharedCopyToRemoveOptional.isEmpty()) {
            System.out.println("ToDo '" + toDoTitle + "' (from " + originalToDo.getOwner() + ") not found on '" + recipientUsername + "'s board '" + boardNameStr + "'. Already unshared or never shared.");
            originalToDo.removeUser(recipientUser); // Clean up the original ToDo's tracking
            return true; // Consider it successfully "unshared"
        }

        ToDo sharedCopyToRemove = sharedCopyToRemoveOptional.get();

        // 5. Remove the shared copy from the recipient's board (in-memory)
        recipientBoard.removeToDo(sharedCopyToRemove); // This removes the specific instance from the recipient's board

        // 6. Remove the shared copy from the database
        try {
            userDAO.deleteToDo(sharedCopyToRemove.getId()); // Delete only this specific instance from the DB
            System.out.println("Shared ToDo '" + toDoTitle + "' successfully unshared from '" + recipientUsername + "' and deleted from database.");
            originalToDo.removeUser(recipientUser); // Remove the user from the original ToDo's tracking list
            return true;
        } catch (SQLException e) {
            System.err.println("Database error unsharing ToDo from '" + recipientUsername + "': " + e.getMessage());
            e.printStackTrace();
            // If DB deletion fails, you might want to re-add it to recipient's board in memory
            recipientBoard.addExistingTodo(sharedCopyToRemove); // Revert in-memory change
            return false;
        }
    }

    /**
     * Retrieves the list of users with whom a specific ToDo has been shared.
     * This relies on the 'users' Set within the ToDo object.
     *
     * @param toDoTitle The title of the ToDo to query.
     * @param boardNameStr The display name of the board where the original ToDo resides.
     * @return An ArrayList of usernames (Strings) that the ToDo has been shared with.
     */
    public ArrayList<String> getUsersToDoIsSharedWith(String toDoTitle, String boardNameStr) {
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
            System.err.println("Error: Board '" + boardNameStr + "' not found for current user.");
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