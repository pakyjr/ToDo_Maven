package controller;

import models.*;
import models.board.Board;
import models.board.BoardName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class Controller {
    public User user;

    public void register(String username, String password){
        this.user = new User(username, password);
    }

    public String addToDo(String boardName, String toDoName, String description, String date, String url, String color, String image, Map<String, Boolean> activities, String status){
        String formattedBoardName = boardName.toUpperCase();
        if(formattedBoardName.equals("FREE TIME")){
            formattedBoardName = "FREE_TIME";
        }

        Board board = user.getBoard(BoardName.valueOf(formattedBoardName));

        if (board == null) {
            System.err.println("Error: Board '" + formattedBoardName + "' not found for user.");
            return null; // Or throw an exception
        }

        ToDo toDo = board.addTodo(toDoName); // ToDo is created here with default values
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

        return toDo.getId().toString();
    }

    public void updateToDo(String boardName, String oldToDoTitle, String newToDoTitle, String description, String date, String url, String color, String image, Map<String, Boolean> activities, String status) {
        String formattedBoardName = boardName.toUpperCase();
        if(formattedBoardName.equals("FREE TIME")){
            formattedBoardName = "FREE_TIME";
        }

        Board board = user.getBoard(BoardName.valueOf(formattedBoardName));
        if (board == null) {
            System.err.println("Error: Board '" + formattedBoardName + "' not found for user.");
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

        } else {
            System.err.println("ToDo with title '" + oldToDoTitle + "' not found on board " + boardName);
        }
    }

    public ToDo getToDoByTitle(String title, BoardName boardName){
        Board board = user.getBoard(boardName);
        if (board == null) {
            System.err.println("Board " + boardName + " not found for user.");
            return null;
        }

        for(ToDo toDo : board.getTodoList()){
            if(toDo.getTitle().equals(title)){
                return new ToDo(toDo);
            }
        }
        return null; // ToDo not found
    }

    public ArrayList<String> getToDoListString(BoardName boardName){
        Board board = user.getBoard(boardName);
        ArrayList<String> resultTitles = new ArrayList<>();
        if (board != null) {
            for(ToDo toDo : board.getTodoList()) {
                resultTitles.add(toDo.getTitle());
            }
        }
        return resultTitles;
    }

    public void deleteToDo(BoardName boardName, String toDoTitle) {
        Board board = user.getBoard(boardName);
        if (board == null) {
            System.err.println("Board " + boardName + " not found for user.");
            return;
        }

        // Find the ToDo object to remove
        Optional<ToDo> toDoToRemoveOptional = board.getTodoList().stream()
                .filter(toDo -> toDo.getTitle().equals(toDoTitle))
                .findFirst();

        if (toDoToRemoveOptional.isPresent()) {
            ToDo toDoToRemove = toDoToRemoveOptional.get();
            board.removeToDo(toDoToRemove);
            System.out.println("ToDo '" + toDoTitle + "' deleted from board " + boardName);
        } else {
            System.err.println("ToDo '" + toDoTitle + "' not found on board " + boardName + " for deletion.");
        }
    }

    /**
     * Moves a ToDo item from a source board to a destination board.
     * @param toDoTitle The title of the ToDo to move.
     * @param sourceBoardName The name of the board the ToDo is currently on.
     * @param destinationBoardName The name of the board to move the ToDo to.
     * @return true if the ToDo was successfully moved, false otherwise.
     */
    public boolean moveToDo(String toDoTitle, BoardName sourceBoardName, BoardName destinationBoardName) {
        Board sourceBoard = user.getBoard(sourceBoardName);
        Board destinationBoard = user.getBoard(destinationBoardName);

        if (sourceBoard == null) {
            System.err.println("Error: Source Board '" + sourceBoardName + "' not found.");
            return false;
        }
        if (destinationBoard == null) {
            System.err.println("Error: Destination Board '" + destinationBoardName + "' not found.");
            return false;
        }
        if (sourceBoardName.equals(destinationBoardName)) {
            System.out.println("Cannot move ToDo to the same board.");
            return false;
        }

        Optional<ToDo> optionalToDo = sourceBoard.getTodoList().stream()
                .filter(t -> t.getTitle().equals(toDoTitle))
                .findFirst();

        if (optionalToDo.isPresent()) {
            ToDo toDoToMove = optionalToDo.get();

            if (destinationBoard.getTodoList().stream().anyMatch(t -> t.getTitle().equals(toDoTitle))) {
                System.err.println("Error: A ToDo with title '" + toDoTitle + "' already exists in the destination board '" + destinationBoardName + "'.");
                return false;
            }

            sourceBoard.removeToDo(toDoToMove);

            destinationBoard.addExistingTodo(toDoToMove);

            System.out.println("ToDo '" + toDoTitle + "' moved from " + sourceBoardName + " to " + destinationBoardName);
            return true;
        } else {
            System.err.println("ToDo '" + toDoTitle + "' not found on board " + sourceBoardName + " for moving.");
            return false;
        }
    }
}