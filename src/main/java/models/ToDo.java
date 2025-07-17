package models;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

    public ToDo(UUID id, String title, String owner) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.createdDate = LocalDate.now();
        this.activityList = new HashMap<>();
        this.sharedUsers = new HashSet<>();
    }

    public UUID getId() {
        return id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Map<String, Boolean> getActivityList() {
        return activityList;
    }

    public void setActivityList(Map<String, Boolean> activityList) {
        this.activityList = activityList;
    }

    public void addActivity(String activityTitle) {
        this.activityList.put(activityTitle, false);
    }

    public void deleteActivity(String activityTitle) {
        this.activityList.remove(activityTitle);
    }

    public Set<User> getUsers() {
        return sharedUsers;
    }

    public void addSharedUser(User user) {
        if (user != null) {
            this.sharedUsers.add(user);
        }
    }

    public void removeSharedUser(String username) {
        this.sharedUsers.removeIf(u -> u.getUsername().equals(username));
    }

    public void clearUsers() {
        this.sharedUsers.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToDo toDo = (ToDo) o;
        return id.equals(toDo.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}