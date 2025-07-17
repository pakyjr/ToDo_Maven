package gui;

import controller.Controller;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class RegisterForm {
    private JPanel register;
    private JTextField userField1;
    private JButton registerButton;
    private JPasswordField passwordField1;
    public JFrame frameRegisterForm, frame;

    private Controller controller;
    public RegisterForm(JFrame parent, Controller c){
        this.controller = c;
        this.frame = parent;

        frameRegisterForm=new JFrame("Registrazione");
        frameRegisterForm.setContentPane(register);
        frameRegisterForm.pack();

        frameRegisterForm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                frame.setVisible(true);
                frameRegisterForm.dispose();
            }
        });

        registerButton.addActionListener(e -> registraUtente());
    }

    private void registraUtente() {
        String username = userField1.getText();
        String password = new String(passwordField1.getPassword());
        if(username.isEmpty() || password.isEmpty()){
            JOptionPane.showMessageDialog(frameRegisterForm, "You need to insert username and password.", "Error while creating the account", JOptionPane.ERROR_MESSAGE);
        }
        else{
            controller.register(username, password);
            if (controller.user != null) {
                frameRegisterForm.setVisible(false);

                BoardForm boardForm = new BoardForm(frameRegisterForm, controller);
                boardForm.frameBoardForm.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(frameRegisterForm, "Registration failed. Username might already exist or a database error occurred.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}