package models.board;

import models.ToDo;
import models.User;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Board {
    private final BoardName name;
    private String description;
    private final String owner;
    private ArrayList<ToDo> todoList;

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

    public void addTodo(ToDo todo) {
        this.todoList.add(todo);
    }

    public void deleteTodo(ToDo todo) {
        todoList.remove(todo);

        //handle boards of other users where the to do is shared
        Set<User> users = todo.getUsers();
        if(!users.isEmpty()) { //if the list is not empty, the to do is shared (recursive case)
            for (User user : users) {
                Board board = user.getBoard(this.name);
                board.deleteTodo(todo);
            }
        }
    }

    public ArrayList<ToDo> getTodoList() {
        return new ArrayList<>(todoList);
    }

    public ArrayList<String> getTodoTitles(){
        ArrayList<String> names = new ArrayList<>();
        for(int i = 0; i < todoList.size(); i++){
            names.add(todoList.get(i).getTitle());
        }
        return names;
    }

    public BoardName getName() {
        return name;
    }

    public void sortDueDate(){
        LocalDate today = LocalDate.now();
        ArrayList<ToDo> filterList = new ArrayList<>();
        for(ToDo todo:todoList){
            if(todo.getDueDate().equals(today)){
                System.out.println(todo.getTitle());
            }
        }
    }

    public void sortDueDate(LocalDate dueDate){
        ArrayList<ToDo> filterList = new ArrayList<>();
        for(ToDo todo:todoList){
            if(todo.getDueDate().equals(dueDate)){
                System.out.println(todo.getTitle());
            }
        }
    }

    public void SearchTitle(String title){
        ArrayList<ToDo> filterList = new ArrayList<>();
        for(ToDo todo:todoList) {
            if (todo.getTitle().equals(title)) {
                System.out.println(todo.getTitle());
            }
        }
    }
}