package controller;

import models.*;

public class Controller {
    public User user;

    public Controller(){

    }

    public void register(String username, String password){
        this.user = new User(username, password);
    }
    //todo qua dobbiamo salvare nel db
}
