package daoImplements;

import dao.ToDoDAO;
import db.DatabaseConnection;
import models.ToDo;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ToDoDAOImplements implements ToDoDAO {

    @Override
    public void addToDo(ToDo toDo) {
        String sql = "INSERT INTO ToDo (id, title, description, owner, url, image, position, color, done, dueDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, toDo.getId());
            stmt.setString(2, toDo.getTitle());
            stmt.setString(3, toDo.getDescription());
            stmt.setString(4, toDo.getOwner());
            stmt.setString(5, toDo.getUrl());
            stmt.setString(6, toDo.getImage());
            stmt.setInt(7, toDo.getPosition());
            stmt.setString(8, toDo.getColor());
            stmt.setBoolean(9, toDo.getDone());

            if (toDo.getDueDate() != null) {
                stmt.setDate(10, Date.valueOf(toDo.getDueDate()));
            } else {
                stmt.setNull(10, Types.DATE);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ToDo> getAllToDos() {
        List<ToDo> todos = new ArrayList<>();
        String sql = "SELECT * FROM ToDo";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ToDo todo = new ToDo(rs.getString("title"));
                todo.setDescription(rs.getString("description"));
                todo.setOwner(rs.getString("owner"));
                todo.setUrl(rs.getString("url"));
                todo.setImage(rs.getString("image"));
                todo.setPosition(rs.getInt("position"));
                todo.setColor(rs.getString("color"));
                todo.setDueDate(rs.getDate("dueDate") != null ? rs.getDate("dueDate").toLocalDate() : null);

                // override the generated UUID with the one from DB
                try {
                    java.lang.reflect.Field idField = ToDo.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(todo, UUID.fromString(rs.getString("id")));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (rs.getBoolean("done")) {
                    todo.toggle(); // toggles false → true
                }

                todos.add(todo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return todos;
    }

    @Override
    public void updateToDo(ToDo toDo) {
        String sql = "UPDATE ToDo SET title = ?, description = ?, owner = ?, url = ?, image = ?, position = ?, color = ?, done = ?, dueDate = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, toDo.getTitle());
            stmt.setString(2, toDo.getDescription());
            stmt.setString(3, toDo.getOwner());
            stmt.setString(4, toDo.getUrl());
            stmt.setString(5, toDo.getImage());
            stmt.setInt(6, toDo.getPosition());
            stmt.setString(7, toDo.getColor());
            stmt.setBoolean(8, toDo.getDone());

            if (toDo.getDueDate() != null) {
                stmt.setDate(9, Date.valueOf(toDo.getDueDate()));
            } else {
                stmt.setNull(9, Types.DATE);
            }

            stmt.setObject(10, toDo.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteToDo(UUID id) {
        String sql = "DELETE FROM ToDo WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}