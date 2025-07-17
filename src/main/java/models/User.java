package models;

import models.board.*;

import java.util.*;

public class User {
    private final UUID id; // Added UUID for unique identification
    private final String username;
    private final String hashedPassword;
    private final ArrayList<Board> boardList;

    public User(String username, String plainPassword) {
        this.id = UUID.randomUUID(); // Generate a new ID for new users
        this.username = username;
        this.hashedPassword = hashPassword(plainPassword);
        this.boardList = new ArrayList<>();
        // fillBoard is called after successful registration in Controller
        // fillBoard(this.username); // Removed from constructor to be called after DB save
    }

    public User(String username, String hashedPassword, ArrayList<Board> existingBoards, UUID id) {
        this.id = id; // Assign existing ID for loaded users
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.boardList = existingBoards != null ? existingBoards : new ArrayList<>();
    }

    public static String hashPassword(String password) {
        // Simple hashing for demonstration. In production, use strong, secure hashing like BCrypt.
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

    // --- NEW METHOD ADDED ---
    public void addBoard(Board board) {
        // This method is useful when loading existing boards from the database
        // to avoid re-creating a new Board object and instead directly add the loaded one.
        boardList.add(board);
    }
    // --- END NEW METHOD ---

    // --- NEW METHOD ADDED ---
    public void clearBoards() {
        this.boardList.clear();
    }
    // --- END NEW METHOD ---

    public void fillBoard(String user) {
        // Ensure default boards are added only if they don't exist
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