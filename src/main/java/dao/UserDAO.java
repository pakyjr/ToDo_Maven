package dao;
import java.sql.SQLException;
import java.util.ArrayList;

public interface UserDAO {
    void login(String username,String password) throws SQLException;


    void register(String username,String password) throws SQLException;
}