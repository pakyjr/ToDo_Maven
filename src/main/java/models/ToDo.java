package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToDo {
    private int position;
    private Date dueDate;
    private String url;
    private String image;
    private String title;
    private String description;
    private String owner;
    private List<String> users;
    private String color = "white";
    private boolean done = false;
    private Map<String, Boolean> activityList;

    public ToDo(String title) {
        this.title = title;
        this.users = new ArrayList<>();
        this.activityList = new HashMap<>();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
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

    public List<String> getUsers() {
        return new ArrayList<>(users);
    }

    public void addUser(String username) {
        if (!users.contains(username)) {
            users.add(username);
        }
    }

    public void removeUser(String username) {
        users.remove(username);
    }
}