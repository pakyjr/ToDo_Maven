package gui;
import controller.*;
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
        this.controller = new Controller();
        this.frame = parentFrame;

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField1.getText();
                String password = passwordField1.getPassword().toString();

                System.out.println("Username: " + username);
                System.out.println("Password (for demonstration purposes, handle securely in production): " + password);
                frame.setVisible(false);

                BoardForm boardForm = new BoardForm(frame);
                boardForm.frameBoardForm.setVisible(true);
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