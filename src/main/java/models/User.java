package models;

import models.board.*;

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

    public static String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    // Original method to create and add a new board
    public Board addBoard(BoardName boardName, String username) {
        for (Board existingBoard : boardList) {
            if (existingBoard.getName().equals(boardName) && existingBoard.getOwner().equals(username)) {
                System.out.println("Board with name " + boardName + " already exists for this user.");
                return null;
            }
        }
        Board board = new Board(boardName, username);
        boardList.add(board);
        return board;
    }

    // Added: Overloaded method to add an existing Board object (e.g., loaded from DAO)
    public void addBoard(Board boardToAdd) {
        // Check if a board with the same name and owner already exists to prevent duplicates
        for (Board existingBoard : boardList) {
            if (existingBoard.getName().equals(boardToAdd.getName()) && existingBoard.getOwner().equals(boardToAdd.getOwner())) {
                System.out.println("Board with name " + boardToAdd.getName() + " already exists for this user.");
                return;
            }
        }
        boardList.add(boardToAdd);
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

    /**
     * Retrieves a specific board for this user by its name.
     * Assumes a user only has one board of a given BoardName.
     * @param boardName The name of the board to retrieve.
     * @return The Board object if found, otherwise null.
     */
    public Board getBoard(BoardName boardName) {
        for (Board board : boardList) {
            if (board.getName().equals(boardName) && board.getOwner().equals(this.username)) {
                return board;
            }
        }
        return null;
    }

    /**
     * Moves a ToDo from a source board to a target board for the current user.
     * This method directly operates on the in-memory ToDo objects.
     * Database updates will be handled by the Controller after this operation.
     *
     * @param sourceBoardName The name of the board from which to move the ToDo.
     * @param targetBoardName The name of the board to which to move the ToDo.
     * @param position The 1-based position of the ToDo on the source board.
     */
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

    /**
     * Clears all boards currently associated with this user.
     * This is typically used before loading boards from persistent storage
     * to prevent in-memory duplicates.
     */
    public void clearBoards() {
        this.boardList.clear();
    }
}