package models;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Rappresenta un'attività (ToDo) appartenente a una board, con informazioni
 * come titolo, descrizione, scadenza, stato e utenti con cui è condivisa.
 */
public class ToDo {
    private UUID id;
    private String title;
    private String description;
    private String status;
    private LocalDate dueDate;
    private LocalDate createdDate;
    private int position;
    private String owner;
    private String url;
    private String color;
    private String image;
    private Map<String, Boolean> activityList;
    private Set<User> sharedUsers;

    /**
     * Crea un nuovo ToDo con titolo e proprietario specificati.
     * @param title Titolo dell'attività
     * @param owner Nome del proprietario
     */
    public ToDo(String title, String owner) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.owner = owner;
        this.createdDate = LocalDate.now();
        this.activityList = new HashMap<>();
        this.sharedUsers = new HashSet<>();
        this.status = "Not Started";
        this.position = 0;
    }

    /**
     * Costruttore con ID specificato (utile per il caricamento da DB).
     * @param id Identificativo univoco
     * @param title Titolo dell'attività
     * @param owner Proprietario dell'attività
     */
    public ToDo(UUID id, String title, String owner) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.createdDate = LocalDate.now();
        this.activityList = new HashMap<>();
        this.sharedUsers = new HashSet<>();
    }

    /** @return UUID dell'attività */
    public UUID getId() {
        return id;
    }

    /** @return Titolo dell'attività */
    public String getTitle() {
        return title;
    }

    /**
     * Imposta il titolo dell'attività.
     * @param title Nuovo titolo
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /** @return Descrizione dell'attività */
    public String getDescription() {
        return description;
    }

    /**
     * Imposta la descrizione dell'attività.
     * @param description Nuova descrizione
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return Stato dell'attività */
    public String getStatus() {
        return status;
    }

    /**
     * Imposta lo stato dell'attività.
     * @param status Stato (es. "In corso", "Completato")
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /** @return Data di scadenza */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Imposta la data di scadenza.
     * @param dueDate Data di scadenza
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /** @return Data di creazione dell'attività */
    public LocalDate getCreatedDate() {
        return createdDate;
    }

    /**
     * Imposta la data di creazione (utile per il caricamento da DB).
     * @param createdDate Data di creazione
     */
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    /** @return Posizione dell'attività nella board */
    public int getPosition() {
        return position;
    }

    /**
     * Imposta la posizione dell'attività nella board.
     * @param position Posizione (0-based)
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /** @return Proprietario dell'attività */
    public String getOwner() {
        return owner;
    }

    /**
     * Imposta il proprietario dell'attività.
     * @param owner Nuovo proprietario
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /** @return URL associato all'attività (es. link esterni) */
    public String getUrl() {
        return url;
    }

    /**
     * Imposta l'URL dell'attività.
     * @param url URL da associare
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /** @return Colore associato all'attività */
    public String getColor() {
        return color;
    }

    /**
     * Imposta il colore dell'attività.
     * @param color Colore in formato esadecimale o nome
     */
    public void setColor(String color) {
        this.color = color;
    }

    /** @return URL o percorso dell'immagine associata */
    public String getImage() {
        return image;
    }

    /**
     * Imposta l'immagine dell'attività.
     * @param image Percorso o URL immagine
     */
    public void setImage(String image) {
        this.image = image;
    }

    /** @return Mappa delle attività secondarie (titolo → completata?) */
    public Map<String, Boolean> getActivityList() {
        return activityList;
    }

    /**
     * Imposta la lista di attività secondarie.
     * @param activityList Mappa titolo-attività / stato completamento
     */
    public void setActivityList(Map<String, Boolean> activityList) {
        this.activityList = activityList;
    }

    /**
     * Aggiunge una nuova attività secondaria (non completata).
     * @param activityTitle Titolo della sotto-attività
     */
    public void addActivity(String activityTitle) {
        this.activityList.put(activityTitle, false);
    }

    /**
     * Elimina una sotto-attività in base al titolo.
     * @param activityTitle Titolo della sotto-attività da rimuovere
     */
    public void deleteActivity(String activityTitle) {
        this.activityList.remove(activityTitle);
    }

    /** @return Insieme degli utenti con cui l'attività è condivisa */
    public Set<User> getUsers() {
        return sharedUsers;
    }

    /**
     * Aggiunge un utente con cui l'attività è condivisa.
     * @param user Oggetto utente da condividere
     */
    public void addSharedUser(User user) {
        if (user != null) {
            this.sharedUsers.add(user);
        }
    }

    /**
     * Rimuove un utente condiviso in base al nome utente.
     * @param username Username dell'utente da rimuovere
     */
    public void removeSharedUser(String username) {
        this.sharedUsers.removeIf(u -> u.getUsername().equals(username));
    }

    /**
     * Rimuove tutti gli utenti condivisi.
     */
    public void clearUsers() {
        this.sharedUsers.clear();
    }

    /**
     * Confronta due ToDo in base all'ID.
     * @param o Oggetto da confrontare
     * @return true se hanno lo stesso ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToDo toDo = (ToDo) o;
        return id.equals(toDo.id);
    }

    /**
     * Calcola l'hashCode in base all'ID.
     * @return HashCode dell'oggetto
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
