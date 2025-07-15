

package models;

import java.time.LocalDate;
import java.util.*;

public class ToDo {
    private UUID id;
    private int position;
    private LocalDate dueDate;
    private String url;
    private String image;
    private String title;
    private String description;
    private String owner; // Changed to String, representing the username of the creator
    private Set<User> users; // This likely represents users the ToDo is shared *with*, or can view/edit
    private String color = "Blue";
    private boolean done = false;
    private Map<String, Boolean> activityList;
    private String status;

    public ToDo(String title) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.users = new HashSet<>();
        this.activityList = new LinkedHashMap<>();
        this.status = "Not Started";
        this.dueDate = null;
        this.url = "";
        this.image = "";
        this.description = "";
        this.owner = ""; // Will be set by the Controller upon creation
    }

    /**
     * Copy constructor for creating a new ToDo instance, typically for sharing.
     * It generates a new UUID for the new instance but copies all other properties,
     * crucially including the 'owner' (creator) from the original ToDo.
     * @param toDo The original ToDo object to copy.
     */
    public ToDo(ToDo toDo){
        this.id = UUID.randomUUID(); // Generate a NEW UUID for the copied ToDo instance
        this.title = toDo.getTitle();
        this.activityList = new LinkedHashMap<>(toDo.getActivityList());
        this.description = toDo.getDescription();
        this.owner = toDo.getOwner(); // Preserve the original owner's username
        this.url = toDo.getUrl();
        this.image = toDo.getImage();
        this.dueDate = toDo.getDueDate();
        this.position = toDo.getPosition();
        // For shared ToDos, the 'users' set might need to be re-evaluated.
        // If 'users' tracks who it's shared *with* from the creator's perspective,
        // then a shared copy doesn't need to inherit this set directly, as it's a new instance.
        // If 'users' tracks who has access, then this copy will implicitly be for one user (the recipient).
        // For simplicity, let's assume 'users' on the original ToDo primarily indicates who it's been shared *to*.
        // The copy itself is for a single user, so its own 'users' set might remain empty unless re-shared.
        this.users = new HashSet<>(); // The copied ToDo starts with no explicit shared users (itself is the recipient)
        this.color = toDo.getColor();
        this.done = toDo.getDone();
        this.status = toDo.getStatus();
    }

    // --- All your existing getters and setters are fine, just ensuring 'owner' is present ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean getDone(){
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

    // Getter and Setter for 'owner'
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

    public boolean isDone() {
        return done;
    }

    public Map<String, Boolean> getActivityList() {
        return new LinkedHashMap<>(activityList);
    }

    public void setActivityList(Map<String, Boolean> activityList) {
        if (activityList != null) {
            this.activityList = new LinkedHashMap<>(activityList);
            updateOverallStatus();
        } else {
            this.activityList = new LinkedHashMap<>();
            updateOverallStatus();
        }
    }

    public Set<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        if (user != null) {
            users.add(user);
        }
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public void clearUsers() {
        this.users.clear();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status != null && !status.trim().isEmpty()) {
            this.status = status;

            this.done = "Completo".equalsIgnoreCase(status) || "Complete".equalsIgnoreCase(status);

            if (this.done) {
                activityList.replaceAll((k, v) -> true);
            } else if ("Not Started".equalsIgnoreCase(status)) {
                activityList.replaceAll((k, v) -> false);
            }

        }
    }

    public void addActivity(String title) {
        if (title != null && !title.trim().isEmpty() && !activityList.containsKey(title)) {
            this.activityList.put(title, false);
            System.out.printf("Added activity: %s%n", title);
            updateOverallStatus();
        } else if (activityList.containsKey(title)) {
            System.out.println("Activity '" + title + "' already exists.");
        } else {
            System.out.println("Activity title cannot be empty.");
        }
    }

    public void deleteActivity(String title) {
        if (activityList.remove(title) != null) {
            System.out.printf("Deleted activity: %s%n", title);
            updateOverallStatus();
        } else {
            System.out.println("Activity not found: " + title);
        }
    }

    public void setActivityStatus(String title, boolean completed) {
        if (activityList.containsKey(title)) {
            this.activityList.put(title, completed);
            updateOverallStatus();
        } else {
            System.out.println("Invalid activity: " + title);
        }
    }

    public void setActivityTrue(String title) {
        setActivityStatus(title, true);
    }

    public void setActivityFalse(String title) {
        setActivityStatus(title, false);
    }

    public void toggle() {
        this.done = !done;

        if (this.done) {
            activityList.replaceAll((k, v) -> true);
            this.status = "Complete";
        } else {
            activityList.replaceAll((k, v) -> false);
            this.status = "Incomplete";
        }
    }

    private void updateOverallStatus() {
        if (activityList.isEmpty()) {
            this.status = "Not Started";
            this.done = false;
        } else {
            boolean allCompleted = activityList.values().stream().allMatch(Boolean::booleanValue);
            if (allCompleted) {
                this.status = "Complete";
                this.done = true;
            } else {
                this.status = "Incomplete";
                this.done = false;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToDo toDo = (ToDo) o;
        // Equality is based on the unique ID
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
                ", owner='" + owner + '\'' + // Include owner in toString
                ", activities=" + activityList.size() +
                ", done=" + done +
                '}';
    }
}