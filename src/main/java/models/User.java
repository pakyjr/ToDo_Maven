package models;

import models.board.*;
import java.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Rappresenta un utente dell'applicazione con credenziali, ID e una lista di bacheche (Board).
 */
public class User {
    private final UUID id;
    private final String username;
    private final String hashedPassword;
    private final ArrayList<Board> boardList;

    /**
     * Costruisce un nuovo utente con una password in chiaro (che verrà hashata internamente).
     * @param username Nome utente
     * @param plainPassword Password in chiaro
     */
    public User(String username, String plainPassword) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.hashedPassword = hashPassword(plainPassword);
        this.boardList = new ArrayList<>();
    }

    /**
     * Costruttore alternativo con tutti i parametri specificati (utile per caricare da database).
     * @param username Nome utente
     * @param hashedPassword Password già hashata
     * @param existingBoards Lista di board esistenti
     * @param id Identificativo univoco dell'utente
     */
    public User(String username, String hashedPassword, ArrayList<Board> existingBoards, UUID id) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.boardList = existingBoards != null ? existingBoards : new ArrayList<>();
    }

    /**
     * Crea un hash della password usando il metodo hashCode.
     * @param password Password in chiaro
     * @return Hash della password come stringa esadecimale
     */
    public static String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    /**
     * Verifica se la password fornita corrisponde all'hash salvato.
     * @param plainPassword Password in chiaro da verificare
     * @return true se la password corrisponde, altrimenti false
     */
    public boolean checkPassword(String plainPassword) {
        return this.hashedPassword.equals(hashPassword(plainPassword));
    }

    /**
     * Aggiunge una nuova board all'utente, se non esiste già una con lo stesso nome.
     * @param boardName Nome della board
     * @param username Nome utente proprietario della board
     * @return La board creata, o null se già esiste
     */
    public Board addBoard(BoardName boardName, String username) {
        for (Board existingBoard : boardList) {
            if (existingBoard.getName().equals(boardName)) {
                System.out.println("Board with name " + boardName + " already exists for this user.");
                return null;
            }
        }
        Board board = new Board(boardName, username);
        boardList.add(board);
        return board;
    }

    /**
     * Aggiunge una board esistente alla lista dell'utente.
     * @param board Oggetto Board da aggiungere
     */
    public void addBoard(Board board) {
        boardList.add(board);
    }

    /**
     * Rimuove tutte le board dell'utente.
     */
    public void clearBoards() {
        this.boardList.clear();
    }

    /**
     * Riempie la lista board con 3 board predefinite: WORK, UNIVERSITY, FREE_TIME.
     * @param user Nome dell'utente proprietario delle board
     */
    public void fillBoard(String user) {
        if (getBoard(BoardName.WORK) == null) {
            addBoard(BoardName.WORK, user);
        }
        if (getBoard(BoardName.UNIVERSITY) == null) {
            addBoard(BoardName.UNIVERSITY, user);
        }
        if (getBoard(BoardName.FREE_TIME) == null) {
            addBoard(BoardName.FREE_TIME, user);
        }
    }

    /**
     * Elimina una board dell'utente se esiste.
     * @param boardName Nome della board da eliminare
     */
    public void deleteBoard(BoardName boardName) {
        Board boardToRemove = null;
        for (Board board : boardList) {
            if (board.getName().equals(boardName)) {
                boardToRemove = board;
                break;
            }
        }

        if (boardToRemove != null) {
            boardList.remove(boardToRemove);
            System.out.printf("Board %s deleted%n", boardName.toString());
        } else {
            System.out.println("Board does not exist");
        }
    }

    /**
     * Restituisce l'ID dell'utente.
     * @return UUID dell'utente
     */
    public UUID getId() {
        return id;
    }

    /**
     * Restituisce il nome utente.
     * @return Username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Restituisce la password hashata.
     * @return Password hashata
     */
    public String getHashedPassword() {
        return hashedPassword;
    }

    /**
     * Restituisce la lista di board associate all'utente.
     * @return Copia della lista di board
     */
    public ArrayList<Board> getBoardList() {
        return new ArrayList<>(this.boardList);
    }

    /**
     * Restituisce una board specifica in base al nome.
     * @param boardName Nome della board da cercare
     * @return Oggetto Board se trovato, altrimenti null
     */
    public Board getBoard(BoardName boardName) {
        String boardNameStr = boardName.toString();
        for (Board board : boardList) {
            if (board.getName().toString().equals(boardNameStr)) {
                return board;
            }
        }
        return null;
    }

    /**
     * Sposta un'attività (ToDo) da una board a un'altra in una posizione specificata.
     * @param sourceBoardName Nome della board di origine
     * @param targetBoardName Nome della board di destinazione
     * @param position Posizione (1-based) dell'attività nella board di origine
     */
    public void moveToDoToAnotherBoard(BoardName sourceBoardName, BoardName targetBoardName, int position) {
        Board sourceBoard = getBoard(sourceBoardName);
        Board targetBoard = getBoard(targetBoardName);

        if (sourceBoard == null || targetBoard == null) {
            System.out.println("Source or target board does not exist.");
            return;
        }

        List<ToDo> sourceTodoList = sourceBoard.getTodoList();
        if (position < 1 || position > sourceTodoList.size()) {
            System.out.println("Invalid position for ToDo in source board.");
            return;
        }

        ToDo todo = sourceTodoList.get(position - 1);
        sourceBoard.removeToDo(todo);
        targetBoard.addExistingTodo(todo);

        System.out.printf("ToDo '%s' moved from %s to %s.%n", todo.getTitle(), sourceBoardName, targetBoardName);
    }
}
