package models;

import models.board.*;

import java.util.*;

public class User {
    private final String username;
    private final String hashedPassword;
    private final ArrayList<Board> boardList;

    public User(String username, String plainPassword) {
        this.username = username;
        this.hashedPassword = hashPassword(plainPassword);
        this.boardList = new ArrayList<>();
        fillBoard(this.username);
    }

    public Board addBoard(BoardName boardName, String username) {
        Board board = new Board(boardName, username);
        boardList.add(board);

        return board;
    }

    public void fillBoard(String user) {
        addBoard(BoardName.WORK, user);
        addBoard(BoardName.UNIVERSITY, user);
        addBoard(BoardName.FREE_TIME, user);
    }

    public void deleteBoard(BoardName boardName) {
        String boardNameStr = boardName.toString();
        if (!boardList.contains(boardNameStr)) {
            System.out.println("Board does not exist");
            return;
        }
        boardList.remove(boardNameStr);
        System.out.printf("Board %s deleted%n", boardNameStr);
    }

    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<Board> getBoardList() {
        return this.boardList;
    }

    public Board getBoard(BoardName boardName) {
        String boardNameStr = boardName.toString().toLowerCase();
        for (Board board : boardList) {
            if (board.getName().toString().toLowerCase().equals(boardNameStr)) {
                return board;
            }
        }
        System.out.println("Board does not exist");
        return null;
    }

    public void moveToDoToAnotherBoard(BoardName sourceBoardName, BoardName targetBoardName, int position) {
        Board sourceBoard = getBoard(sourceBoardName);
        Board targetBoard = getBoard(targetBoardName);

        if (sourceBoard == null || targetBoard == null) {
            return;
        }

        List<ToDo> sourceTodoList = sourceBoard.getTodoList();
        if (position < 1 || position > sourceTodoList.size()) {
            System.out.println("Invalid position");
            return;
        }

        ToDo todo = sourceTodoList.get(position - 1);
        sourceBoard.removeToDo(todo);

        targetBoard.addExistingTodo(todo);
    }
}