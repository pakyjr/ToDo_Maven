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

public class Controller {
    public User user;
    private UserDAO userDAO;

    public Controller() {
        this.userDAO = new UserDAOImpl();
    }

    public void register(String username, String plainPassword){
        String hashedPassword = Integer.toHexString(plainPassword.hashCode());
        //DEGUGGING
        System.out.println("DEBUG (Register): Username='" + username + "', PlainPassword='" + plainPassword + "', HashedPassword Generated='" + hashedPassword + "'");
        //DEBUGGING
        User newUser = new User(username, hashedPassword);

        try {
            boolean success = userDAO.saveUser(newUser);
            if (success) {
                this.user = newUser;
                System.out.println("User '" + username + "' registered successfully.");
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
            String hashedPasswordAttempt = Integer.toHexString(plainPassword.hashCode());

            //DEBUGGING
            System.out.println("DEBUG (Login): Username='" + username + "', PlainPassword='" + plainPassword + "', HashedPassword Attempt='" + hashedPasswordAttempt + "'");
            System.out.println("DEBUG (Login): HashedPassword from DB for '" + username + "': '" + foundUser.getHashedPassword() + "'");
            // DEBUGGING

            if (foundUser.getHashedPassword().equals(hashedPasswordAttempt)) {
                this.user = foundUser;
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

    public String addToDo(String boardNameStr, String toDoName, String description, String date, String url, String color, String image, Map<String, Boolean> activities, String status){
        if (this.user == null) {
            System.err.println("Error: No user is logged in to add a ToDo.");
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

        ToDo toDo = board.addTodo(toDoName);
        if (toDo == null) {
            return null;
        }

        toDo.setDescription(description);
        toDo.setUrl(url);
        toDo.setColor(color);
        toDo.setImage(image);
        toDo.setActivityList(activities);
        toDo.setStatus(status);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);
        toDo.setDueDate(localDate);

        try {
            int boardId = userDAO.getBoardId(boardEnumName, user.getUsername());
            if (boardId != -1) {
                userDAO.saveToDo(toDo, boardId);
            } else {
                System.err.println("Board not found in database for saving ToDo.");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Database error saving ToDo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return toDo.getId().toString();
    }

    public void updateToDo(String boardNameStr, String oldToDoTitle, String newToDoTitle, String description, String date, String url, String color, String image, Map<String, Boolean> activities, String status) {
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

            toDoToUpdate.setTitle(newToDoTitle);
            toDoToUpdate.setDescription(description);
            toDoToUpdate.setUrl(url);
            toDoToUpdate.setColor(color);
            toDoToUpdate.setImage(image);
            toDoToUpdate.setActivityList(activities);
            toDoToUpdate.setStatus(status);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(date, formatter);
            toDoToUpdate.setDueDate(localDate);

            try {
                int boardId = userDAO.getBoardId(boardEnumName, user.getUsername());
                if (boardId != -1) {
                    userDAO.updateToDo(toDoToUpdate, boardId);
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
            System.err.println("Board " + boardNameStr + " not found for user.");
            return null;
        }

        for(ToDo toDo : board.getTodoList()){
            if(toDo.getTitle().equals(title)){
                return new ToDo(toDo);
            }
        }
        return null;
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
            board.removeToDo(toDoToRemove);
            System.out.println("ToDo '" + toDoTitle + "' deleted from board " + boardNameStr);
            try {
                userDAO.deleteToDo(toDoToRemove.getId());
            } catch (SQLException e) {
                System.err.println("Database error deleting ToDo: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("ToDo '" + toDoTitle + "' not found on board " + boardNameStr + " for deletion.");
        }
    }

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

            if (destinationBoard.getTodoList().stream().anyMatch(t -> t.getTitle().equals(toDoTitle))) {
                System.err.println("Error: A ToDo with title '" + toDoTitle + "' already exists in the destination board '" + destinationBoardNameStr + "'.");
                return false;
            }

            sourceBoard.removeToDo(toDoToMove);
            destinationBoard.addExistingTodo(toDoToMove);

            System.out.println("ToDo '" + toDoTitle + "' moved from " + sourceBoardNameStr + " to " + destinationBoardNameStr);

            try {
                int sourceBoardId = userDAO.getBoardId(sourceBoardEnumName, user.getUsername());
                int destBoardId = userDAO.getBoardId(destinationBoardEnumName, user.getUsername());
                if (sourceBoardId != -1 && destBoardId != -1) {
                    userDAO.deleteToDo(toDoToMove.getId());
                    userDAO.saveToDo(toDoToMove, destBoardId);
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
}