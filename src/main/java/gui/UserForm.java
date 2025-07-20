package gui;
import controller.*;
import models.User;
import java.sql.SQLException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Form di login dell'applicazione che gestisce l'autenticazione degli utenti.
 *
 * <p>Questa classe rappresenta l'interfaccia grafica principale per l'accesso al sistema,
 * fornendo campi per username e password, oltre ai pulsanti per il login e la registrazione.
 * La classe implementa il pattern MVC collaborando con il Controller per le operazioni
 * di business logic e gestisce la navigazione tra le diverse form dell'applicazione.</p>
 */
public class UserForm {

    /**
     * Pannello principale che contiene tutti i componenti dell'interfaccia di login.
     * Generato automaticamente dal form designer e utilizzato come content pane del JFrame.
     */
    private JPanel panel1;

    /**
     * Pulsante per avviare il processo di autenticazione.
     * Al click, raccoglie le credenziali inserite e le invia al controller per la validazione.
     */
    private JButton loginButton;

    /**
     * Pulsante per navigare alla form di registrazione per nuovi utenti.
     * Nasconde la form corrente e visualizza la RegisterForm.
     */
    private JButton donTHaveAccountButton;

    /**
     * Campo di testo per l'inserimento dell'username dell'utente.
     * Accetta input testuale senza restrizioni di lunghezza o caratteri.
     */
    private JTextField usernameField1;

    /**
     * Campo password per l'inserimento sicuro della password dell'utente.
     * Maschera automaticamente i caratteri inseriti per sicurezza.
     */
    private JPasswordField passwordField1;

    /**
     * Riferimento statico al frame principale dell'applicazione.
     * Utilizzato per operazioni di visibilità e navigazione tra le form.
     *
     * @deprecated L'uso di campi statici per la gestione delle finestre non è una best practice.
     *             Considerare il refactoring verso un pattern più appropriato.
     */
    public static JFrame frame;

    /**
     * Istanza del controller MVC che gestisce la business logic dell'applicazione.
     * Fornisce metodi per login, registrazione e altre operazioni sui dati.
     */
    private Controller controller;

    /**
     * Metodo main che avvia l'applicazione inizializzando la form di login.
     *
     * <p>Crea e configura il JFrame principale, imposta la UserForm come contenuto,
     * configura le proprietà della finestra e la rende visibile all'utente.
     * Questo è il punto di ingresso dell'applicazione GUI.</p>
     *
     * @param args argomenti da linea di comando (non utilizzati)
     * @see JFrame
     */
    public static void main(String[] args) {
        JFrame mainFrame = new JFrame("Login");
        UserForm loginForm = new UserForm(mainFrame);
        mainFrame.setContentPane(loginForm.panel1);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.setSize(300, 500);
    }

    /**
     * Costruisce una nuova istanza della form di login configurando tutti i componenti e listener.
     *
     * <p>Il costruttore esegue le seguenti operazioni:</p>
     * <ol>
     *   <li>Inizializza il controller per la gestione della business logic</li>
     *   <li>Configura il riferimento al frame genitore</li>
     *   <li>Registra i listener per gli eventi dei pulsanti</li>
     *   <li>Gestisce eventuali errori di inizializzazione del database</li>
     * </ol>
     *
     * <p>In caso di errore durante l'inizializzazione del controller, l'applicazione
     * mostrerà un messaggio di errore user-friendly e terminerà gracefully.</p>
     *
     * @param parentFrame il JFrame che conterrà questa form di login
     * @throws RuntimeException se si verifica un errore critico durante l'inizializzazione
     * @see Controller
     * @see ActionListener
     */
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

        /**
         * Listener per il pulsante di login che gestisce il processo di autenticazione.
         *
         * <p>Quando attivato, questo listener:</p>
         * <ul>
         *   <li>Raccoglie username e password dai campi di input</li>
         *   <li>Invoca il metodo di login del controller</li>
         *   <li>In caso di successo, nasconde la form di login e apre la BoardForm</li>
         *   <li>In caso di fallimento, mostra un messaggio di errore appropriato</li>
         *   <li>Gestisce eventuali eccezioni SQL durante il processo</li>
         * </ul>
         */
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
                        JOptionPane.showMessageDialog(frame,
                                "Invalid username or password.",
                                "Login Error",
                                JOptionPane.ERROR_MESSAGE);
                        System.err.println("Login failed for user: " + username);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Database error during login: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        /**
         * Listener per il pulsante di registrazione che naviga alla form di registrazione.
         *
         * <p>Questo listener gestisce la navigazione dall'interfaccia di login
         * a quella di registrazione per i nuovi utenti, nascondendo la form corrente
         * e visualizzando la RegisterForm con il controller condiviso.</p>
         */
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