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

    // Constructor for new ToDos
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

    // Constructor for loading from DB
    public ToDo(UUID id, String title, String owner) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.createdDate = LocalDate.now();
        this.activityList = new HashMap<>();
        this.sharedUsers = new HashSet<>();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    /**
     * Adds an activity to the ToDo's activity list.
     * @param activityTitle The title of the activity.
     */
    public void addActivity(String activityTitle) {
        this.activityList.put(activityTitle, false);
    }

    /**
     * Deletes an activity from the ToDo's activity list.
     * @param activityTitle The title of the activity to delete.
     */
    public void deleteActivity(String activityTitle) {
        this.activityList.remove(activityTitle);
    }

    /**
     * Retrieves the set of users this ToDo is shared with.
     * @return A Set of User objects.
     */
    public Set<User> getUsers() {
        return sharedUsers;
    }

    /**
     * Adds a user to the set of users this ToDo is shared with.
     * @param user The User object to add.
     */
    public void addSharedUser(User user) {
        if (user != null) {
            this.sharedUsers.add(user);
        }
    }

    /**
     * Removes a user from the set of users this ToDo is shared with by username.
     * @param username The username of the user to remove.
     */
    public void removeSharedUser(String username) {
        this.sharedUsers.removeIf(u -> u.getUsername().equals(username));
    }

    /**
     * Clears all users this ToDo is shared with.
     * This is typically called when the original ToDo is deleted by its owner.
     */
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