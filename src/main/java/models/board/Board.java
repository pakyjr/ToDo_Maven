package models.board;

import models.ToDo;
import models.User;

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

    public List<ToDo> getTodoList() {
        return new ArrayList<>(todoList);
    }

    public void shareTodo(User guest, ToDo todo) {
        Board guestBoard = guest.getBoard(name);
        if (guestBoard != null) {
            guestBoard.addTodo(todo.getTitle(), guest.getUsername());
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
                // Moving down - shift items in between up
                if (item.getPosition() > oldPosition && item.getPosition() <= newPosition) {
                    item.setPosition(item.getPosition() - 1);
                }
            } else {
                // Moving up - shift items in between down
                if (item.getPosition() >= newPosition && item.getPosition() < oldPosition) {
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
    }

    public BoardName getName() {
        return name;
    }

//    public List<ToDo> getTodosDueOn(Date dueDate) {
//        return todoList.stream()
//                .filter(todo -> {
//                    Date todoDueDate = todo.getDueDate();
//                    return todoDueDate != null && isSameDay(todoDueDate, dueDate);
//                })
//                .collect(Collectors.toList());
//    }
//
//    public List<ToDo> getTodosDueToday() {
//        return getTodosDueOn(new Date());
//    }
//
//    private boolean isSameDay(Date date1, Date date2) {
//        Calendar cal1 = Calendar.getInstance();
//        Calendar cal2 = Calendar.getInstance();
//        cal1.setTime(date1);
//        cal2.setTime(date2);
//        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
//                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
//    }
}