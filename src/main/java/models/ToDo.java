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
    private String color = "white";
    private boolean done = false;
    private Map<String, Boolean> activityList;

    public ToDo(String title) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.users = new HashSet<>();
        this.activityList = new HashMap<>();
    }

    public ToDo(ToDo toDo){
        this.id = toDo.getId();
        this.title = toDo.getTitle();
        this.activityList = toDo.getActivityList();
        this.description = toDo.getDescription();
        this.owner = toDo.getOwner();
        this.url = toDo.getUrl();
        this.image = toDo.getImage();
        this.dueDate = toDo.getDueDate();
        this.position = toDo.getPosition();
        this.users = toDo.getUsers();
        this.color = toDo.getColor();
        this.done = toDo.getDone();
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

    public void addActivity(String title) {
        this.activityList.put(title, false);
        System.out.printf("Added activity: %s%n", title);
    }

    public void deleteActivity(String title) {
        this.activityList.remove(title);
        System.out.printf("Deleted activity: %s%n", title);
    }

    public void setActivityStatus(String title, boolean completed) {
        if (!checkValidActivity(title)) {
            return;
        }

        this.activityList.put(title, completed);
        updateDoneStatus();
    }

    public void setActivityTrue(String title) {
        setActivityStatus(title, true);
    }

    public void setActivityFalse(String title) {
        setActivityStatus(title, false);
    }

    private boolean checkValidActivity(String title) {
        if (!activityList.containsKey(title)) {
            System.out.println("Invalid activity: " + title);
            return false;
        }
        return true;
    }

    private void updateDoneStatus() {
        this.done = !activityList.isEmpty() && activityList.values().stream().allMatch(Boolean::booleanValue);
    }

    public void toggle() {
        this.done = !done;
    }

    public Map<String, Boolean> getActivityList() {
        return new HashMap<>(activityList);
    }

    public Set<User> getUsers() {
        return new HashSet<>(users);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public UUID getId() {
        return this.id;
    }
}