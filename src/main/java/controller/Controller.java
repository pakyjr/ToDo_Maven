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

    public Controller(){

    }

    public void register(String username, String password){
        this.user = new User(username, password);
    }

    public String addToDo(String boardName, String toDoName, String description, String date, String url){
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
        return toDo.getId().toString();
    }

    public ArrayList<String> toDoStringList(BoardName board){

            ArrayList<String> list = new ArrayList<>();
            for(int i = 0; i < user.getBoard(board).getTodoList().size(); i++){
                list.add(user.getBoard(board).getTodoList().get(i).getTitle());
            }
            return list;

    }

    public ToDo getToDoByTitle(String title, BoardName board){
        ToDo toDo = null;
        for(int i = 0; i < user.getBoard(board).getTodoList().size(); i++){

            if(user.getBoard(board).getTodoList().get(i).getTitle().equals(title))
                toDo = new ToDo(user.getBoard(board).getTodoList().get(i));

        }
        return toDo;
    }

    public ArrayList<String> getToDoListString(BoardName board){
        ArrayList<String> toResult = new ArrayList<>();
        for(int i = 0; i < user.getBoard(board).getTodoList().size(); i++)
            toResult.add(user.getBoard(board).getTodoList().get(i).getTitle());
        return toResult;
    }

    public void addActivity(String boardName, String activityName){}
    //todo qua dobbiamo salvare nel db
}
