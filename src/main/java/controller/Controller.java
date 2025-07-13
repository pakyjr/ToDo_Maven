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

    // Now correctly accepts color, image, activities, and status
    public String addToDo(String boardName, String toDoName, String description, String date, String url, String color, String image, Map<String, Boolean> activities, String status){
        String formattedBoardName = boardName.toUpperCase();
        if(formattedBoardName.equals("FREE TIME")){
            formattedBoardName = "FREE_TIME";
        }

        Board board = user.getBoard(BoardName.valueOf(formattedBoardName));
        ToDo toDo = board.addTodo(toDoName); // ToDo is created here with default values

        toDo.setDescription(description);
        toDo.setUrl(url);
        toDo.setColor(color);
        toDo.setImage(image);

        // Set activities and status directly. ToDo's setActivityList will handle updateOverallStatus.
        toDo.setActivityList(activities);
        toDo.setStatus(status);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);
        toDo.setDueDate(localDate);

        return toDo.getId().toString();
    }

    // Now correctly accepts all fields for update
    public void updateToDo(String boardName, String oldToDoTitle, String newToDoTitle, String description, String date, String url, String color, String image, Map<String, Boolean> activities, String status) {
        String formattedBoardName = boardName.toUpperCase();
        if(formattedBoardName.equals("FREE TIME")){
            formattedBoardName = "FREE_TIME";
        }

        Board board = user.getBoard(BoardName.valueOf(formattedBoardName));
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

            // Set activities and status. ToDo's setActivityList will handle updateOverallStatus.
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
                return new ToDo(toDo); // Return a copy
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

    // New method to delete a ToDo
    public void deleteToDo(BoardName boardName, String toDoTitle) {
        Board board = user.getBoard(boardName);
        if (board == null) {
            System.err.println("Board " + boardName + " not found for user.");
            return;
        }

        // Find the ToDo by title and remove it from the board's todoList
        // The removeIf method is efficient for this.
        boolean removed = board.getTodoList().removeIf(toDo -> toDo.getTitle().equals(toDoTitle));

        if (removed) {
            System.out.println("ToDo '" + toDoTitle + "' deleted from board " + boardName);
        } else {
            System.err.println("ToDo '" + toDoTitle + "' not found on board " + boardName + " for deletion.");
        }
    }


    // This method is redundant and can be safely removed.
    // public void addActivity(String boardName, String activityName){
    //     System.out.println("Controller.addActivity called. This method is likely redundant with new GUI logic and can be removed.");
    // }
}