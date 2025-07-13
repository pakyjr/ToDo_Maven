package models;

import java.time.LocalDate;
import java.util.*;
        import java.util.stream.Collectors;

public class ToDo {
    private UUID id;
    private int position;
    private LocalDate dueDate;
    private String url;
    private String image; // Stores the name of the image file (e.g., "lupo.png")
    private String title;
    private String description;
    private String owner; // Assuming owner is a String (e.g., username)
    private Set<User> users; // For shared ToDos
    private String color = "Blu"; // Default color to match GUI's default
    private boolean done = false; // Derived from activityList completion
    private Map<String, Boolean> activityList; // Activity name -> Completion status
    private String status; // Textual status (e.g., "Non avviato", "In Progresso", "Completo")

    // Constructor for new ToDos
    public ToDo(String title) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.users = new HashSet<>();
        this.activityList = new LinkedHashMap<>(); // Use LinkedHashMap to maintain insertion order for display
        this.status = "Non avviato"; // Default status for new ToDos
        // Default values for other fields, might be set later
        this.dueDate = null;
        this.url = "";
        this.image = "";
        this.description = "";
        this.owner = ""; // Consider setting a default owner or having it set upon creation by a User
    }

    // Copy constructor to ensure all fields, including the new status and activityList, are copied
    public ToDo(ToDo toDo){
        this.id = toDo.getId();
        this.title = toDo.getTitle();
        // Deep copy activityList to ensure changes to the copy don't affect the original
        this.activityList = new LinkedHashMap<>(toDo.getActivityList());
        this.description = toDo.getDescription();
        this.owner = toDo.getOwner();
        this.url = toDo.getUrl();
        this.image = toDo.getImage();
        this.dueDate = toDo.getDueDate();
        this.position = toDo.getPosition();
        // Deep copy users set
        this.users = new HashSet<>(toDo.getUsers());
        this.color = toDo.getColor();
        this.done = toDo.getDone();
        this.status = toDo.getStatus(); // Copy the current textual status
    }

    // --- Getters and Setters ---

    public UUID getId() {
        return this.id;
    }

    public boolean getDone(){
        // The 'done' boolean should reflect the completion of all activities.
        // It's effectively derived from `updateOverallStatus()`.
        return this.done;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    // `isDone()` is a common convention for boolean getters without "get" prefix
    public boolean isDone() {
        return done;
    }

    public Map<String, Boolean> getActivityList() {
        // Return a copy to prevent external modification of the internal map
        return new LinkedHashMap<>(activityList);
    }

    // Setter for activityList to be used when loading from persistent storage or GUI
    public void setActivityList(Map<String, Boolean> activityList) {
        // Set with a new copy to ensure no direct external reference
        this.activityList = new LinkedHashMap<>(activityList);
        // Recalculate status and done flag when the activity list is set/updated
        updateOverallStatus();
    }

    public Set<User> getUsers() {
        // Return a copy to prevent external modification of the internal set
        return new HashSet<>(users);
    }

    public void addUser(User user) {
        if (user != null) {
            users.add(user);
        }
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    // Getter for the textual status
    public String getStatus() {
        return status;
    }

    // Setter for the textual status.
    // When the status is explicitly set, we should make sure 'done' aligns,
    // but not necessarily re-evaluate `activityList` completion.
    public void setStatus(String status) {
        this.status = status;
        if ("Completo".equals(status)) {
            this.done = true;
            // Optionally, if an external "Completo" status means all activities are done,
            // you might want to mark them all as true. Be careful with side effects.
            // activityList.replaceAll((k, v) -> true);
        } else {
            this.done = false;
        }
    }


    // --- Activity Management Methods ---

    /**
     * Adds a new activity to the ToDo list, defaulting its status to incomplete (false).
     * Automatically updates the overall status of the ToDo.
     * @param title The title/description of the activity.
     */
    public void addActivity(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.activityList.put(title, false); // New activities are incomplete by default
            System.out.printf("Added activity: %s%n", title);
            updateOverallStatus(); // Update overall status when activity is added
        }
    }

    /**
     * Deletes an activity from the ToDo list.
     * Automatically updates the overall status of the ToDo.
     * @param title The title/description of the activity to delete.
     */
    public void deleteActivity(String title) {
        if (activityList.remove(title) != null) { // remove returns the value associated with key, or null if not found
            System.out.printf("Deleted activity: %s%n", title);
            updateOverallStatus(); // Update overall status when activity is deleted
        } else {
            System.out.println("Activity not found: " + title);
        }
    }

    /**
     * Sets the completion status of a specific activity.
     * Automatically updates the overall status of the ToDo.
     * @param title The title of the activity.
     * @param completed True if the activity is completed, false otherwise.
     */
    public void setActivityStatus(String title, boolean completed) {
        if (activityList.containsKey(title)) {
            this.activityList.put(title, completed);
            updateOverallStatus(); // Update the textual status and the 'done' boolean
        } else {
            System.out.println("Invalid activity: " + title);
        }
    }

    /**
     * Marks a specific activity as completed.
     * @param title The title of the activity.
     */
    public void setActivityTrue(String title) {
        setActivityStatus(title, true);
    }

    /**
     * Marks a specific activity as incomplete.
     * @param title The title of the activity.
     */
    public void setActivityFalse(String title) {
        setActivityStatus(title, false);
    }

    /**
     * Toggles the 'done' status of the entire ToDo.
     * Note: If activities are present, this might override their individual states.
     * Consider if this method is truly needed, as 'done' is usually derived from activities.
     */
    public void toggle() {
        this.done = !done;
        // If toggling the ToDo directly, how should it affect individual activities?
        // Option 1: If marking ToDo as done, mark all activities as done.
        if (this.done && !activityList.isEmpty()) {
            activityList.replaceAll((k, v) -> true);
        } else if (!this.done && !activityList.isEmpty()) {
            // Option 2: If marking ToDo as not done, mark all activities as not done.
            activityList.replaceAll((k, v) -> false);
        }
        updateOverallStatus(); // Ensure textual status is updated after toggle
    }

    /**
     * Internal method to update the textual status and the 'done' boolean based on activity completion.
     * This method should be called whenever the `activityList` is modified.
     */
    private void updateOverallStatus() {
        if (activityList.isEmpty()) {
            this.status = "Non avviato";
            this.done = false;
        } else {
            boolean allCompleted = activityList.values().stream().allMatch(Boolean::booleanValue);
            if (allCompleted) {
                this.status = "Completo";
                this.done = true;
            } else {
                this.status = "In Progresso";
                this.done = false;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToDo toDo = (ToDo) o;
        return Objects.equals(id, toDo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ToDo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", dueDate=" + dueDate +
                ", activities=" + activityList.size() +
                ", done=" + done +
                '}';
    }
}