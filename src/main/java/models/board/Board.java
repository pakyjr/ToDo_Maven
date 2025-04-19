package models.board;

import models.ToDo;
import models.User;

import java.util.*;

public class Board {
    private BoardName name;
    private String description;
    private String owner;
    private ArrayList<ToDo> todoList;


    public Board(BoardName name, String owner){
        this.name = name;
        this.owner = owner;
        todoList = new ArrayList<>();
    }

    public Board(BoardName name, String owner, String description){
        this.name = name;
        this.description = description;
        this.owner = owner;
        todoList = new ArrayList<>();
    }

    public ToDo addTodo(String title) {
        return addTodo(title, this.owner);

    }

    public ToDo addTodo(String title, String owner){
        ToDo todo = new ToDo(title);
        todo.setOwner(this.owner);
        todoList.add(todo);

        int listSize = todoList.size();
        todo.setPosition(listSize);

        return todo;
    }

    public ArrayList<ToDo> getTodoList() {
        return todoList;
    }

    public void shareTodo(User guest, ToDo todo) {
        guest.getBoard(name).addTodo(todo.getTitle(), guest.getUsername());
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

    //TODO inconsistent, we should make the todo inside? idk


    public void deleteTodo(ToDo todo){
        //1) TODO Handle users (if the to do we are deleting is shared this method must handle the users)
        //2) TODO Handle position

        int position = todo.getPosition();
        todoList.remove(position-1);

        for(ToDo item: todoList) {
            if(item.getPosition() >= position){ //shift every item back
                item.setPosition(item.getPosition() - 1);
            }
        }
    }

    public BoardName getName() {
        return name;
    }

    //override, crei una che non ha argomenti, e quella controlla la data odierna.
    //TODO metodo che crea una lista vuota, e ci aggiunge solo i todo che hanno la due date che coincide con la data passata in parametro
    //sortDueData(Date dueDate) --> creare una lista che ha solo i todo che hanno duedate == dueDate

    public void sortDueDate(){
        //data odierna
        Date today = new Date();
    }

    public void sortDueDate(Date dueDate){
        //data
    }
}