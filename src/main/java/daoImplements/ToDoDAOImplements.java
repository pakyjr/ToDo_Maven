package daoImplements;

import dao.ToDoDAO;
import db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;

public class ToDoDAOImplements implements ToDoDAO {

    @Override
    public void addToDo(String title, String description) {
        try (Connection conn = DatabaseConnection.getInstance()) {
            String sql = "INSERT INTO ToDo (title, description) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void getAllToDo(ArrayList<String> titles, ArrayList<String> descriptions) {
        try (Connection conn = DatabaseConnection.getInstance()) {
            String sql = "SELECT * FROM ToDo";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                titles.add(rs.getString("title"));
                descriptions.add(rs.getString("description"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
