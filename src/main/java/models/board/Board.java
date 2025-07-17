package models.board;

import models.ToDo;
import java.util.*;

public class Board {
    private int id;
    private final BoardName name;
    private String description;
    private final String owner;
    private String color;
    private final List<ToDo> todoList;

    public Board(BoardName name, String owner) {
        this.name = name;
        this.owner = owner;
        this.todoList = new ArrayList<>();
        this.color = "Default";
        this.id = 0;
    }

    public Board(BoardName name, String owner, String description) {
        this.name = name;
        this.owner = owner;
        this.description = description;
        this.todoList = new ArrayList<>();
        this.color = "Default";
        this.id = 0;
    }

    public Board(int id, BoardName name, String owner, String description, String color) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.description = description;
        this.color = color;
        this.todoList = new ArrayList<>();
    }

    public Board(int id, BoardName name, String owner, String color) {
        this(id, name, owner, null, color);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ToDo addTodo(String title) {
        return addTodo(title, this.owner);
    }

    public ToDo addTodo(String title, String owner) {
        if (todoList.stream().anyMatch(todo -> todo.getTitle().equals(title) && todo.getOwner().equals(owner))) {
            System.err.println("Error: A ToDo with title '" + title + "' by owner '" + owner + "' already exists on board " + this.name);
            return null;
        }

        ToDo todo = new ToDo(title, owner);
        // todo.setOwner(owner); // This line is no longer needed after fixing the constructor call
        todoList.add(todo);
        todo.setPosition(todoList.size());
        return todo;
    }

    public void addExistingTodo(ToDo existingTodo) {
        if (todoList.stream().anyMatch(todo -> todo.getId().equals(existingTodo.getId()))) {
            System.err.println("Error: ToDo with ID '" + existingTodo.getId() + "' already exists on board " + this.name);
            return;
        }
        todoList.add(existingTodo);
    }

    public void removeToDo(ToDo toDoToRemove) {
        boolean removed = todoList.remove(toDoToRemove);
        if (removed) {
            for (int i = 0; i < todoList.size(); i++) {
                todoList.get(i).setPosition(i + 1);
            }
            System.out.println("ToDo '" + toDoToRemove.getTitle() + "' removed from board '" + name + "'.");

            if (this.owner.equals(toDoToRemove.getOwner())) {
                System.out.println("This ToDo was created by the current user. Clearing shared users list...");
                toDoToRemove.clearUsers();
            }
        } else {
            System.out.println("ToDo '" + toDoToRemove.getTitle() + "' not found on board '" + name + "'.");
        }
    }

    public List<ToDo> getTodoList() {
        return new ArrayList<>(todoList);
    }

    public BoardName getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return id == board.id && name == board.name && Objects.equals(owner, board.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, owner);
    }
}