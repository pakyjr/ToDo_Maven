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

    public User(String username, String hashedPassword, ArrayList<Board> existingBoards) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.boardList = existingBoards != null ? existingBoards : new ArrayList<>();
    }

    public static String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    public Board addBoard(BoardName boardName, String username) {
        // You might want to make this `username` parameter redundant if Board always uses `this.username`
        // However, if a user can create a board *on behalf* of another user (less common), keep it.
        // For standard use, the board's owner will be the User's username.

        for (Board existingBoard : boardList) {
            if (existingBoard.getName().equals(boardName) && existingBoard.getOwner().equals(username)) { // Added check for owner
                System.out.println("Board with name " + boardName + " already exists for this user.");
                return null;
            }
        }
        Board board = new Board(boardName, username); // The board's owner is the user creating it
        boardList.add(board);
        return board;
    }

    public void fillBoard(String user) {
        // Ensure default boards are created for the current user (this.username)
        // If a board is not found, add it, setting the owner as 'user' (which should be this.username)
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
        // Find the board by name AND owner (this user's username)
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
        // Ensure we find the board owned by *this* user
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

        List<ToDo> sourceTodoList = sourceBoard.getTodoList(); // Get a copy of the list
        if (position < 1 || position > sourceTodoList.size()) {
            System.out.println("Invalid position for ToDo in source board.");
            return;
        }

        // Get the ToDo from the source list.
        // IMPORTANT: We're getting the actual object reference from the source board's internal list
        // so that when we remove it, it's the correct object.
        ToDo todoToMove = sourceBoard.getTodoList().get(position - 1); // Get the actual object

        // Check if the ToDo being moved is owned by someone else (i.e., it's a shared copy)
        // If it's a shared copy, its owner is not this user.
        // We might want to restrict moving of shared ToDos, or simply move the specific instance.
        // For now, let's allow moving the *instance* regardless of its original owner.

        // Remove the ToDo from the source board's list.
        // The removeToDo method in Board will re-index positions.
        sourceBoard.removeToDo(todoToMove);

        // Add the ToDo to the target board's list.
        // The addExistingTodo method in Board will assign a new position.
        targetBoard.addExistingTodo(todoToMove); // Add the same instance

        System.out.printf("ToDo '%s' (ID: %s) moved from %s to %s for user %s.%n",
                todoToMove.getTitle(), todoToMove.getId(), sourceBoardName, targetBoardName, this.username);
    }
}