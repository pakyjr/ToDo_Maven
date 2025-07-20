package gui;

import controller.Controller;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * Classe che gestisce l'interfaccia grafica per la registrazione di nuovi utenti.
 * Presenta un modulo con campi per username e password e gestisce il processo
 * di registrazione tramite un controller.
 */
public class RegisterForm {
    /** Pannello principale del form di registrazione */
    private JPanel register;

    /** Campo di testo per l'inserimento del nome utente */
    private JTextField userField1;

    /** Pulsante per avviare il processo di registrazione */
    private JButton registerButton;

    /** Campo per l'inserimento della password (nascosta) */
    private JPasswordField passwordField1;

    /** Frame principale del form di registrazione e riferimento al frame genitore */
    public JFrame frameRegisterForm, frame;

    /** Controller per gestire la logica di business della registrazione */
    private Controller controller;

    /**
     * Costruttore della classe RegisterForm.
     * Inizializza l'interfaccia grafica, configura il frame e imposta i listener
     * per la gestione degli eventi.
     *
     * @param parent Il frame genitore da cui viene aperta questa finestra
     * @param c Il controller che gestisce la logica di registrazione
     */
    public RegisterForm(JFrame parent, Controller c){
        this.controller = c;
        this.frame = parent;

        frameRegisterForm=new JFrame("Registrazione");
        frameRegisterForm.setContentPane(register);
        frameRegisterForm.pack();

        // Listener per gestire la chiusura della finestra
        frameRegisterForm.addWindowListener(new WindowAdapter() {
            /**
             * Gestisce l'evento di chiusura della finestra.
             * Rende nuovamente visibile il frame genitore e chiude quello corrente.
             *
             * @param windowEvent L'evento di chiusura della finestra
             */
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                frame.setVisible(true);
                frameRegisterForm.dispose();
            }
        });

        // Associa l'azione di registrazione al pulsante
        registerButton.addActionListener(e -> registraUtente());
    }

    /**
     * Metodo privato che gestisce il processo di registrazione dell'utente.
     * Valida i dati inseriti (username e password non vuoti), effettua la registrazione
     * tramite il controller e gestisce il successo o il fallimento dell'operazione.
     *
     * In caso di successo, nasconde il form di registrazione e apre il form della board.
     * In caso di errore, mostra un messaggio di errore appropriato.
     */
    private void registraUtente() {
        String username = userField1.getText();
        String password = new String(passwordField1.getPassword());

        // Validazione dei campi obbligatori
        if(username.isEmpty() || password.isEmpty()){
            JOptionPane.showMessageDialog(frameRegisterForm,
                    "You need to insert username and password.",
                    "Error while creating the account",
                    JOptionPane.ERROR_MESSAGE);
        }
        else{
            // Tentativo di registrazione tramite controller
            controller.register(username, password);

            if (controller.user != null) {
                // Registrazione riuscita: nasconde il form corrente e apre la board
                frameRegisterForm.setVisible(false);

                BoardForm boardForm = new BoardForm(frameRegisterForm, controller);
                boardForm.frameBoardForm.setVisible(true);
            } else {
                // Registrazione fallita: mostra messaggio di errore
                JOptionPane.showMessageDialog(frameRegisterForm,
                        "Registration failed. Username might already exist or a database error occurred.",
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
