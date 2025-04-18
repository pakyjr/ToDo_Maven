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

    public ArrayList<ToDo> getTodoList() {
        return todoList;
    }

    public Set<String> getAllowedUsers() {
        return users;
    }

    public void addUser(String user_id){
        this.users.add(user_id);
    }

    public void changePosition(ToDo todo, int newPosition){
        int oldPosition = todo.getPosition();
        todo.setPosition(newPosition);

        int i = 0;
        for(ToDo item : todoList){
            if(i >= newPosition - 1 && i < oldPosition - 1){
                item.setPosition(item.getPosition() + 1);
            }
            i++;
        }

        todoList.sort(Comparator.comparingInt(ToDo::getPosition));
    }

    public ToDo addTodo(ToDo todo){
        todoList.add(todo);

        int listSize = todoList.size();
        todo.setPosition(listSize);

        return todo;
    }

    public void deleteTodo(ToDo todo){
        //1) Handle users
        //2) Handle position
        int position = todo.getPosition();
        ToDo T = todoList.get(position-1);
        todoList.remove(position-1);
    }
}