package gui;

import controller.Controller;
import models.ToDo;
import models.board.Board;
import models.board.BoardName;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.toedter.calendar.JDateChooser;

/**
 * Classe che gestisce l'interfaccia grafica principale dell'applicazione per la gestione dei ToDo.
 * Permette la visualizzazione, creazione, modifica, eliminazione e organizzazione dei ToDo
 * attraverso diverse board personalizzabili con colori e funzionalità di ricerca e filtro.
 */
public class BoardForm {
    /**
     * Pannello principale dell'interfaccia board
     */
    private JPanel board;

    /**
     * ComboBox per la selezione delle board disponibili
     */
    private JComboBox comboBoxBoards;

    /**
     * Pulsante per aggiungere un nuovo ToDo
     */
    private JButton addToDo;

    /**
     * Pulsante per ordinare i ToDo per titolo
     */
    private JButton orderToDoByTitle;

    /**
     * Pulsante per eliminare il ToDo selezionato
     */
    private JButton deleteToDo;

    /**
     * Pulsante per mostrare i ToDo con scadenza oggi
     */
    private JButton todayDueDate;

    /**
     * Pannello scrollabile contenente la lista dei ToDo
     */
    public JScrollPane ScrollPanel;

    /**
     * Pulsante per spostare un ToDo verso l'alto nella lista
     */
    private JButton MoveUp;

    /**
     * Lista grafica dei ToDo
     */
    private JList jList;

    /**
     * Campo di testo per la ricerca per titolo
     */
    private JTextField textFieldSearchTitle;

    /**
     * Date chooser per la ricerca per data di scadenza
     */
    private JDateChooser dateChooserSearchDate;

    /**
     * Pulsante per ordinare i ToDo per data di scadenza
     */
    private JButton OrderByDueDate;

    /**
     * Pulsante per spostare un ToDo verso il basso nella lista
     */
    private JButton MoveDown;

    /**
     * Pulsante per spostare un ToDo in un'altra board
     */
    private JButton changeBoard;

    /**
     * ComboBox per la selezione del colore della board
     */
    private JComboBox<String> colorChange;

    /**
     * Pannello secondario per la personalizzazione dei colori
     */
    private JPanel campo1;

    /**
     * Frame principale dell'interfaccia board
     */
    public JFrame frameBoardForm;

    /**
     * Modello della lista per la gestione dinamica dei ToDo visualizzati
     */
    public static DefaultListModel<String> listModel;

    /**
     * Controller per la gestione della logica di business
     */
    private Controller controller;

    /**
     * Data evidenziata per il filtraggio e la visualizzazione speciale
     */
    private LocalDate highlightDate;

    /**
     * Costruttore della classe BoardForm.
     * Inizializza l'interfaccia grafica completa, configura tutti i componenti,
     * imposta i listener per gli eventi e prepara la board per l'uso.
     *
     * @param frame Il frame genitore (generalmente dalla form di login/registrazione)
     * @param c     Il controller che gestisce la logica dell'applicazione
     */
    public BoardForm(JFrame frame, Controller c) {
        frameBoardForm = new JFrame("Personal Area");
        frameBoardForm.setContentPane(board);
        frameBoardForm.pack();
        frameBoardForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.controller = c;

        // Inizializza la combobox delle board
        this.comboBoxBoards.addItem("Boards");
        for (BoardName name : BoardName.values()) {
            this.comboBoxBoards.addItem(name.getDisplayName());
        }
        this.comboBoxBoards.setSelectedItem("Boards");

        // Inizializza la combobox dei colori
        this.colorChange.addItem("Blue");
        this.colorChange.addItem("Red");
        this.colorChange.addItem("Yellow");
        this.colorChange.addItem("Green");
        this.colorChange.addItem("Orange");
        this.colorChange.addItem("Violet");

        // Configura il renderer per centrare il testo nella combobox colori
        colorChange.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        setPanelColors((String) colorChange.getSelectedItem());

        // Listener per il cambio colore della board
        colorChange.addActionListener(new ActionListener() {
            /**
             * Gestisce il cambio di colore della board selezionata.
             * Aggiorna sia l'aspetto visuale che i dati persistenti della board.
             *
             * @param e L'evento di selezione del colore
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                String colorSelected = (String) colorChange.getSelectedItem();
                setPanelColors(colorSelected);

                String selectedBoardDisplayName = (String) comboBoxBoards.getSelectedItem();
                if (!"Boards".equals(selectedBoardDisplayName)) {
                    BoardName selectedBoardEnum = getBoardNameFromDisplayName(selectedBoardDisplayName);
                    if (selectedBoardEnum != null) {
                        Board currentBoard = controller.user.getBoard(selectedBoardEnum);
                        if (currentBoard != null) {
                            currentBoard.setColor(colorSelected);
                            controller.updateBoard(currentBoard);
                            System.out.println("DEBUG: Board color changed to " + colorSelected + " for board " + selectedBoardDisplayName);
                        }
                    }
                }
            }
        });

        // Configura il renderer per centrare il testo nella combobox board
        comboBoxBoards.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        // Inizializza il modello e il renderer della lista ToDo
        listModel = new DefaultListModel<String>();
        jList.setModel(listModel);
        jList.setCellRenderer(new ToDoListCellRenderer(controller, null, this));

        // Disabilita inizialmente tutti i pulsanti che richiedono selezioni
        MoveUp.setEnabled(false);
        MoveDown.setEnabled(false);
        deleteToDo.setEnabled(false);
        changeBoard.setEnabled(false);
        addToDo.setEnabled(false);
        orderToDoByTitle.setEnabled(false);
        OrderByDueDate.setEnabled(false);
        todayDueDate.setEnabled(false);

        // Configura il date chooser per la ricerca
        if (dateChooserSearchDate != null) {
            dateChooserSearchDate.setDateFormatString("EEEE, dd MMMM yyyy");
            dateChooserSearchDate.setPreferredSize(new Dimension(200, textFieldSearchTitle.getPreferredSize().height));
            dateChooserSearchDate.getDateEditor().addPropertyChangeListener(evt -> {
                if ("date".equals(evt.getPropertyName())) {
                    filterByDate();
                }
            });
        }

        // Listener per la selezione degli elementi nella lista
        jList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean isSelected = !jList.isSelectionEmpty();
                MoveUp.setEnabled(isSelected && jList.getSelectedIndex() > 0);
                MoveDown.setEnabled(isSelected && jList.getSelectedIndex() < listModel.getSize() - 1);
                deleteToDo.setEnabled(isSelected);
                changeBoard.setEnabled(isSelected);
            }
        });

        // Listener per l'aggiunta di un nuovo ToDo
        addToDo.addActionListener(new ActionListener() {
            /**
             * Gestisce l'apertura del form per aggiungere un nuovo ToDo.
             * Verifica che sia selezionata una board valida prima di procedere.
             *
             * @param e L'evento del pulsante
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentBoardDisplayName = comboBoxBoards.getSelectedItem().toString();
                if ("Boards".equals(currentBoardDisplayName)) {
                    JOptionPane.showMessageDialog(frameBoardForm, "Please select a valid board before adding a ToDo.", "No Board Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                ToDoForm toDoForm = new ToDoForm(frameBoardForm, controller, currentBoardDisplayName, null);
                frameBoardForm.setVisible(false);
                toDoForm.frameToDoForm.setVisible(true);
            }
        });

        // Listener per il doppio click sui ToDo (modifica)
        jList.addMouseListener(new MouseAdapter() {
            /**
             * Gestisce il doppio click su un ToDo per aprirne il form di modifica.
             *
             * @param e L'evento del mouse
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = jList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        String selectedToDoTitle = (String) listModel.getElementAt(index);
                        String currentBoardDisplayName = comboBoxBoards.getSelectedItem().toString();

                        if ("Boards".equals(currentBoardDisplayName)) {
                            JOptionPane.showMessageDialog(frameBoardForm, "Please select a valid board to view or edit ToDos.", "Invalid Board Selected", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        ToDo selectedToDo = controller.getToDoByTitle(selectedToDoTitle, currentBoardDisplayName);

                        if (selectedToDo != null) {
                            ToDoForm toDoForm = new ToDoForm(frameBoardForm, controller, currentBoardDisplayName, selectedToDo);
                            frameBoardForm.setVisible(false);
                            toDoForm.frameToDoForm.setVisible(true);
                            jList.clearSelection();
                        }
                    }
                }
            }
        });

        // Listener per il cambio di board selezionata
        comboBoxBoards.addActionListener(new ActionListener() {
            /**
             * Gestisce il cambio di board selezionata.
             * Aggiorna la lista dei ToDo, i colori dell'interfaccia e lo stato dei pulsanti.
             *
             * @param e L'evento di selezione della board
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedBoardDisplayName = comboBoxBoards.getSelectedItem().toString();
                listModel.clear();
                jList.clearSelection();

                boolean boardSelected = !"Boards".equals(selectedBoardDisplayName);
                addToDo.setEnabled(boardSelected);
                orderToDoByTitle.setEnabled(boardSelected);
                OrderByDueDate.setEnabled(boardSelected);
                todayDueDate.setEnabled(boardSelected);
                MoveUp.setEnabled(false);
                MoveDown.setEnabled(false);
                deleteToDo.setEnabled(false);
                changeBoard.setEnabled(false);
                if (dateChooserSearchDate != null) {
                    dateChooserSearchDate.setEnabled(boardSelected);
                }

                if (boardSelected) {
                    BoardName selectedBoardEnum = getBoardNameFromDisplayName(selectedBoardDisplayName);
                    if (selectedBoardEnum != null) {
                        Board selectedBoard = controller.user.getBoard(selectedBoardEnum);
                        if (selectedBoard != null) {
                            setPanelColors(selectedBoard.getColor());
                            colorChange.setSelectedItem(selectedBoard.getColor());
                        }
                    }

                    ArrayList<String> todos = controller.getToDoListString(selectedBoardDisplayName);
                    listModel.addAll(todos);
                    ((ToDoListCellRenderer) jList.getCellRenderer()).setCurrentBoard(selectedBoardDisplayName);
                } else {
                    ((ToDoListCellRenderer) jList.getCellRenderer()).setCurrentBoard(null);
                    setPanelColors("Blue");
                    colorChange.setSelectedItem("Blue");
                }
                textFieldSearchTitle.setText("");
                if (dateChooserSearchDate != null) {
                    dateChooserSearchDate.setDate(null);
                }

                highlightDate = null;
                jList.repaint();
            }
        });

        // Listener per la ricerca testuale in tempo reale
        textFieldSearchTitle.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterToDoList();
            }

            public void removeUpdate(DocumentEvent e) {
                filterToDoList();
            }

            public void changedUpdate(DocumentEvent e) {
                filterToDoList();
            }
        });

        // Listener per mostrare i ToDo di oggi
        todayDueDate.addActionListener(e -> {
            showTodosToday();
        });

        // Listener per ordinare per data di scadenza
        OrderByDueDate.addActionListener(new ActionListener() {
            /**
             * Ordina i ToDo della board corrente per data di scadenza.
             * Le date nulle vengono posizionate alla fine.
             *
             * @param e L'evento del pulsante
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedBoardDisplayName = comboBoxBoards.getSelectedItem().toString();
                if ("Boards".equals(selectedBoardDisplayName)) return;

                BoardName selectedBoardEnum = getBoardNameFromDisplayName(selectedBoardDisplayName);
                if (selectedBoardEnum == null) return;

                List<ToDo> todos = controller.user.getBoard(selectedBoardEnum).getTodoList();
                todos.sort(Comparator.comparing(ToDo::getDueDate, Comparator.nullsLast(LocalDate::compareTo)));

                listModel.clear();
                for (ToDo todo : todos) {
                    listModel.addElement(todo.getTitle());
                }
                jList.clearSelection();
                MoveUp.setEnabled(false);
                MoveDown.setEnabled(false);
                deleteToDo.setEnabled(false);
                changeBoard.setEnabled(false);
                highlightDate = null;
                jList.repaint();
            }
        });

        // Listener per ordinare per titolo
        orderToDoByTitle.addActionListener(new ActionListener() {
            /**
             * Ordina i ToDo della board corrente alfabeticamente per titolo.
             * L'ordinamento è case-insensitive.
             *
             * @param e L'evento del pulsante
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedBoardDisplayName = comboBoxBoards.getSelectedItem().toString();
                if ("Boards".equals(selectedBoardDisplayName)) return;

                BoardName selectedBoardEnum = getBoardNameFromDisplayName(selectedBoardDisplayName);
                if (selectedBoardEnum == null) return;

                List<ToDo> todos = controller.user.getBoard(selectedBoardEnum).getTodoList();
                todos.sort(Comparator.comparing(todo -> todo.getTitle().toLowerCase()));

                listModel.clear();
                for (ToDo todo : todos) {
                    listModel.addElement(todo.getTitle());
                }
                jList.clearSelection();
                MoveUp.setEnabled(false);
                MoveDown.setEnabled(false);
                deleteToDo.setEnabled(false);
                changeBoard.setEnabled(false);
                highlightDate = null;
                jList.repaint();
            }
        });

        // Listener per spostare un ToDo verso l'alto
        MoveUp.addActionListener(new ActionListener() {
            /**
             * Sposta il ToDo selezionato di una posizione verso l'alto nella lista.
             * Aggiorna sia la visualizzazione che l'ordine effettivo nella board.
             *
             * @param e L'evento del pulsante
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = jList.getSelectedIndex();
                String selectedBoardDisplayName = comboBoxBoards.getSelectedItem().toString();
                if (selectedIndex > 0 && !"Boards".equals(selectedBoardDisplayName)) {
                    String selectedTitle = listModel.getElementAt(selectedIndex);

                    BoardName currentBoardEnum = getBoardNameFromDisplayName(selectedBoardDisplayName);
                    if (currentBoardEnum == null) return;

                    String elementToMove = listModel.remove(selectedIndex);
                    listModel.add(selectedIndex - 1, elementToMove);

                    List<ToDo> todos = controller.user.getBoard(currentBoardEnum).getTodoList();
                    ToDo todoToMove = null;
                    int actualIndex = -1;
                    for (int i = 0; i < todos.size(); i++) {
                        if (todos.get(i).getTitle().equals(selectedTitle)) {
                            todoToMove = todos.get(i);
                            actualIndex = i;
                            break;
                        }
                    }

                    if (todoToMove != null && actualIndex != -1 && actualIndex > 0) {
                        Collections.swap(todos, actualIndex, actualIndex - 1);
                    }

                    jList.setSelectedIndex(selectedIndex - 1);
                    jList.ensureIndexIsVisible(selectedIndex - 1);
                    jList.repaint();
                }
            }
        });

        // Listener per spostare un ToDo verso il basso
        MoveDown.addActionListener(new ActionListener() {
            /**
             * Sposta il ToDo selezionato di una posizione verso il basso nella lista.
             * Aggiorna sia la visualizzazione che l'ordine effettivo nella board.
             *
             * @param e L'evento del pulsante
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = jList.getSelectedIndex();
                String selectedBoardDisplayName = comboBoxBoards.getSelectedItem().toString();
                if (selectedIndex < listModel.getSize() - 1 && selectedIndex != -1 && !"Boards".equals(selectedBoardDisplayName)) {
                    String selectedTitle = listModel.getElementAt(selectedIndex);

                    BoardName currentBoardEnum = getBoardNameFromDisplayName(selectedBoardDisplayName);
                    if (currentBoardEnum == null) return;

                    String elementToMove = listModel.remove(selectedIndex);
                    listModel.add(selectedIndex + 1, elementToMove);

                    List<ToDo> todos = controller.user.getBoard(currentBoardEnum).getTodoList();

                    ToDo todoToMove = null;
                    int actualIndex = -1;
                    for (int i = 0; i < todos.size(); i++) {
                        if (todos.get(i).getTitle().equals(selectedTitle)) {
                            todoToMove = todos.get(i);
                            actualIndex = i;
                            break;
                        }
                    }

                    if (todoToMove != null && actualIndex != -1 && actualIndex < todos.size() - 1) {
                        Collections.swap(todos, actualIndex, actualIndex + 1);
                    }

                    jList.setSelectedIndex(selectedIndex + 1);
                    jList.ensureIndexIsVisible(selectedIndex + 1);
                    jList.repaint();
                }
            }
        });

        // Listener per eliminare un ToDo
        deleteToDo.addActionListener(new ActionListener() {
            /**
             * Elimina il ToDo selezionato dopo conferma dell'utente.
             * Aggiorna la visualizzazione e rimuove l'elemento dal database.
             *
             * @param e L'evento del pulsante
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = jList.getSelectedIndex();
                String currentBoardDisplayName = comboBoxBoards.getSelectedItem().toString();
                if (selectedIndex != -1 && !"Boards".equals(currentBoardDisplayName)) {
                    String selectedToDoTitle = listModel.getElementAt(selectedIndex);

                    int confirmResult = JOptionPane.showConfirmDialog(frameBoardForm,
                            "Are you sure you want to delete '" + selectedToDoTitle + "'?",
                            "Confirm Deletion",
                            JOptionPane.YES_NO_OPTION);

                    if (confirmResult == JOptionPane.YES_OPTION) {
                        controller.deleteToDo(currentBoardDisplayName, selectedToDoTitle);
                        listModel.remove(selectedIndex);

                        jList.clearSelection();
                        MoveUp.setEnabled(false);
                        MoveDown.setEnabled(false);
                        deleteToDo.setEnabled(false);
                        changeBoard.setEnabled(false);
                        jList.repaint();
                        JOptionPane.showMessageDialog(frameBoardForm, "'" + selectedToDoTitle + "' deleted successfully.");
                    }
                } else {
                    JOptionPane.showMessageDialog(frameBoardForm, "Please select a ToDo to delete and ensure a board is selected.", "No ToDo Selected", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Listener per spostare un ToDo in un'altra board
        changeBoard.addActionListener(new ActionListener() {
            /**
             * Permette di spostare il ToDo selezionato in un'altra board.
             * Presenta una dialog per la selezione della board di destinazione.
             *
             * @param e L'evento del pulsante
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = jList.getSelectedIndex();
                String currentBoardDisplayName = comboBoxBoards.getSelectedItem().toString();
                if (selectedIndex != -1 && !"Boards".equals(currentBoardDisplayName)) {
                    String selectedToDoTitle = listModel.getElementAt(selectedIndex);

                    ArrayList<String> availableBoardDisplayNames = new ArrayList<>();
                    for (BoardName name : BoardName.values()) {
                        if (!name.getDisplayName().equals(currentBoardDisplayName)) {
                            availableBoardDisplayNames.add(name.getDisplayName());
                        }
                    }

                    if (availableBoardDisplayNames.isEmpty()) {
                        JOptionPane.showMessageDialog(frameBoardForm, "No other boards available to move this ToDo to.", "No Destination Boards", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    String[] boardOptions = availableBoardDisplayNames.toArray(new String[0]);

                    String destinationBoardString = (String) JOptionPane.showInputDialog(
                            frameBoardForm,
                            "Select the destination board for '" + selectedToDoTitle + "':",
                            "Move ToDo",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            boardOptions,
                            boardOptions[0]);

                    if (destinationBoardString != null) {
                        boolean moved = controller.moveToDo(selectedToDoTitle, currentBoardDisplayName, destinationBoardString);

                        if (moved) {
                            listModel.remove(selectedIndex);
                            jList.clearSelection();
                            MoveUp.setEnabled(false);
                            MoveDown.setEnabled(false);
                            deleteToDo.setEnabled(false);
                            changeBoard.setEnabled(false);
                            jList.repaint();
                            JOptionPane.showMessageDialog(frameBoardForm, "'" + selectedToDoTitle + "' moved successfully to " + destinationBoardString + " board.");
                        } else {
                            JOptionPane.showMessageDialog(frameBoardForm, "Failed to move '" + selectedToDoTitle + "'.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frameBoardForm, "Please select a ToDo to move and ensure a board is selected.", "No ToDo Selected", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }

    /**
     * Metodo per la creazione personalizzata dei componenti UI.
     * Inizializza il JDateChooser con le impostazioni appropriate.
     */
    private void createUIComponents() {
        dateChooserSearchDate = new JDateChooser();
        dateChooserSearchDate.setDateFormatString("EEEE, dd MMMM yyyy");
        dateChooserSearchDate.setPreferredSize(new Dimension(200, 25));
    }

    /**
     * Imposta i colori dei pannelli dell'interfaccia in base al colore selezionato.
     *
     * @param colorSelected Il nome del colore da applicare ("Blue", "Red", "Yellow", etc.)
     */
    private void setPanelColors(String colorSelected) {
        if (colorSelected == null) {
            colorSelected = "Blue";
        }

        switch (colorSelected) {
            case "Blue":
                board.setBackground(new Color(160, 235, 219));
                campo1.setBackground(new Color(115, 207, 214));
                break;
            case "Yellow":
                board.setBackground(new Color(248, 255, 98));
                campo1.setBackground(new Color(252, 214, 9));
                break;
            case "Red":
                board.setBackground(new Color(255, 87, 84));
                campo1.setBackground(new Color(214, 6, 11));
                break;
            case "Green":
                board.setBackground(new Color(87, 255, 116));
                campo1.setBackground(new Color(0, 201, 20));
                break;
            case "Orange":
                board.setBackground(new Color(255, 176, 76));
                campo1.setBackground(new Color(255, 140, 0));
                break;
            case "Violet":
                board.setBackground(new Color(217, 165, 255));
                campo1.setBackground(new Color(175, 64, 255));
                break;
            default:
                board.setBackground(new Color(160, 235, 219));
                campo1.setBackground(new Color(115, 207, 214));
                break;
        }
    }

    /**
     * Converte il nome visualizzato di una board nel corrispondente enum BoardName.
     *
     * @param displayName Il nome visualizzato della board
     * @return L'enum BoardName corrispondente, o null se non trovato
     */
    private BoardName getBoardNameFromDisplayName(String displayName) {
        for (BoardName name : BoardName.values()) {
            if (name.getDisplayName().equals(displayName)) {
                return name;
            }
        }
        return null;
    }

    /**
     * Filtra la lista dei ToDo in base al testo di ricerca e alla data selezionata.
     * Applica contemporaneamente filtri per titolo e data di scadenza.
     */
    private void filterToDoList() {
        String selectedBoardDisplayName = comboBoxBoards.getSelectedItem().toString();
        if ("Boards".equals(selectedBoardDisplayName)) {
            listModel.clear();
            jList.repaint();
            return;
        }

        String searchText = textFieldSearchTitle.getText().toLowerCase();
        BoardName boardNameEnum = getBoardNameFromDisplayName(selectedBoardDisplayName);
        if (boardNameEnum == null) {
            listModel.clear();
            jList.repaint();
            return;
        }

        List<ToDo> todosInCurrentBoard = controller.user.getBoard(boardNameEnum).getTodoList();
        listModel.clear();

        for (ToDo todo : todosInCurrentBoard) {
            // Filtro per titolo
            boolean matchesTitle = todo.getTitle().toLowerCase().contains(searchText);

            // Filtro per data
            boolean matchesDate = true;
            if (highlightDate != null) {
                matchesDate = (todo.getDueDate() != null && todo.getDueDate().equals(highlightDate));
            } else if (dateChooserSearchDate.getDate() != null) {
                LocalDate selectedDateFromChooser = dateChooserSearchDate.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                matchesDate = (todo.getDueDate() != null && todo.getDueDate().equals(selectedDateFromChooser));
            }

            if (matchesTitle && matchesDate) {
                listModel.addElement(todo.getTitle());
            }
        }

        jList.clearSelection();
        MoveUp.setEnabled(false);
        MoveDown.setEnabled(false);
        deleteToDo.setEnabled(false);
        changeBoard.setEnabled(false);
        jList.repaint();
    }

    /**
     * Filtra i ToDo in base alla data selezionata nel date chooser.
     * Aggiorna la data evidenziata e riapplica tutti i filtri.
     */
    private void filterByDate() {
        String selectedBoardDisplayName = comboBoxBoards.getSelectedItem().toString();
        if ("Boards".equals(selectedBoardDisplayName)) {
            highlightDate = null;
            listModel.clear();
            jList.repaint();
            return;
        }
        BoardName boardNameEnum = getBoardNameFromDisplayName(selectedBoardDisplayName);
        if (boardNameEnum == null) {
            highlightDate = null;
            listModel.clear();
            jList.repaint();
            return;
        }

        Date selectedDate = null;
        if (dateChooserSearchDate != null) {
            selectedDate = dateChooserSearchDate.getDate();
        }

        // Aggiorna sempre highlightDate quando cambia il date chooser
        if (selectedDate == null) {
            highlightDate = null;
        } else {
            highlightDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        // Riapplica entrambi i filtri (titolo e data)
        filterToDoList();
    }

    /**
     * Mostra i ToDo con scadenza oggi nella board corrente.
     * Imposta il filtro data su oggi e aggiorna la visualizzazione.
     */
    private void showTodosToday() {
        String selectedBoardDisplayName = comboBoxBoards.getSelectedItem().toString();
        if ("Boards".equals(selectedBoardDisplayName)) return;

        LocalDate today = LocalDate.now();
        BoardName boardEnum = getBoardNameFromDisplayName(selectedBoardDisplayName);
        if (boardEnum == null) return;

        highlightDate = today;
        if (dateChooserSearchDate != null) {
            dateChooserSearchDate.setDate(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        // Re-apply both title and date filters, with highlightDate set to today
        filterToDoList();

        jList.clearSelection();
        MoveUp.setEnabled(false);
        MoveDown.setEnabled(false);
        deleteToDo.setEnabled(false);
        changeBoard.setEnabled(false);

        jList.repaint();
    }

    /**
     * Custom renderer per celle di lista che visualizza elementi ToDo con colorazione condizionale.
     * Estende DefaultListCellRenderer per fornire rendering personalizzato basato sullo stato
     * del ToDo, date di scadenza e selezioni dell'utente.
     */
    public class ToDoListCellRenderer extends DefaultListCellRenderer {
        /**
         * Controller per accedere ai dati dei ToDo
         */
        private Controller controller;

        /**
         * Nome del board attualmente visualizzato
         */
        private String currentBoardDisplayName;

        /**
         * Riferimento al form del board per accedere alle impostazioni di evidenziazione
         */
        private BoardForm boardForm;

        /**
         * Costruttore per inizializzare il renderer con le dipendenze necessarie.
         *
         * @param controller              il controller per accedere ai dati dei ToDo
         * @param initialBoardDisplayName il nome iniziale del board da visualizzare
         * @param boardForm               il form del board contenente le impostazioni di evidenziazione
         */
        public ToDoListCellRenderer(Controller controller, String initialBoardDisplayName, BoardForm boardForm) {
            this.controller = controller;
            this.currentBoardDisplayName = initialBoardDisplayName;
            this.boardForm = boardForm;
        }

        /**
         * Aggiorna il board correntemente visualizzato.
         *
         * @param boardDisplayName il nuovo nome del board da visualizzare
         */
        public void setCurrentBoard(String boardDisplayName) {
            this.currentBoardDisplayName = boardDisplayName;
        }

        /**
         * Renderizza una cella della lista con colorazione condizionale basata sullo stato del ToDo.
         * <p>
         * Logica di colorazione:
         * - Rosso: ToDo scaduti (data di scadenza passata e stato non "Completo"/"Complete")
         * - Verde chiaro: ToDo con data di scadenza che corrisponde alla data evidenziata
         * - Colori di selezione: Quando la cella è selezionata
         *
         * @param list         la JList che contiene l'elemento
         * @param value        l'oggetto da renderizzare (dovrebbe essere una String con il titolo del ToDo)
         * @param index        l'indice dell'elemento nella lista
         * @param isSelected   true se l'elemento è selezionato
         * @param cellHasFocus true se la cella ha il focus
         * @return il Component da utilizzare per il rendering della cella
         */
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // Ottiene il renderer di base
            JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            // Imposta colori di default
            renderer.setForeground(list.getForeground());
            renderer.setBackground(list.getBackground());

            // Non applica colorazione speciale se non c'è un board selezionato o è il board principale
            if (currentBoardDisplayName == null || "Boards".equals(currentBoardDisplayName)) {
                return renderer;
            }

            // Processa solo se il valore è una stringa (titolo del ToDo)
            if (value instanceof String) {
                String toDoTitle = (String) value;

                // Recupera l'oggetto ToDo completo tramite il controller
                ToDo toDo = controller.getToDoByTitle(toDoTitle, currentBoardDisplayName);

                if (toDo != null) {
                    // Evidenzia in rosso i ToDo scaduti e non completati
                    if (toDo.getDueDate() != null) {
                        LocalDate today = LocalDate.now();
                        if (toDo.getDueDate().isBefore(today) && !"Completo".equals(toDo.getStatus()) && !"Complete".equals(toDo.getStatus())) {
                            renderer.setForeground(Color.RED);
                        }
                    }

                    // Evidenzia con sfondo verde chiaro i ToDo con data corrispondente alla data evidenziata
                    if (boardForm.highlightDate != null && toDo.getDueDate() != null && toDo.getDueDate().equals(boardForm.highlightDate)) {
                        renderer.setBackground(new Color(200, 255, 200)); // Verde chiaro
                    }
                }
            }

            // Applica i colori di selezione se l'elemento è selezionato (sovrascrive altre colorazioni)
            if (isSelected) {
                renderer.setBackground(list.getSelectionBackground());
                renderer.setForeground(list.getSelectionForeground());
            }

            return renderer;
        }
    }
}