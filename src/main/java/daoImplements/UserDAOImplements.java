package daoImplements;

import dao.UserDAO;
import models.User;

import java.util.HashMap;
import java.util.Map;

public class UserDAOImplements implements UserDAO {
    private final Map<String, User> userDatabase = new HashMap<>();

    @Override
    public void saveUser(User user) {
        if (userDatabase.containsKey(user.getUsername())) {
            System.out.println("User already exists.");
            return;
        }
        userDatabase.put(user.getUsername(), user);
        System.out.println("User saved: " + user.getUsername());
    }

    @Override
    public User getUser(String username) {
        if (!userDatabase.containsKey(username)) {
            System.out.println("User not found.");
            return null;
        }
        return userDatabase.get(username);
    }

    @Override
    public void deleteUser(String username) {
        if (!userDatabase.containsKey(username)) {
            System.out.println("User not found.");
            return;
        }
        userDatabase.remove(username);
        System.out.println("User deleted: " + username);
    }

    @Override
    public void updateUser(User user) {
        if (!userDatabase.containsKey(user.getUsername())) {
            System.out.println("User does not exist.");
            return;
        }
        userDatabase.put(user.getUsername(), user);
        System.out.println("User updated: " + user.getUsername());
    }
}