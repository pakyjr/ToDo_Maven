package models.board;

import models.ToDo;
import models.User;
import java.time.LocalDate;
import java.util.*;


public class Board {
    private BoardName name;
    private String description;
    private String owner;
    private Set<String> users;
    private ArrayList<ToDo> todoList;


    public Board(BoardName name, String owner){
        this.name = name;
        this.owner = owner;
        todoList = new ArrayList<>();
        this.users = new HashSet<>();
        this.users.add(owner);
    }

    public Board(BoardName name, String owner, String description){
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.users = new HashSet<>();

        this.users.add(owner);
        todoList = new ArrayList<>();
    }

    public ArrayList<ToDo> getTodoList() {
        return todoList;
    }

    public Set<String> getAllowedUsers() {
        return users;
    }

    private void addUser(User guest) {
        if (users.contains(guest.getUsername())) {
            return;
        }

        this.users.add(guest.getUsername());

        if (!guest.getBoardList().containsKey(name.toString())) {
            guest.addBoard(name, this.owner);
        }
    }

    public void shareTodo(User guest, ToDo todo) {
        addUser(guest);
        guest.getBoard(name).addTodo(todo);
        guest.getBoard(name).addUser(guest);
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
    public ToDo addTodo(ToDo todo){
        todoList.add(todo);

        int listSize = todoList.size();
        todo.setPosition(listSize);

        return todo;
    }

    public void deleteTodo(ToDo todo){
        //1) Handle users
        //2) TODO Handle position
        int position = todo.getPosition();
        ToDo T = todoList.get(position-1);
        todoList.remove(position-1);
    }

    public BoardName getName() {
        return name;
    }

    //override, crei una che non ha argomenti, e quella controlla la data odierna.
    //TODO metodo che crea una lista vuota, e ci aggiunge solo i todo che hanno la due date che coincide con la data passata in parametro
    //sortDueData(Date dueDate) --> creare una lista che ha solo i todo che hanno duedate == dueDate

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