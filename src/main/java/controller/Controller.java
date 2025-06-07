package controller;
import models.*;
import models.board.Board;
import models.board.BoardName;

import java.util.ArrayList;
import java.util.Comparator;

public class Controller {
    private ArrayList<User> users;
    private ArrayList<Board> board;

    public Controller() {
        this.users = new ArrayList<>();
        this.board = new ArrayList<>();
    }

    public void addTodo(String name, ToDo todo) {
        for (int i= 0; i< board.size(); i++) {
            if (this.board.get(i).getName().toString().equals(name)) {
                this.board.get(i).addTodo(todo);
            }
        }
    }

    public void removeTodo(String name, ToDo todo){
        for (int i= 0; i< board.size(); i++) {
            if (this.board.get(i).getName().toString().equals(name)) {
                this.board.get(i).deleteTodo(todo);
            }
        }
    }

    public ArrayList<String> toDoTitles(){
        
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
}
