package controller;

import models.*;
import models.board.Board;
import models.board.BoardName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Controller {
    public User user;

    public Controller(){

    }

    public void register(String username, String password){
        this.user = new User(username, password);
    }

    public void addToDo(String boardName, String toDoName, String description, String date, String url){
        if(boardName.equals("Free Time")){
            boardName = "FREE_TIME";
        }
        Board board = user.getBoard(BoardName.valueOf(boardName.toUpperCase()));
        ToDo toDo = board.addTodo(toDoName);
        toDo.setDescription(description);
        toDo.setUrl(url);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);
        toDo.setDueDate(localDate);
    }
    //todo qua dobbiamo salvare nel db
}
