package controller;

import models.*;
import models.board.Board;
import models.board.BoardName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

public class Controller {
    public User user;


    public void register(String username, String password){
        this.user = new User(username, password);
    }

    public String addToDo(String boardName, String toDoName, String description, String date, String url){

        String formattedBoardName = boardName.toUpperCase();
        if(formattedBoardName.equals("FREE TIME")){
            formattedBoardName = "FREE_TIME";
        }

        Board board = user.getBoard(BoardName.valueOf(formattedBoardName));
        ToDo toDo = board.addTodo(toDoName);

        toDo.setDescription(description);
        toDo.setUrl(url);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);
        toDo.setDueDate(localDate);

        return toDo.getId().toString();
    }

    public void updateToDo(String boardName, String oldToDoTitle, String newToDoTitle, String description, String date, String url) {
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

    public void addActivity(String boardName, String activityName){

        System.out.println("Controller.addActivity called. This method might be redundant with new GUI logic.");

    }


}