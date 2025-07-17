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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Controller {
    public User user;
    private UserDAO userDAO;

    public Controller() throws SQLException {
        this.userDAO = new UserDAOImpl();
    }

    public void register(String username, String plainPassword){
        User newUser = new User(username, plainPassword);

        try {
            boolean success = userDAO.saveUser(newUser);
            if (success) {
                this.user = newUser;

                newUser.fillBoard(newUser.getUsername());

                for (Board board : newUser.getBoardList()) {
                    userDAO.saveBoard(board, newUser.getId());
                }

                System.out.println("User '" + username + "' registered successfully and default boards created.");
            } else {
                System.err.println("Registration failed: User '" + username + "' might already exist.");
                this.user = null;
            }
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
            this.user = null;
        }
    }

    public User login(String username, String plainPassword) throws SQLException {
        Optional<User> optionalUser = userDAO.getUserByUsername(username);

        if (optionalUser.isPresent()) {
            User foundUser = optionalUser.get();
            if (foundUser.checkPassword(plainPassword)) {
                this.user = foundUser;
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

    public String addToDo(String boardNameStr, String toDoName, String description, String date, String url, String color, String image, Map<String, Boolean> activities, String status, String owner){
        if (this.user == null) {
            System.err.println("Error: No user is logged in to add a ToDo.");
            return null;
        }

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

        ToDo toDo = board.addTodo(toDoName, owner);
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
            return null;
        }

        try {
            int boardId = userDAO.getBoardId(boardEnumName, user.getUsername());
            if (boardId != -1) {
                userDAO.saveToDo(toDo, boardId);
                System.out.println("ToDo '" + toDoName + "' added successfully to board '" + boardNameStr + "'.");
            } else {
                System.err.println("Board not found in database for saving ToDo.");
                board.removeToDo(toDo);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Database error saving ToDo: " + e.getMessage());
            e.printStackTrace();
            board.removeToDo(toDo);
            return null;
        }

        return toDo.getId().toString();
    }

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

            if (!isCurrentUserToDoCreator(toDoToUpdate)) {
                System.err.println("Permission Denied: User '" + this.user.getUsername() + "' is not the owner of ToDo '" + oldToDoTitle + "'. Update aborted.");
                return;
            }

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
                return;
            }

            try {
                int boardId = userDAO.getBoardId(boardEnumName, user.getUsername());
                if (boardId != -1) {
                    userDAO.updateToDo(toDoToUpdate, boardId);
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

            boolean isCreator = isCurrentUserToDoCreator(toDoToRemove);
            boolean isRecipientDeleting = !isCreator && this.user.getBoard(boardEnumName).getTodoList().contains(toDoToRemove);

            if (!isCreator && !isRecipientDeleting) {
                System.err.println("Permission Denied: You cannot delete this ToDo.");
                return;
            }

            try {

                if (isCreator) {
                    userDAO.removeAllToDoSharing(toDoToRemove.getId().toString());
                    System.out.println("Removed all shared instances of ToDo '" + toDoTitle + "'.");
                }

                userDAO.deleteToDo(toDoToRemove.getId().toString(), user.getUsername());
                board.removeToDo(toDoToRemove);
                System.out.println("ToDo '" + toDoTitle + "' deleted successfully from board '" + boardNameStr + "'.");

                if (isRecipientDeleting) {

                }

            } catch (SQLException e) {
                System.err.println("Database error deleting ToDo: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("ToDo with title '" + toDoTitle + "' not found on board " + boardNameStr);
        }
    }

    public boolean moveToDo(String toDoTitle, String currentBoardDisplayName, String destinationBoardDisplayName) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to move a ToDo.");
            return false;
        }

        BoardName currentBoardEnum;
        BoardName destinationBoardEnum;
        try {
            currentBoardEnum = BoardName.fromDisplayName(currentBoardDisplayName);
            destinationBoardEnum = BoardName.fromDisplayName(destinationBoardDisplayName);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name provided. " + e.getMessage());
            return false;
        }

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

        Optional<ToDo> optionalToDo = currentBoard.getTodoList().stream()
                .filter(t -> t.getTitle().equals(toDoTitle))
                .findFirst();

        if (!optionalToDo.isPresent()) {
            System.err.println("Error: ToDo '" + toDoTitle + "' not found on board '" + currentBoardDisplayName + "'.");
            return false;
        }
        ToDo toDoToMove = optionalToDo.get();

        if (!isCurrentUserToDoCreator(toDoToMove)) {
            System.err.println("Permission Denied: Only the creator can move this ToDo.");
            return false;
        }

        if (destinationBoard.getTodoList().stream().anyMatch(t -> t.getTitle().equals(toDoTitle) && t.getOwner().equals(toDoToMove.getOwner()))) {
            System.err.println("Error: A ToDo with title '" + toDoTitle + "' and same owner already exists on destination board '" + destinationBoardDisplayName + "'. Move aborted.");
            return false;
        }

        try {

            int currentBoardId = userDAO.getBoardId(currentBoardEnum, user.getUsername()); // Not strictly needed for moving, but good for consistency check
            int destinationBoardId = userDAO.getBoardId(destinationBoardEnum, user.getUsername());

            if (currentBoardId == -1 || destinationBoardId == -1) {
                System.err.println("Database error: Could not find board IDs for move operation.");
                return false;
            }

            userDAO.updateToDoBoardId(toDoToMove.getId().toString(), destinationBoardId);

            currentBoard.removeToDo(toDoToMove);
            destinationBoard.addExistingTodo(toDoToMove);

            System.out.println("ToDo '" + toDoTitle + "' successfully moved from '" + currentBoardDisplayName + "' to '" + destinationBoardDisplayName + "'.");
            return true;

        } catch (SQLException e) {
            System.err.println("Database error moving ToDo '" + toDoTitle + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

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
            return true;
        }

        boolean allSuccess = true;
        for (String username : usernamesToShareWith) {
            try {

                userDAO.shareToDo(toDo.getId().toString(), username);

                Optional<User> sharedUserOptional = userDAO.getUserByUsername(username);
                if (sharedUserOptional.isPresent()) {
                    toDo.addSharedUser(sharedUserOptional.get());
                    System.out.println("ToDo '" + toDo.getTitle() + "' shared with user '" + username + "'.");
                } else {
                    System.err.println("User '" + username + "' not found to add to in-memory ToDo object.");
                    allSuccess = false;
                }

            } catch (SQLException e) {
                System.err.println("Database error sharing ToDo '" + toDo.getTitle() + "' with '" + username + "': " + e.getMessage());
                e.printStackTrace();
                allSuccess = false;
            }
        }
        return allSuccess;
    }

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
            return true;
        }

        boolean allSuccess = true;
        for (String username : usernamesToRemoveSharing) {
            try {

                userDAO.removeToDoSharing(toDo.getId().toString(), username);

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