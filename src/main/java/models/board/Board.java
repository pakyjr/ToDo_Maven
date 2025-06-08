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
    private final List<ToDo> todoList;

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

    public ToDo addTodo(String title) {
        return addTodo(title, this.owner);
    }

    public ToDo addTodo(String title, String owner) {
        ToDo todo = new ToDo(title);
        todo.setOwner(owner);
        todoList.add(todo);

        int listSize = todoList.size();
        todo.setPosition(listSize);

        return todo;
    }

    public void addExistingTodo(ToDo todo) {
        todoList.add(todo);
        int listSize = todoList.size();
        todo.setPosition(listSize);
    }

    public ArrayList<ToDo> getTodoList() {
        return new ArrayList<>(todoList);
    }

    public void shareTodo(User guest, ToDo todo) {
        todo.addUser(guest);
        Board guestBoard = guest.getBoard(name);
        if (guestBoard != null) {
            guestBoard.addExistingTodo(todo);
        }
    }

    public void changePosition(ToDo todo, int newPosition) {
        if (newPosition < 1 || newPosition > todoList.size()) {
            System.out.println("Invalid position");
            return;
        }

        int oldPosition = todo.getPosition();
        if (oldPosition == newPosition) {
            return;
        }

        todo.setPosition(newPosition);

        for (int i = 0; i < todoList.size(); i++) {
            ToDo item = todoList.get(i);
            if (oldPosition < newPosition) {
                // Moving down
                if (i > oldPosition - 1 && i <= newPosition - 1) {
                    item.setPosition(item.getPosition() - 1);
                }
            } else {
                // Moving up
                if (i >= newPosition - 1 && i < oldPosition) {
                    item.setPosition(item.getPosition() + 1);
                }
            }
        }

        todoList.sort(Comparator.comparingInt(ToDo::getPosition));
    }

    public void deleteTodo(ToDo todo) {
        int position = todo.getPosition();
        todoList.remove(todo);

        // Update positions of remaining todos
        for (ToDo item : todoList) {
            if (item.getPosition() > position) {
                item.setPosition(item.getPosition() - 1);
            }
        }

        //handle boards of other users where the to do is shared
        Set<User> users = todo.getUsers();
        if(!users.isEmpty()) { //if the list is not empty, the to do is shared (recursive case)
            for (User user : users) {
                Board board = user.getBoard(this.name);
                board.deleteTodo(todo);
            }
        }
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