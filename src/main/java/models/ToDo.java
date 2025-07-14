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
    private String owner;
    private Set<User> users;
    private String color = "Blu";
    private boolean done = false;
    private Map<String, Boolean> activityList;
    private String status;

    public ToDo(String title) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.users = new HashSet<>(); // Initialize the set for shared users
        this.activityList = new LinkedHashMap<>(); // Maintains insertion order for activities
        this.status = "Not Started"; // Default status
        this.dueDate = null;
        this.url = "";
        this.image = "";
        this.description = "";
        this.owner = "";
    }


    public ToDo(ToDo toDo){
        this.id = toDo.getId(); // UUID is immutable, so direct copy is fine
        this.title = toDo.getTitle();
        this.activityList = new LinkedHashMap<>(toDo.getActivityList());
        this.description = toDo.getDescription();
        this.owner = toDo.getOwner();
        this.url = toDo.getUrl();
        this.image = toDo.getImage();
        this.dueDate = toDo.getDueDate();
        this.position = toDo.getPosition();
        this.users = new HashSet<>(toDo.getUsers());
        this.color = toDo.getColor();
        this.done = toDo.getDone();
        this.status = toDo.getStatus();
    }

    public UUID getId() {
        return this.id;
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

    public boolean isDone() { // Standard getter name for boolean
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
        if (title != null && !title.trim().isEmpty() && !activityList.containsKey(title)) { // Prevent duplicate activities
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
            this.status = "Complete"; // Use "Complete" for consistency
        } else {
            activityList.replaceAll((k, v) -> false);
            this.status = "Incomplete"; // Use "Incomplete" when activities are reset to false
        }
    }

    /**
     * Updates the overall status ("Not Started", "Incomplete", "Complete")
     * and the 'done' flag based on the state of its activities.
     */
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
        // Comparing by UUID is the most robust way to check for equality of ToDo objects
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