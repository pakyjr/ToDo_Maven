package gui;
import controller.*;
import models.User;
import java.sql.SQLException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserForm {
    private JPanel panel1;
    private JButton loginButton;
    private JButton donTHaveAccountButton;
    private JTextField usernameField1;
    private JPasswordField passwordField1;
    public static JFrame frame;

    private Controller controller;

    public static void main(String[] args) {
        JFrame mainFrame = new JFrame("Login");
        UserForm loginForm = new UserForm(mainFrame);
        mainFrame.setContentPane(loginForm.panel1);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.setSize(300, 500);

    }

    public UserForm(JFrame parentFrame) {
        try {
            this.controller = new Controller();
        } catch (SQLException e) {
            System.err.println("Error initializing Controller: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame,
                    "A database error occurred during application startup. Please contact support.",
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        this.frame = parentFrame;

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField1.getText();
                String password = new String(passwordField1.getPassword());

                try {
                    User loggedInUser = controller.login(username, password);
                    if (loggedInUser != null) {
                        System.out.println("Login successful for user: " + loggedInUser.getUsername());
                        frame.setVisible(false);

                        BoardForm boardForm = new BoardForm(frame, controller);
                        boardForm.frameBoardForm.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid username or password.", "Login Error", JOptionPane.ERROR_MESSAGE);
                        System.err.println("Login failed for user: " + username);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Database error during login: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        donTHaveAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);

                RegisterForm registerinterface = new RegisterForm(frame, controller);
                registerinterface.frameRegisterForm.setVisible(true);
            }
        });
    }
}