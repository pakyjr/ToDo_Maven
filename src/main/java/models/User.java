package models;

import models.board.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String hashedPassword;
    private HashMap<String, Board> boardList;

    public User(String username) {
        id = UUID.randomUUID();
        this.username = username;
        boardList = new HashMap<>();
    }

    public User(String username, String plainPassword) {
        id = UUID.randomUUID();
        this.username = username;
        hashedPassword = hashPassword(plainPassword);
        boardList = new HashMap<>();
    }

    public void addBoard(BoardName boardName) {

        if(boardList.containsKey(boardName.toString())) {
            System.out.println("Board already exists");
            return;
        }

        Board board = new Board(boardName, getId());
        boardList.put(boardName.toString(), board);
    }

    public void deleteBoard(BoardName boardName) {
        if(!boardList.containsKey(boardName.toString())) {
            System.out.println("Board does not exist");
            return;
        }
        boardList.remove(boardName.toString());
        System.out.println(String.format("Board %s deleted", boardName.toString()));
    }

    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    public String getId(){
        return id.toString();
    }
}