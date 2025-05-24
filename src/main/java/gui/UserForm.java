package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserForm {
    private JPanel panel1;
    private JButton loginButton;
    private JButton donTHaveAccountButton;
    private JTextField usernameField1;
    private JPasswordField passwordField1;
    public JFrame frame; // Il JFrame associato a questo form

    public static void main(String[] args) {
        // Crea il JFrame principale
        JFrame mainFrame = new JFrame("Login");
        // Crea un'istanza di userform passando il JFrame
        UserForm loginForm = new UserForm(mainFrame);
        mainFrame.setContentPane(loginForm.panel1);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.setSize(300, 500);

    }

    // Costruttore che accetta il JFrame padre
    public UserForm(JFrame parentFrame) {
        this.frame = parentFrame; // Assegna il JFrame passato

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField1.getText();
                char[] passwordChars = passwordField1.getPassword(); // Get password as char array
                String password = new String(passwordChars); // Convert char array to String

                System.out.println("Username: " + username);
                System.out.println("Password (for demonstration purposes, handle securely in production): " + password);


            }
        });

        donTHaveAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                frame.setVisible(false);

                RegisterForm registerinterface = new RegisterForm(frame);
                registerinterface.frameRegisterForm.setVisible(true);
            }
        });
    }

}