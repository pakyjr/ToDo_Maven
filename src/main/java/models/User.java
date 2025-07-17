package models;

import models.board.*;
import java.util.*;

public class User {
    private final UUID id;
    private final String username;
    private final String hashedPassword;
    private final ArrayList<Board> boardList;

    public User(String username, String plainPassword) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.hashedPassword = hashPassword(plainPassword);
        this.boardList = new ArrayList<>();
    }

    public User(String username, String hashedPassword, ArrayList<Board> existingBoards, UUID id) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.boardList = existingBoards != null ? existingBoards : new ArrayList<>();
    }

    public static String hashPassword(String password) {

        return Integer.toHexString(password.hashCode());
    }

    public boolean checkPassword(String plainPassword) {
        return this.hashedPassword.equals(hashPassword(plainPassword));
    }

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

    public void addBoard(Board board) {
        boardList.add(board);
    }

    public void clearBoards() {
        this.boardList.clear();
    }


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

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() { // Added for DAO
        return hashedPassword;
    }

    public ArrayList<Board> getBoardList() {
        return new ArrayList<>(this.boardList);
    }

    public Board getBoard(BoardName boardName) {
        String boardNameStr = boardName.toString();
        for (Board board : boardList) {
            if (board.getName().toString().equals(boardNameStr)) {
                return board;
            }
        }
        return null;
    }

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