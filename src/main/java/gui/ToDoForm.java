package gui;

import controller.Controller;
import models.ToDo;
import models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Classe che rappresenta il form per la creazione e modifica dei ToDo.
 * Gestisce l'interfaccia utente per inserire, modificare e visualizzare i dettagli di un ToDo,
 * incluse le attività, la condivisione con altri utenti e la personalizzazione visuale.
 */
public class ToDoForm {
    /** Panel principale del form ToDo */
    private JPanel todoPanel;

    /** Campo di testo per il nome del ToDo */
    private JTextField nameField;

    /** Campo di testo per la descrizione del ToDo */
    private JTextField descriptionField;

    /** Campo di testo per la data di scadenza */
    private JTextField dueDateField;

    /** Campo di testo per l'URL associato al ToDo */
    private JTextField urlField;

    /** Pulsante per salvare il ToDo */
    private JButton buttonSave;

    /** Etichetta per visualizzare l'immagine del ToDo */
    private JLabel image;

    /** ComboBox per selezionare il colore del ToDo */
    private JComboBox <String> colorChange;

    /** Panel per il primo campo */
    private JPanel campo1;

    /** Panel per il secondo campo */
    private JPanel campo2;

    /** Campo di testo per lo stato del ToDo (non modificabile) */
    private JTextField statusField;

    /** Panel che contiene la lista delle attività */
    private JPanel panelActivity;

    /** Pulsante per eliminare attività selezionate */
    private JButton deleteButton;

    /** Pulsante per aprire l'URL nel browser */
    private JButton openURL;

    /** Campo di testo per il proprietario del ToDo (non modificabile) */
    private JTextField ownerfield;

    /** Pulsante per condividere il ToDo con altri utenti */
    private JButton shareToDo;

    /** Pulsante per modificare le impostazioni di condivisione */
    private JButton changeSharing;

    /** ComboBox per visualizzare i membri che hanno accesso al ToDo */
    private JComboBox<String> membersToDo;

    /** Frame principale del form e frame parent */
    public JFrame frameToDoForm, frame;

    /** Nome della board corrente */
    private String currentBoard;

    /** Riferimento al controller principale */
    private Controller controller;

    /** ToDo correntemente in modifica (null per nuovi ToDo) */
    private ToDo currentToDo;

    /** Array dei nomi delle immagini disponibili per i ToDo */
    private String[] imageNames = {"read.jpg", "art.jpg", "happybirthday.jpg", "happyhalloween.jpg", "happynewyear.jpg",
            "santa.jpg", "music.jpg", "choco.jpg", "coffee.jpg", "sweet.jpg","film.jpg",
            "filo.jpg","game.jpg", "graduated.jpg", "mountain.jpg", "pool.jpg", "sher.jpg",
            "theatre.jpg", "sport.jpg", "study.jpg", "martial.jpg"};

    /** Indice dell'immagine correntemente selezionata */
    private int currentImageIndex = 0;

    /**
     * Costruttore della classe ToDoForm.
     * Inizializza il form per la creazione o modifica di un ToDo.
     *
     * @param parent Il frame parent da cui viene aperto questo form
     * @param c Il controller principale dell'applicazione
     * @param cu Il nome della board corrente
     * @param toDoToEdit Il ToDo da modificare (null per creare un nuovo ToDo)
     */
    public ToDoForm(JFrame parent, Controller c, String cu, ToDo toDoToEdit){
        this.frame = parent;
        this.controller = c;
        this.currentBoard = cu;
        this.currentToDo = toDoToEdit;

        // Configurazione del frame principale
        frameToDoForm = new JFrame(toDoToEdit == null ? "ToDo Creation" : "Edit ToDo");
        frameToDoForm.setContentPane(todoPanel);
        todoPanel.setPreferredSize(new Dimension(800,600));
        frameToDoForm.pack();

        // Gestione della chiusura della finestra
        frameToDoForm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (BoardForm.listModel != null) {
                    BoardForm.listModel.clear();
                    BoardForm.listModel.addAll(controller.getToDoListString(currentBoard));
                }
                frame.setVisible(true);
                frameToDoForm.dispose();
            }
        });

        // Inizializzazione della ComboBox dei colori
        this.colorChange.addItem("Blue");
        this.colorChange.addItem("Red");
        this.colorChange.addItem("Yellow");
        this.colorChange.addItem("Green");
        this.colorChange.addItem("Orange");
        this.colorChange.addItem("Violet");

        // Configurazione del renderer per centrare i testi nelle ComboBox
        colorChange.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        membersToDo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        // Impostazione dei colori iniziali
        setPanelColors((String) colorChange.getSelectedItem());

        // Configurazione del layout per il panel delle attività
        panelActivity.setLayout(new BoxLayout(panelActivity, BoxLayout.Y_AXIS));

        // Caricamento dell'immagine iniziale
        loadImage(imageNames[currentImageIndex]);

        // Configurazione dei campi non modificabili
        ownerfield.setEditable(false);
        statusField.setEditable(false);

        // Configurazione per la modifica di un ToDo esistente
        if (currentToDo != null) {
            nameField.setText(currentToDo.getTitle());
            descriptionField.setText(currentToDo.getDescription());

            if (currentToDo.getDueDate() != null) {
                dueDateField.setText(currentToDo.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            urlField.setText(currentToDo.getUrl());

            ownerfield.setText(currentToDo.getOwner());

            populateMembersComboBox();

            // Controllo dei permessi per l'utente corrente
            if (!controller.isCurrentUserToDoCreator(currentToDo)) {
                // Disabilita controlli per utenti non creatori
                image.setEnabled(false);
                nameField.setEditable(false);
                descriptionField.setEditable(false);
                dueDateField.setEditable(false);
                urlField.setEditable(false);
                colorChange.setEnabled(false);
                panelActivity.setEnabled(false);
                buttonSave.setEnabled(false);
                deleteButton.setEnabled(false);
                shareToDo.setEnabled(false);
                changeSharing.setEnabled(false);
                membersToDo.setEnabled(true);
            } else {
                // Abilita controlli per il creatore
                image.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        currentImageIndex = (currentImageIndex + 1) % imageNames.length;
                        loadImage(imageNames[currentImageIndex]);
                    }
                });
                shareToDo.setEnabled(true);
                changeSharing.setEnabled(true);
                membersToDo.setEnabled(true);
            }

            // Ripristino dell'immagine salvata
            String storedImage = currentToDo.getImage();
            if (storedImage != null && !storedImage.isEmpty()) {
                for (int i = 0; i < imageNames.length; i++) {
                    if (imageNames[i].equals(storedImage)) {
                        currentImageIndex = i;
                        loadImage(imageNames[currentImageIndex]);
                        break;
                    }
                }
            } else {
                currentImageIndex = 0;
                loadImage(imageNames[currentImageIndex]);
            }

            // Ripristino del colore salvato
            String storedColor = currentToDo.getColor();
            if (storedColor != null && !storedColor.isEmpty()) {
                for (int i = 0; i < colorChange.getItemCount(); i++) {
                    if (colorChange.getItemAt(i).equals(storedColor)) {
                        colorChange.setSelectedIndex(i);
                        setPanelColors(storedColor);
                        break;
                    }
                }
            }

            // Ripristino delle attività salvate
            if (currentToDo.getActivityList() != null && !currentToDo.getActivityList().isEmpty()) {
                for (Map.Entry<String, Boolean> entry : currentToDo.getActivityList().entrySet()) {
                    JCheckBox checkBox = new JCheckBox(entry.getKey());
                    checkBox.setSelected(entry.getValue());
                    if (!controller.isCurrentUserToDoCreator(currentToDo)) {
                        checkBox.setEnabled(false);
                    } else {
                        checkBox.addItemListener(e -> checkCompletionStatus());
                    }
                    panelActivity.add(checkBox);
                }
                panelActivity.revalidate();
                panelActivity.repaint();
                checkCompletionStatus();
            } else {
                statusField.setText("Not Started");
            }

        } else {
            // Configurazione per un nuovo ToDo
            image.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    currentImageIndex = (currentImageIndex + 1) % imageNames.length;
                    loadImage(imageNames[currentImageIndex]);
                }
            });
            statusField.setText("Not Started");
            ownerfield.setText(controller.user.getUsername());
            membersToDo.setEnabled(false);
            shareToDo.setEnabled(false);
            changeSharing.setEnabled(false);
        }

        // Configurazione degli action listener
        setupActionListeners();
    }

    /**
     * Configura tutti gli action listener per i componenti del form.
     * Metodo privato per organizzare meglio il codice del costruttore.
     */
    private void setupActionListeners() {
        // Action listener per il pulsante di apertura URL
        openURL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openUrlInBrowser();
            }
        });

        // Action listener per il pulsante di salvataggio
        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToDoData();
            }
        });

        // Action listener per il cambio colore
        colorChange.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String colorSelected = (String) colorChange.getSelectedItem();
                setPanelColors(colorSelected);
            }
        });

        // Mouse listener per aggiungere attività
        panelActivity.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                addNewActivity();
            }
        });

        // Action listener per eliminare attività
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedActivities();
            }
        });

        // Action listener per condividere ToDo
        shareToDo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shareToDoWithUsers();
            }
        });

        // Action listener per modificare condivisione
        changeSharing.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeSharingSettings();
            }
        });

        // Action listener per la ComboBox membri (vuoto per ora)
        membersToDo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implementazione futura se necessaria
            }
        });
    }

    /**
     * Apre l'URL specificato nel campo URL utilizzando il browser predefinito del sistema.
     * Gestisce la validazione dell'URL e aggiunge automaticamente il protocollo http se mancante.
     */
    private void openUrlInBrowser() {
        String urlText = urlField.getText().trim();
        if (urlText.isEmpty()) {
            JOptionPane.showMessageDialog(frameToDoForm, "URL field is empty. Please enter a link.", "No URL Provided", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (!urlText.startsWith("http://") && !urlText.startsWith("https://")) {
            urlText = "http://" + urlText;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(urlText));
            } else {
                JOptionPane.showMessageDialog(frameToDoForm, "Your system does not support opening web links automatically.", "Feature Not Supported", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frameToDoForm,
                    "Failed to open URL: " + ex.getMessage() +
                            "\nPlease ensure the link is a valid web address (e.g., www.google.com or https://example.com).",
                    "Error Opening URL", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error attempting to open URL: " + urlText + " - " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Salva i dati del ToDo dopo aver validato tutti i campi obbligatori.
     * Gestisce sia la creazione di nuovi ToDo che l'aggiornamento di quelli esistenti.
     */
    private void saveToDoData() {
        String title = nameField.getText().trim();
        String description = descriptionField.getText().trim();
        String dueDateString = dueDateField.getText().trim();
        String url = urlField.getText().trim();
        String owner = ownerfield.getText();

        // Validazione dei campi obbligatori
        if (title.isEmpty() || description.isEmpty() || dueDateString.isEmpty()) {
            JOptionPane.showMessageDialog(frameToDoForm, "Please fill in all required fields (Title, Description, Due Date).", "Missing Data", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validazione e parsing della data
        LocalDate dueDate;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dueDate = LocalDate.parse(dueDateString, formatter);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(frameToDoForm, "Due Date must be in format dd/MM/yyyy (e.g., 31/12/2025).", "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Raccolta delle attività
        Map<String, Boolean> activitiesMap = new HashMap<>();
        for (Component comp : panelActivity.getComponents()) {
            if (comp instanceof JCheckBox) {
                JCheckBox cb = (JCheckBox) comp;
                activitiesMap.put(cb.getText(), cb.isSelected());
            }
        }

        // Calcolo dello stato
        String calculatedStatus = calculateToDoStatus(activitiesMap);

        String selectedColor = (String) colorChange.getSelectedItem();
        String selectedImageName = imageNames[currentImageIndex];

        // Creazione o aggiornamento del ToDo
        if (currentToDo == null) {
            createNewToDo(title, description, dueDateString, url, selectedColor, selectedImageName, activitiesMap, calculatedStatus, owner);
        } else {
            updateExistingToDo(title, description, dueDateString, url, selectedColor, selectedImageName, activitiesMap, calculatedStatus, dueDate);
        }

        // Aggiornamento della lista nella BoardForm
        updateBoardFormList();

        // Chiusura del form
        frame.setVisible(true);
        frameToDoForm.setVisible(false);
        frameToDoForm.dispose();
    }

    /**
     * Crea un nuovo ToDo con i parametri specificati.
     *
     * @param title Titolo del ToDo
     * @param description Descrizione del ToDo
     * @param dueDateString Data di scadenza in formato stringa
     * @param url URL associato al ToDo
     * @param selectedColor Colore selezionato
     * @param selectedImageName Nome dell'immagine selezionata
     * @param activitiesMap Mappa delle attività con il loro stato
     * @param calculatedStatus Status calcolato del ToDo
     * @param owner Proprietario del ToDo
     */
    private void createNewToDo(String title, String description, String dueDateString, String url,
                               String selectedColor, String selectedImageName, Map<String, Boolean> activitiesMap,
                               String calculatedStatus, String owner) {
        String newToDoId = controller.addToDo(currentBoard, title, description, dueDateString, url, selectedColor, selectedImageName, activitiesMap, calculatedStatus, owner);
        if (newToDoId != null) {
            currentToDo = controller.getToDoByTitle(title, currentBoard);
            if (currentToDo != null) {
                JOptionPane.showMessageDialog(frameToDoForm, "ToDo added successfully.");
                shareToDo.setEnabled(true);
                changeSharing.setEnabled(true);
                membersToDo.setEnabled(true);
                populateMembersComboBox();
            } else {
                JOptionPane.showMessageDialog(frameToDoForm, "Failed to retrieve newly created ToDo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frameToDoForm, "Failed to add ToDo.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Aggiorna un ToDo esistente con i parametri specificati.
     *
     * @param title Nuovo titolo del ToDo
     * @param description Nuova descrizione del ToDo
     * @param dueDateString Nuova data di scadenza in formato stringa
     * @param url Nuovo URL associato al ToDo
     * @param selectedColor Nuovo colore selezionato
     * @param selectedImageName Nuovo nome dell'immagine selezionata
     * @param activitiesMap Nuova mappa delle attività con il loro stato
     * @param calculatedStatus Nuovo status calcolato del ToDo
     * @param dueDate Nuova data di scadenza come oggetto LocalDate
     */
    private void updateExistingToDo(String title, String description, String dueDateString, String url,
                                    String selectedColor, String selectedImageName, Map<String, Boolean> activitiesMap,
                                    String calculatedStatus, LocalDate dueDate) {
        if (!controller.isCurrentUserToDoCreator(currentToDo)) {
            JOptionPane.showMessageDialog(frameToDoForm, "You can only edit ToDos you created.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String oldTitle = currentToDo.getTitle();
        controller.updateToDo(currentBoard, oldTitle, title, description, dueDateString, url, selectedColor, selectedImageName, activitiesMap, calculatedStatus, currentToDo.getOwner());

        // Aggiornamento dell'oggetto currentToDo
        currentToDo.setTitle(title);
        currentToDo.setDescription(description);
        currentToDo.setDueDate(dueDate);
        currentToDo.setUrl(url);
        currentToDo.setActivityList(activitiesMap);
        currentToDo.setColor(selectedColor);
        currentToDo.setImage(selectedImageName);

        JOptionPane.showMessageDialog(frameToDoForm, "ToDo updated successfully.");
    }

    /**
     * Calcola lo stato del ToDo basandosi sulle attività completate.
     *
     * @param activitiesMap Mappa delle attività con il loro stato di completamento
     * @return Stringa rappresentante lo stato calcolato ("Not Started", "Complete", "Incomplete")
     */
    private String calculateToDoStatus(Map<String, Boolean> activitiesMap) {
        if (activitiesMap.isEmpty()) {
            return "Not Started";
        } else {
            boolean allChecked = activitiesMap.values().stream().allMatch(Boolean::booleanValue);
            return allChecked ? "Complete" : "Incomplete";
        }
    }

    /**
     * Aggiorna la lista dei ToDo nella BoardForm se disponibile.
     */
    private void updateBoardFormList() {
        if (BoardForm.listModel != null) {
            BoardForm.listModel.clear();
            BoardForm.listModel.addAll(controller.getToDoListString(currentBoard));
        } else {
            System.err.println("BoardForm.listModel is null. Cannot update the list.");
        }
    }

    /**
     * Gestisce l'aggiunta di una nuova attività al ToDo.
     * Verifica i permessi dell'utente prima di consentire l'aggiunta.
     */
    private void addNewActivity() {
        if (currentToDo != null && !controller.isCurrentUserToDoCreator(currentToDo)) {
            JOptionPane.showMessageDialog(frameToDoForm, "You can only add activities to ToDos you created.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String label = JOptionPane.showInputDialog(panelActivity, "Insert the Activity name:");
        if (label != null && !label.trim().isEmpty()) {
            JCheckBox checkBox = new JCheckBox(label);
            checkBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    checkCompletionStatus();
                }
            });
            panelActivity.add(checkBox);
            panelActivity.revalidate();
            panelActivity.repaint();

            if (currentToDo != null) {
                currentToDo.addActivity(label);
            }
        }
        checkCompletionStatus();
    }

    /**
     * Elimina le attività selezionate dal ToDo dopo conferma dell'utente.
     * Verifica i permessi dell'utente prima di consentire l'eliminazione.
     */
    private void deleteSelectedActivities() {
        if (currentToDo != null && !controller.isCurrentUserToDoCreator(currentToDo)) {
            JOptionPane.showMessageDialog(frameToDoForm, "You can only delete activities from ToDos you created.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ArrayList<JCheckBox> selectedCheckBoxes = new ArrayList<>();
        for (Component comp : panelActivity.getComponents()) {
            if (comp instanceof JCheckBox) {
                JCheckBox cb = (JCheckBox) comp;
                if (cb.isSelected()) {
                    selectedCheckBoxes.add(cb);
                }
            }
        }

        if (selectedCheckBoxes.isEmpty()) {
            JOptionPane.showMessageDialog(frameToDoForm, "Please select at least one activity to delete.", "No Activity Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmResult = JOptionPane.showConfirmDialog(frameToDoForm,
                "Are you sure you want to delete the selected activity(ies)?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirmResult == JOptionPane.YES_OPTION) {
            for (JCheckBox cb : selectedCheckBoxes) {
                panelActivity.remove(cb);
                if (currentToDo != null) {
                    currentToDo.deleteActivity(cb.getText());
                }
            }
            panelActivity.revalidate();
            panelActivity.repaint();
            checkCompletionStatus();
            JOptionPane.showMessageDialog(frameToDoForm, "Selected activity(ies) deleted successfully.");
        }
    }

    /**
     * Gestisce la condivisione del ToDo con altri utenti.
     * Mostra una finestra di dialogo per selezionare gli utenti con cui condividere il ToDo.
     */
    private void shareToDoWithUsers() {
        if (currentToDo == null) {
            JOptionPane.showMessageDialog(frameToDoForm, "Please save the ToDo first before sharing.", "Cannot Share", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!controller.isCurrentUserToDoCreator(currentToDo)) {
            JOptionPane.showMessageDialog(frameToDoForm, "Only the creator can share this ToDo.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Set<User> allUsers = controller.getAllUsers();
        Set<String> alreadySharedUsernames = new HashSet<>();

        // Raccolta degli utenti che già hanno accesso
        for(User u : currentToDo.getUsers()){
            alreadySharedUsernames.add(u.getUsername());
        }
        alreadySharedUsernames.add(currentToDo.getOwner());

        // Creazione della lista degli utenti disponibili
        java.util.List<String> availableUsers = new ArrayList<>();
        for (User user : allUsers) {
            if (!alreadySharedUsernames.contains(user.getUsername())) {
                availableUsers.add(user.getUsername());
            }
        }

        if (availableUsers.isEmpty()) {
            JOptionPane.showMessageDialog(frameToDoForm, "No other users available to share with.", "No Users", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Creazione e configurazione della finestra di selezione
        String[] userChoices = availableUsers.toArray(new String[0]);
        JList<String> userList = new JList<>(userChoices);
        userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setPreferredSize(new Dimension(200, 150));

        int option = JOptionPane.showConfirmDialog(frameToDoForm, scrollPane, "Select Users to Share With", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            java.util.List<String> selectedUsernames = userList.getSelectedValuesList();
            if (selectedUsernames.isEmpty()) {
                JOptionPane.showMessageDialog(frameToDoForm, "No users selected for sharing.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            boolean success = controller.shareToDoWithUsers(currentToDo, selectedUsernames, controller.user.getUsername());
            if (success) {
                JOptionPane.showMessageDialog(frameToDoForm, "ToDo shared successfully with selected users.");
                populateMembersComboBox();
            } else {
                JOptionPane.showMessageDialog(frameToDoForm, "Failed to share ToDo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Gestisce la modifica delle impostazioni di condivisione del ToDo.
     * Consente di rimuovere l'accesso al ToDo da utenti selezionati.
     */
    private void changeSharingSettings() {
        if (currentToDo == null || !controller.isCurrentUserToDoCreator(currentToDo)) {
            JOptionPane.showMessageDialog(frameToDoForm, "Only the creator can manage sharing for this ToDo.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Set<String> sharedUsernames = new HashSet<>();

        // Raccolta degli utenti che hanno accesso (escluso il proprietario)
        for(User u : currentToDo.getUsers()){
            if (!u.getUsername().equals(currentToDo.getOwner())) {
                sharedUsernames.add(u.getUsername());
            }
        }

        if (sharedUsernames.isEmpty()) {
            JOptionPane.showMessageDialog(frameToDoForm, "This ToDo is not currently shared with anyone (besides the owner).", "No Shared Users", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Creazione e configurazione della finestra di selezione
        String[] userChoices = sharedUsernames.toArray(new String[0]);
        JList<String> userList = new JList<>(userChoices);
        userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setPreferredSize(new Dimension(200, 150));

        int option = JOptionPane.showConfirmDialog(frameToDoForm, scrollPane, "Select Users to Remove Sharing From", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            java.util.List<String> selectedUsernames = userList.getSelectedValuesList();
            if (selectedUsernames.isEmpty()) {
                JOptionPane.showMessageDialog(frameToDoForm, "No users selected to remove sharing.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            boolean success = controller.removeToDoSharing(currentToDo, selectedUsernames);
            if (success) {
                JOptionPane.showMessageDialog(frameToDoForm, "Sharing revoked successfully for selected users.");
                populateMembersComboBox();
            } else {
                JOptionPane.showMessageDialog(frameToDoForm, "Failed to revoke sharing.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Popola la ComboBox dei membri con gli utenti che hanno accesso al ToDo.
     * Include il proprietario e tutti gli utenti con cui il ToDo è condiviso.
     */

    private void populateMembersComboBox() {
        membersToDo.removeAllItems();

        if (currentToDo != null) {

            Set<String> addedUsernames = new HashSet<>();

            String ownerUsername = currentToDo.getOwner();
            if (ownerUsername != null && !ownerUsername.isEmpty()) {
                membersToDo.addItem(ownerUsername + " (Owner)");
                addedUsernames.add(ownerUsername);
            }

            if (currentToDo.getUsers() != null) {
                for (User user : currentToDo.getUsers()) {

                    if (!user.getUsername().equals(ownerUsername) && !addedUsernames.contains(user.getUsername())) {
                        membersToDo.addItem(user.getUsername());
                        addedUsernames.add(user.getUsername());
                    }
                }
            }

            if (membersToDo.getItemCount() == 1 && membersToDo.getItemAt(0).equals(ownerUsername + " (Owner)")) {
                membersToDo.addItem("Not shared with other users");
            } else if (membersToDo.getItemCount() == 0) {

                membersToDo.addItem("No members available");
            }

        } else {
            membersToDo.addItem("Not applicable (ToDo not saved)");
            membersToDo.setEnabled(false);
        }
    }


    /**
     * Imposta i colori del pannello in base al colore selezionato.
     * Applica una combinazione di colori predefinita al pannello principale
     * e ai campi di input.
     *
     * @param colorSelected il nome del colore selezionato ("Blue", "Yellow",
     *                     "Red", "Green", "Orange", "Violet")
     */
    private void setPanelColors(String colorSelected) {
        if (colorSelected == null) return;

        switch (colorSelected) {
            case "Blue":
                todoPanel.setBackground(new Color(160, 235, 219));
                campo1.setBackground(new Color(115, 207, 214));
                campo2.setBackground(new Color(115, 207, 214));
                break;
            case "Yellow":
                todoPanel.setBackground(new Color(248, 255, 98));
                campo1.setBackground(new Color(252, 214, 9));
                campo2.setBackground(new Color(252, 214, 9));
                break;
            case "Red":
                todoPanel.setBackground(new Color(255, 87, 84));
                campo1.setBackground(new Color(214, 6, 11));
                campo2.setBackground(new Color(214, 6, 11));
                break;
            case "Green":
                todoPanel.setBackground(new Color(87,255,116));
                campo1.setBackground(new Color(0,201,20));
                campo2.setBackground(new Color(0,201,20));
                break;
            case "Orange":
                todoPanel.setBackground(new Color(255,176,76));
                campo1.setBackground(new Color(255,140,0));
                campo2.setBackground(new Color(255,140,0));
                break;
            case "Violet":
                todoPanel.setBackground(new Color(217,165,255));
                campo1.setBackground(new Color(175,64,255));
                campo2.setBackground(new Color(175,64,255));
                break;
        }
    }

    /**
     * Verifica lo stato di completamento dell'attività controllando
     * lo stato di tutte le checkbox presenti nel pannello delle attività.
     * Aggiorna automaticamente il campo di stato e l'oggetto ToDo corrente.
     *
     * Gli stati possibili sono:
     * - "Not Started": quando non ci sono checkbox
     * - "Complete": quando tutte le checkbox sono selezionate
     * - "Incomplete": quando almeno una checkbox non è selezionata
     */
    private void checkCompletionStatus() {
        boolean allChecked = true;
        int checkBoxCount = 0;

        for (Component comp : panelActivity.getComponents()) {
            if (comp instanceof JCheckBox) {
                checkBoxCount++;
                JCheckBox cb = (JCheckBox) comp;
                if (!cb.isSelected()) {
                    allChecked = false;
                    break;
                }
            }
        }

        if (checkBoxCount == 0) {
            statusField.setText("Not Started");
        } else if (allChecked) {
            statusField.setText("Complete");
        } else {
            statusField.setText("Incomplete");
        }

        if (currentToDo != null) {
            currentToDo.setStatus(statusField.getText());
        }
    }

    /**
     * Carica un'immagine dalla cartella delle risorse e la visualizza
     * nel componente immagine dell'interfaccia utente.
     * L'immagine viene automaticamente ridimensionata per adattarsi
     * alle dimensioni del componente.
     *
     * @param imageName il nome del file immagine da caricare (deve essere
     *                 presente nella cartella /images/ delle risorse)
     *
     * @throws Exception se si verifica un errore durante il caricamento
     *                  dell'immagine (viene gestita internamente)
     */
    private void loadImage(String imageName) {
        try {
            URL imageUrl = getClass().getResource("/images/" + imageName);

            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(imageUrl);
                Image img = icon.getImage();

                Image scaledImg = img.getScaledInstance(image.getWidth() > 0 ? image.getWidth() : 180,
                        image.getHeight() > 0 ? image.getHeight() : 195,
                        Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImg);
                image.setIcon(icon);
                image.setText("");
            } else {
                System.err.println("Error: Image '" + imageName + "' not found in /images/");
                image.setText("Image not found!");
                image.setIcon(null);
            }
        } catch (Exception ex) {
            System.err.println("Error loading image '" + imageName + "': " + ex.getMessage());
            ex.printStackTrace();
            image.setText("Loading error!");
            image.setIcon(null);
        }
    }

}