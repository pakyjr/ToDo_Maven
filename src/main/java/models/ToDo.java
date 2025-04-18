package models;

import java.util.ArrayList;
import java.util.Date;
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
    private List<String> users;
    private String color = "white";
    private boolean done = false;

    public ToDo(String title) {
        this.title = title;
        this.users = new ArrayList<>();
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
}