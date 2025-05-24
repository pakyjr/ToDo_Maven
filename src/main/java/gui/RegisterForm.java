package gui;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RegisterForm {
    private JPanel register;
    private JTextField userField1;
    private JButton registerButton;
    private JPasswordField passwordField1;
    public JFrame frameRegisterForm;

    public RegisterForm(JFrame parent){

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
        if(username.equals("") || password.equals("")){
            JOptionPane.showMessageDialog(frameRegisterForm, "You need to insert username and password.", "Error while creating the account", JOptionPane.ERROR_MESSAGE);
        }
        else{
            frameRegisterForm.setVisible(false);

            BoardForm boardForm = new BoardForm(frameRegisterForm);
            boardForm.frameBoardForm.setVisible(true);

            System.out.println("Utente registrato:");
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
        }

        // Qui puoi salvare i dati su file o database


    }

}

