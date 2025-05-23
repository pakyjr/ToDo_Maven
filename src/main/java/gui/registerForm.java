package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class registerForm {
    private JPanel register;
    private JTextField userField1;
    private JButton registerButton;
    private JPasswordField passwordField1;
    public JFrame frameRegisterForm;

    public registerForm(JFrame parent){

        frameRegisterForm=new JFrame("Registrazione");
        frameRegisterForm.setContentPane(register);
        frameRegisterForm.pack();

        frameRegisterForm.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {

            }
        });

        registerButton.addActionListener(e -> registraUtente());
    }

    private void registraUtente() {
        String username = userField1.getText();
        String password = new String(passwordField1.getPassword());

        // Qui puoi salvare i dati su file o database
        System.out.println("Utente registrato:");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
    }

}

