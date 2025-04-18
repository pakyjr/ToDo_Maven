package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
//TODO handle date

public class ToDo {
    private int position; //
    private Date dueDate;
    private String url;
    private String image;
    private String title;
    private String description;
    private String owner;
    private ArrayList<String> users;
    private String color = "white";
    private boolean done = false;
    private HashMap<String, Boolean> activityList;

    public ToDo(String title) {
        this.title = title;
        this.users = new ArrayList<>();
        this.activityList = new HashMap<>();
    }

    //REGION SET & GET
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

    public void setDescription(String detailedDescription) {
        this.description = detailedDescription;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
    //END REGION

    public void addActivity(String title){
        this.activityList.put(title, false);
        System.out.printf("Added activity: %s\n", title);
    }

    public void deleteActivity(String title){
        this.activityList.remove(title);
        System.out.printf("Deleted activity: %s\n", title);
    }

    public void setActivityTrue(String title){
        if(!checkValidActivity(title)){
            return;
        }
        this.activityList.put(title, true);
        if(activityList.values().stream().allMatch(status -> status)){
            this.done = true;
        }
    }

    public void setActivityFalse(String title){
        if(!checkValidActivity(title)){
            return;
        }
        this.activityList.put(title, false);
        this.done = false;
    }

    private boolean checkValidActivity(String title){
        if(!activityList.containsKey(title)){
            System.out.println("Invalid activity: " + title);
            return false;
        }
        return true;
    }

    public void toggle(){
        this.done = !done;
    }
}