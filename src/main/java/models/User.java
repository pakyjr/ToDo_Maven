package models;

import models.board.*;

import java.util.*;

public class User {
    private String username;
    private String hashedPassword;
    private HashMap<String, Board> boardList;

    public User(String username, String plainPassword) {
        this.username = username;
        hashedPassword = hashPassword(plainPassword);
        boardList = new HashMap<>();
    }

    public Optional<Board> addBoard(BoardName boardName, String username) {

        if(boardList.containsKey(boardName.toString())) {
            System.out.println("Board already exists");
            return Optional.empty();
        }

        Board board = new Board(boardName, username);
        boardList.put(boardName.toString(), board);

        return Optional.of(board);
    }

    public void deleteBoard(BoardName boardName) {
        if(!boardList.containsKey(boardName.toString())) {
            System.out.println("Board does not exist");
            return;
        }
        boardList.remove(boardName.toString());
        System.out.printf("Board %s deleted%n", boardName.toString());
    }

    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    public String getUsername(){
        return username;
    }

    public HashMap<String, Board> getBoardList() {
        return boardList;
    }

    public Board getBoard(BoardName boardName) {
        if(!boardList.containsKey(boardName.toString())) {
            System.out.println("Board does not exist");
            return null;
        }
        return boardList.get(boardName.toString());
    }


    //TODO TO TEST!
    public void changeBoard(BoardName oldBoardName, BoardName newBoardName, int position){

        Board oldBoard = getBoard(oldBoardName);
        Board newBoard = getBoard(newBoardName);

        ArrayList<ToDo> oldTodoList = oldBoard.getTodoList();
        ToDo todo = oldTodoList.get(position - 1);
        oldBoard.deleteTodo(todo);

        newBoard.addTodo(todo);
        newBoard.changePosition(todo, position);
         //TODO va aggioranta la posizione nella lista nuova.
    }
}