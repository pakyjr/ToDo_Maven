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
    private String owner;
    private Set<User> users;
    private String color = "Blu";
    private boolean done = false;
    private Map<String, Boolean> activityList;
    private String status;

    public ToDo(String title) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.users = new HashSet<>();
        this.activityList = new LinkedHashMap<>();
        this.status = "In Progress";
        this.dueDate = null;
        this.url = "";
        this.image = "";
        this.description = "";
        this.owner = "";
    }


    public ToDo(ToDo toDo){
        this.id = toDo.getId();
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

    public boolean isDone() {
        return done;
    }

    public Map<String, Boolean> getActivityList() {
        return new LinkedHashMap<>(activityList);
    }


    public void setActivityList(Map<String, Boolean> activityList) {

        this.activityList = new LinkedHashMap<>(activityList);
        updateOverallStatus();
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


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if ("Completo".equals(status)) {
            this.done = true;

        } else {
            this.done = false;
        }
    }

    public void addActivity(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.activityList.put(title, false); // New activities are incomplete by default
            System.out.printf("Added activity: %s%n", title);
            updateOverallStatus(); // Update overall status when activity is added
        }
    }

    public void deleteActivity(String title) {
        if (activityList.remove(title) != null) { // remove returns the value associated with key, or null if not found
            System.out.printf("Deleted activity: %s%n", title);
            updateOverallStatus(); // Update overall status when activity is deleted
        } else {
            System.out.println("Activity not found: " + title);
        }
    }

    public void setActivityStatus(String title, boolean completed) {
        if (activityList.containsKey(title)) {
            this.activityList.put(title, completed);
            updateOverallStatus(); // Update the textual status and the 'done' boolean
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

        if (this.done && !activityList.isEmpty()) {
            activityList.replaceAll((k, v) -> true);
        } else if (!this.done && !activityList.isEmpty()) {

            activityList.replaceAll((k, v) -> false);
        }
        updateOverallStatus(); // Ensure textual status is updated after toggle
    }


    private void updateOverallStatus() {
        if (activityList.isEmpty()) {
            this.status = "In Progress";
            this.done = false;
        } else {
            boolean allCompleted = activityList.values().stream().allMatch(Boolean::booleanValue);
            if (allCompleted) {
                this.status = "Completo";
                this.done = true;
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