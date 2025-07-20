package controller;

import models.*;
import models.board.Board;
import models.board.BoardName;
import dao.UserDAO;
import dao.UserDAOImpl;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller principale dell'applicazione che gestisce le operazioni sui ToDo e sui Board.
 * Funge da intermediario tra il livello di presentazione e il livello di accesso ai dati,
 * fornendo funzionalità per la gestione degli utenti, board e ToDo items.
 */
public class Controller {
    /** Utente attualmente loggato nel sistema */
    public User user;

    /** Data Access Object per le operazioni sulla persistenza degli utenti */
    private UserDAO userDAO;

    /**
     * Costruttore del Controller che inizializza il DAO per l'accesso ai dati.
     *
     * @throws SQLException se si verifica un errore durante l'inizializzazione del DAO
     */
    public Controller() throws SQLException {
        this.userDAO = new UserDAOImpl();
    }

    /**
     * Registra un nuovo utente nel sistema creando i board di default.
     *
     * @param username nome utente per il nuovo account
     * @param plainPassword password in chiaro che verrà hashata internamente
     */
    public void register(String username, String plainPassword){
        User newUser = new User(username, plainPassword);

        try {
            boolean success = userDAO.saveUser(newUser);
            if (success) {
                this.user = newUser;
                // Crea i board di default per il nuovo utente
                newUser.fillBoard(newUser.getUsername());

                // Salva tutti i board di default nel database
                for (Board board : newUser.getBoardList()) {
                    userDAO.saveBoard(board, newUser.getId());
                }

                System.out.println("User '" + username + "' registered successfully and default boards created.");
            } else {
                System.err.println("Registration failed: User '" + username + "' might already exist.");
                this.user = null;
            }
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
            this.user = null;
        }
    }

    /**
     * Effettua il login di un utente esistente caricando i suoi dati dal database.
     *
     * @param username nome utente per il login
     * @param plainPassword password in chiaro che verrà verificata
     * @return l'oggetto User se il login ha successo, null altrimenti
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public User login(String username, String plainPassword) throws SQLException {
        Optional<User> optionalUser = userDAO.getUserByUsername(username);

        if (optionalUser.isPresent()) {
            User foundUser = optionalUser.get();
            if (foundUser.checkPassword(plainPassword)) {
                this.user = foundUser;
                // Carica tutti i board e ToDo dell'utente
                userDAO.loadUserBoardsAndToDos(this.user);
                System.out.println("User '" + username + "' logged in successfully.");
                return foundUser;
            } else {
                System.out.println("Login failed: Incorrect password for user '" + username + "'.");
                this.user = null;
                return null;
            }
        } else {
            System.out.println("Login failed: User '" + username + "' not found.");
            this.user = null;
            return null;
        }
    }

    /**
     * Aggiorna un board esistente nel database.
     * Verifica che l'utente corrente sia il proprietario del board prima dell'aggiornamento.
     *
     * @param board il board da aggiornare
     */
    public void updateBoard(Board board) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to update a board.");
            return;
        }
        if (board == null) {
            System.err.println("Error: Board object cannot be null for update.");
            return;
        }
        try {
            // Verifica dei permessi di proprietà
            if (!this.user.getUsername().equals(board.getOwner())) {
                System.err.println("Permission Denied: User '" + this.user.getUsername() + "' is not the owner of board '" + board.getName().getDisplayName() + "'. Update aborted.");
                return;
            }
            userDAO.updateBoard(board);
            System.out.println("Board '" + board.getName().getDisplayName() + "' updated successfully in the database.");
        } catch (SQLException e) {
            System.err.println("Database error updating board '" + board.getName().getDisplayName() + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Aggiunge un nuovo ToDo a un board specificato.
     *
     * @param boardNameStr nome del board dove aggiungere il ToDo
     * @param toDoName titolo del nuovo ToDo
     * @param description descrizione del ToDo
     * @param date data di scadenza nel formato dd/MM/yyyy
     * @param url URL associato al ToDo
     * @param color colore del ToDo per la visualizzazione
     * @param image immagine associata al ToDo
     * @param activities mappa delle attività associate al ToDo
     * @param status stato corrente del ToDo
     * @param owner proprietario del ToDo (deve corrispondere all'utente loggato)
     * @return l'ID del ToDo creato come stringa, null se la creazione fallisce
     */
    public String addToDo(String boardNameStr, String toDoName, String description, String date, String url, String color, String image, Map<String, Boolean> activities, String status, String owner){
        if (this.user == null) {
            System.err.println("Error: No user is logged in to add a ToDo.");
            return null;
        }

        // Verifica che il proprietario corrisponda all'utente loggato
        if (!this.user.getUsername().equals(owner)) {
            System.err.println("Error: Attempted to create ToDo with owner '" + owner + "' but current user is '" + this.user.getUsername() + "'. ToDo owner must match current user.");
            return null;
        }

        // Conversione del nome del board da stringa a enum
        BoardName boardEnumName;
        try {
            boardEnumName = BoardName.fromDisplayName(boardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name '" + boardNameStr + "'. " + e.getMessage());
            return null;
        }

        Board board = user.getBoard(boardEnumName);

        if (board == null) {
            System.err.println("Error: Board '" + boardNameStr + "' not found for user.");
            return null;
        }

        // Creazione del ToDo e impostazione delle proprietà
        ToDo toDo = board.addTodo(toDoName, owner);
        if (toDo == null) {
            return null;
        }

        toDo.setDescription(description);
        toDo.setUrl(url);
        toDo.setColor(color);
        toDo.setImage(image);
        toDo.setActivityList(activities);
        toDo.setStatus(status);

        // Parsing e impostazione della data di scadenza
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(date, formatter);
            toDo.setDueDate(localDate);
        } catch (java.time.format.DateTimeParseException e) {
            System.err.println("Error parsing due date: " + date + " - " + e.getMessage());
            return null;
        }

        // Salvataggio nel database
        try {
            int boardId = userDAO.getBoardId(boardEnumName, user.getUsername());
            if (boardId != -1) {
                userDAO.saveToDo(toDo, boardId);
                System.out.println("ToDo '" + toDoName + "' added successfully to board '" + boardNameStr + "'.");
            } else {
                System.err.println("Board not found in database for saving ToDo.");
                board.removeToDo(toDo);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Database error saving ToDo: " + e.getMessage());
            e.printStackTrace();
            board.removeToDo(toDo);
            return null;
        }

        return toDo.getId().toString();
    }

    /**
     * Aggiorna un ToDo esistente con nuove informazioni.
     * Verifica i permessi di proprietà prima dell'aggiornamento.
     *
     * @param boardNameStr nome del board contenente il ToDo
     * @param oldToDoTitle titolo attuale del ToDo
     * @param newToDoTitle nuovo titolo del ToDo
     * @param description nuova descrizione
     * @param date nuova data di scadenza nel formato dd/MM/yyyy
     * @param url nuovo URL associato
     * @param color nuovo colore
     * @param image nuova immagine
     * @param activities nuove attività
     * @param status nuovo stato
     * @param owner proprietario del ToDo
     */
    public void updateToDo(String boardNameStr, String oldToDoTitle, String newToDoTitle, String description, String date, String url, String color, String image, Map<String, Boolean> activities, String status, String owner) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to update a ToDo.");
            return;
        }

        BoardName boardEnumName;
        try {
            boardEnumName = BoardName.fromDisplayName(boardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name '" + boardNameStr + "'. " + e.getMessage());
            return;
        }

        Board board = user.getBoard(boardEnumName);

        if (board == null) {
            System.err.println("Error: Board '" + boardNameStr + "' not found for user.");
            return;
        }

        // Ricerca del ToDo da aggiornare
        Optional<ToDo> optionalToDo = board.getTodoList().stream()
                .filter(t -> t.getTitle().equals(oldToDoTitle))
                .findFirst();

        if (optionalToDo.isPresent()) {
            ToDo toDoToUpdate = optionalToDo.get();

            // Verifica dei permessi di proprietà
            if (!isCurrentUserToDoCreator(toDoToUpdate)) {
                System.err.println("Permission Denied: User '" + this.user.getUsername() + "' is not the owner of ToDo '" + oldToDoTitle + "'. Update aborted.");
                return;
            }

            // Verifica che il nuovo titolo non sia duplicato
            if (!oldToDoTitle.equals(newToDoTitle) &&
                    board.getTodoList().stream().anyMatch(t -> t.getTitle().equals(newToDoTitle) && t.getOwner().equals(toDoToUpdate.getOwner()))) {
                System.err.println("Error: A ToDo with title '" + newToDoTitle + "' and same owner already exists on board '" + boardNameStr + "'. Update aborted.");
                return;
            }

            // Aggiornamento delle proprietà del ToDo
            toDoToUpdate.setTitle(newToDoTitle);
            toDoToUpdate.setDescription(description);
            toDoToUpdate.setUrl(url);
            toDoToUpdate.setColor(color);
            toDoToUpdate.setImage(image);
            toDoToUpdate.setActivityList(activities);
            toDoToUpdate.setStatus(status);

            // Parsing e aggiornamento della data
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate localDate = LocalDate.parse(date, formatter);
                toDoToUpdate.setDueDate(localDate);
            } catch (java.time.format.DateTimeParseException e) {
                System.err.println("Error parsing due date: " + date + " - " + e.getMessage());
                return;
            }

            // Salvataggio delle modifiche nel database
            try {
                int boardId = userDAO.getBoardId(boardEnumName, user.getUsername());
                if (boardId != -1) {
                    userDAO.updateToDo(toDoToUpdate, boardId);
                    System.out.println("ToDo '" + oldToDoTitle + "' updated successfully to '" + newToDoTitle + "' on board '" + boardNameStr + "'.");
                } else {
                    System.err.println("Board not found in database for updating ToDo.");
                }
            } catch (SQLException e) {
                System.err.println("Database error updating ToDo: " + e.getMessage());
                e.printStackTrace();
            }

        } else {
            System.err.println("ToDo with title '" + oldToDoTitle + "' not found on board " + boardNameStr);
        }
    }

    /**
     * Recupera un ToDo specifico tramite il suo titolo e il nome del board.
     *
     * @param title titolo del ToDo da cercare
     * @param boardNameStr nome del board dove cercare
     * @return l'oggetto ToDo se trovato, null altrimenti
     */
    public ToDo getToDoByTitle(String title, String boardNameStr){
        if (this.user == null) {
            System.err.println("Error: No user is logged in to get a ToDo.");
            return null;
        }

        BoardName boardEnumName;
        try {
            boardEnumName = BoardName.fromDisplayName(boardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name '" + boardNameStr + "'. " + e.getMessage());
            return null;
        }

        Board board = user.getBoard(boardEnumName);
        if (board == null) {
            System.err.println("Board " + boardNameStr + " not found for user '" + user.getUsername() + "'.");
            return null;
        }

        return board.getTodoList().stream()
                .filter(toDo -> toDo.getTitle().equals(title))
                .findFirst()
                .orElse(null);
    }

    /**
     * Recupera una lista dei titoli di tutti i ToDo presenti in un board specificato.
     *
     * @param boardNameStr nome del board di cui ottenere i ToDo
     * @return ArrayList contenente i titoli dei ToDo, lista vuota se il board non esiste
     */
    public ArrayList<String> getToDoListString(String boardNameStr){
        if (this.user == null) {
            System.err.println("Error: No user is logged in to get ToDo list.");
            return new ArrayList<>();
        }

        BoardName boardEnumName;
        try {
            boardEnumName = BoardName.fromDisplayName(boardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name '" + boardNameStr + "'. " + e.getMessage());
            return new ArrayList<>();
        }

        Board board = user.getBoard(boardEnumName);
        ArrayList<String> resultTitles = new ArrayList<>();
        if (board != null) {
            for(ToDo toDo : board.getTodoList()) {
                resultTitles.add(toDo.getTitle());
            }
        }
        return resultTitles;
    }

    /**
     * Elimina un ToDo dal board e dal database.
     * Gestisce diversi scenari di permessi: il creatore può eliminare l'originale,
     * gli utenti con cui è condiviso possono eliminare la loro copia.
     *
     * @param boardNameStr nome del board contenente il ToDo
     * @param toDoTitle titolo del ToDo da eliminare
     */
    public void deleteToDo(String boardNameStr, String toDoTitle) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to delete a ToDo.");
            return;
        }

        BoardName boardEnumName;
        try {
            boardEnumName = BoardName.fromDisplayName(boardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name '" + boardNameStr + "'. " + e.getMessage());
            return;
        }

        Board board = user.getBoard(boardEnumName);
        if (board == null) {
            System.err.println("Board " + boardNameStr + " not found for user.");
            return;
        }

        Optional<ToDo> toDoToRemoveOptional = board.getTodoList().stream()
                .filter(toDo -> toDo.getTitle().equals(toDoTitle))
                .findFirst();

        if (toDoToRemoveOptional.isPresent()) {
            ToDo toDoToRemove = toDoToRemoveOptional.get();

            // Verifica dei permessi di eliminazione
            boolean isCreator = isCurrentUserToDoCreator(toDoToRemove);
            boolean isRecipientDeleting = !isCreator && this.user.getBoard(boardEnumName).getTodoList().contains(toDoToRemove);

            if (!isCreator && !isRecipientDeleting) {
                System.err.println("Permission Denied: You cannot delete this ToDo. Only the creator can delete the original, or you can delete your shared copy.");
                return;
            }

            try {
                // Se è il creatore, rimuove tutte le condivisioni
                if (isCreator) {
                    userDAO.removeAllToDoSharing(toDoToRemove.getId().toString());
                    System.out.println("Removed all shared instances of ToDo '" + toDoTitle + "'.");
                }

                userDAO.deleteToDo(toDoToRemove.getId().toString(), user.getUsername());
                board.removeToDo(toDoToRemove);
                System.out.println("ToDo '" + toDoTitle + "' deleted successfully from board '" + boardNameStr + "'.");

            } catch (SQLException e) {
                System.err.println("Database error deleting ToDo: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("ToDo with title '" + toDoTitle + "' not found on board " + boardNameStr + " for deletion.");
        }
    }

    /**
     * Sposta un ToDo da un board a un altro.
     * Solo il creatore del ToDo può spostarlo e deve verificare che non esistano duplicati.
     *
     * @param toDoTitle titolo del ToDo da spostare
     * @param currentBoardDisplayName nome del board sorgente
     * @param destinationBoardDisplayName nome del board destinazione
     * @return true se lo spostamento ha successo, false altrimenti
     */
    public boolean moveToDo(String toDoTitle, String currentBoardDisplayName, String destinationBoardDisplayName) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to move a ToDo.");
            return false;
        }

        BoardName currentBoardEnum;
        BoardName destinationBoardEnum;
        try {
            currentBoardEnum = BoardName.fromDisplayName(currentBoardDisplayName);
            destinationBoardEnum = BoardName.fromDisplayName(destinationBoardDisplayName);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name provided. " + e.getMessage());
            return false;
        }

        Board currentBoard = user.getBoard(currentBoardEnum);
        Board destinationBoard = user.getBoard(destinationBoardEnum);

        if (currentBoard == null) {
            System.err.println("Error: Source board '" + currentBoardDisplayName + "' not found for current user.");
            return false;
        }
        if (destinationBoard == null) {
            System.err.println("Error: Destination board '" + destinationBoardDisplayName + "' not found for current user.");
            return false;
        }

        Optional<ToDo> optionalToDo = currentBoard.getTodoList().stream()
                .filter(t -> t.getTitle().equals(toDoTitle))
                .findFirst();

        if (!optionalToDo.isPresent()) {
            System.err.println("Error: ToDo '" + toDoTitle + "' not found on board '" + currentBoardDisplayName + "'.");
            return false;
        }
        ToDo toDoToMove = optionalToDo.get();

        // Verifica dei permessi - solo il creatore può spostare
        if (!isCurrentUserToDoCreator(toDoToMove)) {
            System.err.println("Permission Denied: Only the creator can move this ToDo.");
            return false;
        }

        // Verifica che non esistano duplicati nel board destinazione
        if (destinationBoard.getTodoList().stream().anyMatch(t -> t.getTitle().equals(toDoTitle) && t.getOwner().equals(toDoToMove.getOwner()))) {
            System.err.println("Error: A ToDo with title '" + toDoTitle + "' and same owner already exists on destination board '" + destinationBoardDisplayName + "'. Move aborted.");
            return false;
        }

        try {
            int destinationBoardId = userDAO.getBoardId(destinationBoardEnum, user.getUsername());

            if (destinationBoardId == -1) {
                System.err.println("Database error: Could not find destination board ID for move operation.");
                return false;
            }

            // Aggiornamento nel database e nella memoria
            userDAO.updateToDoBoardId(toDoToMove.getId().toString(), destinationBoardId);

            currentBoard.removeToDo(toDoToMove);
            destinationBoard.addExistingTodo(toDoToMove);

            System.out.println("ToDo '" + toDoTitle + "' successfully moved from '" + currentBoardDisplayName + "' to '" + destinationBoardDisplayName + "'.");
            return true;

        } catch (SQLException e) {
            System.err.println("Database error moving ToDo '" + toDoTitle + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Condivide un ToDo con una lista di utenti specificati.
     * Solo il creatore del ToDo può condividerlo con altri utenti.
     *
     * @param toDo il ToDo da condividere
     * @param usernamesToShareWith lista degli username con cui condividere
     * @param boardNameStr nome del board contenente il ToDo
     * @return true se tutte le condivisioni hanno successo, false se almeno una fallisce
     */
    public boolean shareToDoWithUsers(ToDo toDo, List<String> usernamesToShareWith, String boardNameStr) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in.");
            return false;
        }
        if (toDo == null) {
            System.err.println("Error: ToDo to share cannot be null.");
            return false;
        }
        if (!isCurrentUserToDoCreator(toDo)) {
            System.err.println("Permission Denied: Only the creator can share this ToDo.");
            return false;
        }
        if (usernamesToShareWith == null || usernamesToShareWith.isEmpty()) {
            System.out.println("No users selected to share with.");
            return true;
        }

        boolean allSuccess = true;
        for (String username : usernamesToShareWith) {
            try {
                // Condivisione nel database
                userDAO.shareToDo(toDo.getId().toString(), username);
                System.out.println("DEBUG: ToDo '" + toDo.getTitle() + "' DB shared with '" + username + "'.");

                // Aggiornamento dei dati in memoria del destinatario
                Optional<User> recipientUserOptional = userDAO.getUserByUsername(username);

                if (recipientUserOptional.isPresent()) {
                    User recipientUser = recipientUserOptional.get();

                    userDAO.loadUserBoardsAndToDos(recipientUser);

                    toDo.addSharedUser(recipientUser);

                    System.out.println("ToDo '" + toDo.getTitle() + "' shared successfully with user '" + username + "' and recipient's in-memory data updated.");

                } else {
                    System.err.println("User '" + username + "' not found after sharing. In-memory update for recipient failed.");
                    allSuccess = false;
                }

            } catch (SQLException e) {
                System.err.println("Database error sharing ToDo '" + toDo.getTitle() + "' with '" + username + "': " + e.getMessage());
                e.printStackTrace();
                allSuccess = false;
            } catch (IllegalArgumentException e) {
                System.err.println("Error with board name during sharing: " + e.getMessage());
                e.printStackTrace();
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    /**
     * Rimuove la condivisione di un ToDo da una lista di utenti specificati.
     * Solo il creatore del ToDo può gestire le condivisioni.
     *
     * @param toDo il ToDo di cui rimuovere la condivisione
     * @param usernamesToRemoveSharing lista degli username da cui rimuovere la condivisione
     * @return true se tutte le rimozioni hanno successo, false altrimenti
     */
    public boolean removeToDoSharing(ToDo toDo, List<String> usernamesToRemoveSharing) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in.");
            return false;
        }
        if (toDo == null) {
            System.err.println("Error: ToDo cannot be null.");
            return false;
        }
        if (!isCurrentUserToDoCreator(toDo)) {
            System.err.println("Permission Denied: Only the creator can manage sharing for this ToDo.");
            return false;
        }
        if (usernamesToRemoveSharing == null || usernamesToRemoveSharing.isEmpty()) {
            System.out.println("No users selected to remove sharing from.");
            return true;
        }

        boolean allSuccess = true;
        for (String username : usernamesToRemoveSharing) {
            try {
                userDAO.removeToDoSharing(toDo.getId().toString(), username);
                toDo.removeSharedUser(username);
                System.out.println("Removed sharing of ToDo '" + toDo.getTitle() + "' from user '" + username + "'.");

            } catch (SQLException e) {
                System.err.println("Database error removing sharing of ToDo '" + toDo.getTitle() + "' from '" + username + "': " + e.getMessage());
                e.printStackTrace();
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    /**
     * Recupera tutti gli utenti registrati nel sistema.
     *
     * @return Set contenente tutti gli utenti, set vuoto se l'operazione fallisce
     */
    public Set<User> getAllUsers() {
        if (this.user == null) {
            System.err.println("Error: No user is logged in to retrieve all users.");
            return java.util.Collections.emptySet();
        }
        try {
            return new HashSet<>(userDAO.getAllUsers());
        } catch (SQLException e) {
            System.err.println("Database error retrieving all users: " + e.getMessage());
            e.printStackTrace();
            return java.util.Collections.emptySet();
        }
    }

    /**
     * Verifica se l'utente correntemente loggato è il creatore di un ToDo specificato.
     *
     * @param toDo il ToDo di cui verificare la proprietà
     * @return true se l'utente corrente è il creatore, false altrimenti
     */
    public boolean isCurrentUserToDoCreator(ToDo toDo) {
        return this.user != null && toDo != null && this.user.getUsername().equals(toDo.getOwner());
    }

    /**
     * Recupera la lista degli utenti con cui è condiviso un ToDo specificato.
     * Solo il creatore del ToDo può vedere con chi è condiviso.
     *
     * @param boardNameStr nome del board contenente il ToDo
     * @param toDoTitle titolo del ToDo di cui ottenere gli utenti condivisi
     * @return ArrayList contenente gli username degli utenti con cui è condiviso,
     *         lista vuota se il ToDo non è trovato o l'utente non ha i permessi
     */
    public ArrayList<String> getSharedUsersForToDo(String boardNameStr, String toDoTitle) {
        if (this.user == null) {
            System.err.println("Error: No user is logged in.");
            return new ArrayList<>();
        }

        BoardName boardEnumName;
        try {
            boardEnumName = BoardName.fromDisplayName(boardNameStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid board name '" + boardNameStr + "'. " + e.getMessage());
            return new ArrayList<>();
        }

        Board board = user.getBoard(boardEnumName);
        if (board == null) {
            System.err.println("Error: Board \"" + boardNameStr + "\" not found for current user.");
            return new ArrayList<>();
        }

        Optional<ToDo> optionalToDo = board.getTodoList().stream()
                .filter(t -> t.getTitle().equals(toDoTitle))
                .findFirst();

        if (optionalToDo.isPresent()) {
            ToDo originalToDo = optionalToDo.get();

            // Verifica dei permessi - solo il creatore può vedere le condivisioni
            if (!isCurrentUserToDoCreator(originalToDo)) {
                System.err.println("Permission Denied: Only the creator can see who this ToDo is shared with.");
                return new ArrayList<>();
            }
            return originalToDo.getUsers().stream()
                    .map(User::getUsername)
                    .collect(Collectors.toCollection(ArrayList::new));
        } else {
            System.err.println("Error: ToDo '" + toDoTitle + "' not found in board '" + boardNameStr + "'.");
            return new ArrayList<>();
        }
    }
}