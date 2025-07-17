package models;

import models.board.*;
import org.mindrot.jbcrypt.BCrypt;
import java.util.*;

public class User {
    private int id;
    private final String username;
    private final String hashedPassword;
    private final ArrayList<Board> boardList;

    public User(String username, String plainPassword) {
        this.username = username;
        this.hashedPassword = hashPassword(plainPassword);
        this.boardList = new ArrayList<>();
        this.id = -1;
    }

    public User(String username, String hashedPassword, ArrayList<Board> existingBoards) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.boardList = existingBoards != null ? existingBoards : new ArrayList<>();
        this.id = -1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static String hashPassword(String plainPassword) {

        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public boolean checkPassword(String plainPassword) {
        return BCrypt.checkpw(plainPassword, this.hashedPassword);
    }

    public Board addBoard(BoardName boardName, String username) {
        for (Board existingBoard : boardList) {
            if (existingBoard.getName().equals(boardName) && existingBoard.getOwner().equals(username)) {
                System.out.println("Board with name " + boardName.getDisplayName() + " already exists for this user. Not adding duplicate.");
                return null;
            }
        }
        Board board = new Board(boardName, username);
        boardList.add(board);
        System.out.println("DEBUG: User.addBoard(BoardName, username) added board '" + boardName.getDisplayName() + "' to in-memory list.");
        return board;
    }

    public void addBoard(Board boardToAdd) {

        for (Board existingBoard : boardList) {
            if (existingBoard.getName().equals(boardToAdd.getName()) && existingBoard.getOwner().equals(boardToAdd.getOwner())) {
                System.out.println("DEBUG: Board with name '" + boardToAdd.getName().getDisplayName() + "' already exists in user's in-memory list. Not adding duplicate.");
                return;
            }
        }
        boardList.add(boardToAdd);
        System.out.println("DEBUG: User.addBoard(Board) successfully added board '" + boardToAdd.getName().getDisplayName() + "' to in-memory list.");
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
            if (board.getName().equals(boardName) && board.getOwner().equals(this.username)) {
                boardToRemove = board;
                break;
            }
        }

        if (boardToRemove != null) {
            boardList.remove(boardToRemove);
            System.out.printf("Board %s deleted%n", boardName.toString());
        } else {
            System.out.println("Board does not exist for this user.");
        }
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public ArrayList<Board> getBoardList() {
        return new ArrayList<>(this.boardList);
    }

    public Board getBoard(BoardName boardName) {
        for (Board board : boardList) {
            if (board.getName().equals(boardName) && board.getOwner().equals(this.username)) {
                return board;
            }
        }
        System.out.println("DEBUG: getBoard(" + boardName.getDisplayName() + ") returned null for user '" + this.username + "'. Board not found in in-memory list.");
        return null;
    }

    public void moveToDoToAnotherBoard(BoardName sourceBoardName, BoardName targetBoardName, int position) {
        Board sourceBoard = getBoard(sourceBoardName);
        Board targetBoard = getBoard(targetBoardName);

        if (sourceBoard == null || targetBoard == null) {
            System.out.println("Source or target board does not exist for this user.");
            return;
        }

        List<ToDo> sourceTodoList = sourceBoard.getTodoList();
        if (position < 1 || position > sourceTodoList.size()) {
            System.out.println("Invalid position for ToDo in source board.");
            return;
        }

        ToDo todoToMove = sourceBoard.getTodoList().get(position - 1);

        sourceBoard.removeToDo(todoToMove);
        targetBoard.addExistingTodo(todoToMove);

        System.out.printf("ToDo '%s' (ID: %s) moved from %s to %s for user %s.%n",
                todoToMove.getTitle(), todoToMove.getId(), sourceBoardName, targetBoardName, this.username);
    }

    public void clearBoards() {
        this.boardList.clear();
        System.out.println("DEBUG: User's in-memory board list cleared.");
    }
}