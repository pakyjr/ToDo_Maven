package models.board;

import models.ToDo;
import java.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Rappresenta una bacheca (Board) contenente una lista di attività (ToDo).
 * Ogni board ha un nome, un proprietario, un colore e un identificatore.
 */
public class Board {
    private int id;
    private final BoardName name;
    private final String owner;
    private String color;
    private final List<ToDo> todoList;

    /**
     * Costruisce una nuova board con nome e proprietario.
     * @param name Nome della board (valore enum)
     * @param owner Nome dell'utente proprietario
     */
    public Board(BoardName name, String owner) {
        this.name = name;
        this.owner = owner;
        this.todoList = new ArrayList<>();
        this.color = "Default";
        this.id = 0;
    }

    /**
     * Costruisce una board con ID, nome, proprietario e colore specificati.
     * @param id ID della board
     * @param name Nome della board
     * @param owner Nome del proprietario
     * @param color Colore associato alla board
     */
    public Board(int id, BoardName name, String owner, String color) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.color = color;
        this.todoList = new ArrayList<>();
    }

    /** @return Colore della board */
    public String getColor() {
        return color;
    }

    /**
     * Imposta il colore della board.
     * @param color Nuovo colore
     */
    public void setColor(String color) {
        this.color = color;
    }

    /** @return ID numerico della board */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID della board.
     * @param id Nuovo ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Aggiunge un nuovo ToDo alla board con il proprietario predefinito (owner della board).
     * @param title Titolo del ToDo
     * @return Il ToDo creato, oppure null se già esistente
     */
    public ToDo addTodo(String title) {
        return addTodo(title, this.owner);
    }

    /**
     * Aggiunge un nuovo ToDo alla board con titolo e proprietario specificati.
     * Evita duplicati per titolo e proprietario.
     * @param title Titolo del ToDo
     * @param owner Proprietario del ToDo
     * @return Il ToDo creato, oppure null se già presente
     */
    public ToDo addTodo(String title, String owner) {
        if (todoList.stream().anyMatch(todo -> todo.getTitle().equals(title) && todo.getOwner().equals(owner))) {
            System.err.println("Error: A ToDo with title '" + title + "' by owner '" + owner + "' already exists on board " + this.name);
            return null;
        }

        ToDo todo = new ToDo(title, owner);
        todoList.add(todo);
        todo.setPosition(todoList.size());
        return todo;
    }

    /**
     * Aggiunge un ToDo esistente alla board, evitando duplicati in base all'ID.
     * @param existingTodo ToDo esistente da aggiungere
     */
    public void addExistingTodo(ToDo existingTodo) {
        if (todoList.stream().anyMatch(todo -> todo.getId().equals(existingTodo.getId()))) {
            System.err.println("Error: ToDo with ID '" + existingTodo.getId() + "' already exists on board " + this.name);
            return;
        }
        todoList.add(existingTodo);
    }

    /**
     * Rimuove un ToDo dalla board e aggiorna le posizioni degli altri.
     * Se il ToDo è stato creato dal proprietario della board, la lista utenti condivisi viene svuotata.
     * @param toDoToRemove ToDo da rimuovere
     */
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

    /**
     * Restituisce una copia della lista dei ToDo presenti nella board.
     * @return Lista dei ToDo
     */
    public List<ToDo> getTodoList() {
        return new ArrayList<>(todoList);
    }

    /** @return Nome (enum) della board */
    public BoardName getName() {
        return name;
    }

    /** @return Proprietario della board */
    public String getOwner() {
        return owner;
    }

    /**
     * Confronta due board in base a ID, nome e proprietario.
     * @param o Oggetto da confrontare
     * @return true se sono uguali
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return id == board.id && name == board.name && Objects.equals(owner, board.owner);
    }

    /** @return Hash della board */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, owner);
    }
}
