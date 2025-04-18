package models.board;

import models.ToDo;

import java.util.*;

public class Board {
    private BoardName name;
    private String description;
    private String owner_id;
    private Set<String> users;
    private ArrayList<ToDo> todoList;


    public Board(BoardName name, String owner_id){
        this.name = name;
        this.owner_id = owner_id;
        todoList = new ArrayList<>();
    }

    public Board(BoardName name, String owner_id, String description){
        this.name = name;
        this.description = description;
        this.owner_id = owner_id;
        this.users = new HashSet<>();

        this.users.add(owner_id);
        todoList = new ArrayList<>();
    }

    public Set<String> getAllowedUsers() {
        return users;
    }

    public void addUser(String user_id){
        this.users.add(user_id);
    }

    public void addTodo(ToDo todo){
        todoList.add(todo);

        int listSize = todoList.size();
        todo.setPosition(listSize);
    }

    public void deleteTodo(ToDo todo){

    }





    //il todo deve essere in grado di referenziare la sua bacheca di appartenenza!
    //perche la lista di todo, sta nella bacheca, quindi se il add e il delete sta nel todo, allora il todo deve manipolare la sua bacheca!
    //nella classe todo, se aggiungo un todo alla bacheca, allora la classe todo sta manipolando l'array di todo dentro la bacheca.


}