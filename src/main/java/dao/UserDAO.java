package dao;

import models.User;

public interface UserDAO {
    void saveUser(User user);
    User getUser(String username);
    void deleteUser(String username);
    void updateUser(User user);
}