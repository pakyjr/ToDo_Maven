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
        // Prevent adding a ToDo if a duplicate title already exists on this board
        if (todoList.stream().anyMatch(todo -> todo.getTitle().equals(title))) {
            System.err.println("Error: A ToDo with title '" + title + "' already exists on board " + this.name);
            return null;
        }

        ToDo todo = new ToDo(title);
        todo.setOwner(owner);
        todoList.add(todo);

        todo.setPosition(todoList.size());

        return todo;
    }

    public void addExistingTodo(ToDo todo) {

        if (todoList.stream().anyMatch(existingTodo -> existingTodo.getTitle().equals(todo.getTitle()))) {
            System.err.println("Warning: ToDo with title '" + todo.getTitle() + "' already exists on board " + this.name + ". Not adding duplicate.");
            return;
        }
        todoList.add(todo);
        todo.setPosition(todoList.size());
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
            System.out.println("Invalid position: " + newPosition + ". Must be between 1 and " + todoList.size());
            return;
        }

        // Find the actual index of the todo in the list based on the object reference
        int oldIndex = -1;
        for (int i = 0; i < todoList.size(); i++) {
            if (todoList.get(i) == todo) {
                oldIndex = i;
                break;
            }
        }

        if (oldIndex == -1) {
            System.out.println("ToDo not found in this board's list: " + todo.getTitle());
            return;
        }

        int oldPosition = todo.getPosition();
        if (oldPosition == newPosition) {
            return;
        }

        // Remove the todo from its old position and re-insert at the new position
        todoList.remove(oldIndex);
        todoList.add(newPosition - 1, todo);


        for (int i = 0; i < todoList.size(); i++) {
            todoList.get(i).setPosition(i + 1);
        }
    }

    public void removeToDo(ToDo todo) {
        if (!todoList.contains(todo)) {
            System.err.println("Error: ToDo '" + todo.getTitle() + "' not found on board " + this.name + " for removal.");
            return;
        }

        int positionRemoved = todo.getPosition();
        todoList.remove(todo);

        for (ToDo item : todoList) {
            if (item.getPosition() > positionRemoved) {
                item.setPosition(item.getPosition() - 1);
            }
        }

        Set<User> usersWithSharedTodo = todo.getUsers();
        if (usersWithSharedTodo != null && !usersWithSharedTodo.isEmpty()) {
            for (User user : usersWithSharedTodo) {

                if (!user.getBoard(this.name).equals(this)) {
                    Board board = user.getBoard(this.name);
                    if (board != null && board.getTodoList().contains(todo)) {
                        board.removeToDo(todo);
                    }
                }
            }
        }

        if (this.owner.equals(todo.getOwner())) {
            todo.clearUsers();
        }
    }

    public void deleteTodoFromThisBoardOnly(ToDo todo) {
        if (!todoList.contains(todo)) {
            System.err.println("Error: ToDo '" + todo.getTitle() + "' not found on board " + this.name + " for removal.");
            return;
        }

        int positionRemoved = todo.getPosition();
        todoList.remove(todo);

        for (ToDo item : todoList) {
            if (item.getPosition() > positionRemoved) {
                item.setPosition(item.getPosition() - 1);
            }
        }
        System.out.println("ToDo '" + todo.getTitle() + "' deleted from board " + this.name + " only.");
    }


    public BoardName getName() {
        return name;
    }

    public List<ToDo> getTodosDueToday() {
        LocalDate today = LocalDate.now();
        return todoList.stream()
                .filter(todo -> todo.getDueDate() != null && todo.getDueDate().equals(today))
                .collect(Collectors.toList());
    }

    public List<ToDo> getTodosByDueDate(LocalDate dueDate) {
        return todoList.stream()
                .filter(todo -> todo.getDueDate() != null && todo.getDueDate().equals(dueDate))
                .collect(Collectors.toList());
    }

    public List<ToDo> searchTodosByTitle(String title) {
        return todoList.stream()
                .filter(todo -> todo.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return name == board.name && Objects.equals(owner, board.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, owner);
    }
}