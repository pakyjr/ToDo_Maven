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

public class BoardForm {
    private JPanel board;
    private JComboBox comboBoxBoards;
    private JButton addToDo;
    private JButton orderToDoByTitle;
    private JButton deleteToDo;
    private JButton todayDueDate;
    public JScrollPane ScrollPanel;
    private JButton MoveUp;
    private JList jList;
    private JTextField textFieldSearchTitle;
    private JDateChooser dateChooserSearchDate;
    private JButton OrderByDueDate;
    private JButton MoveDown;
    private JButton changeBoard;
    private JComboBox<String> colorChange;
    private JPanel campo1;
    public JFrame frameBoardForm;

    public static DefaultListModel<String> listModel;
    private Controller controller;
    private LocalDate highlightDate;

    public BoardForm(JFrame frame, Controller c){
        frameBoardForm = new JFrame("Personal Area");
        frameBoardForm.setContentPane(board);
        frameBoardForm.pack();
        frameBoardForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.controller = c;

        this.comboBoxBoards.addItem("Boards");
        for (BoardName name : BoardName.values()) {
            this.comboBoxBoards.addItem(name.getDisplayName());
        }
        this.comboBoxBoards.setSelectedItem("Boards");

        this.colorChange.addItem("Blue");
        this.colorChange.addItem("Red");
        this.colorChange.addItem("Yellow");
        this.colorChange.addItem("Green");
        this.colorChange.addItem("Orange");
        this.colorChange.addItem("Violet");

        colorChange.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        setPanelColors((String) colorChange.getSelectedItem());

        colorChange.addActionListener(new ActionListener() {
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

        comboBoxBoards.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        listModel = new DefaultListModel<String>();
        jList.setModel(listModel);

        jList.setCellRenderer(new ToDoListCellRenderer(controller, null, this));

        MoveUp.setEnabled(false);
        MoveDown.setEnabled(false);
        deleteToDo.setEnabled(false);
        changeBoard.setEnabled(false);
        addToDo.setEnabled(false);
        orderToDoByTitle.setEnabled(false);
        OrderByDueDate.setEnabled(false);
        todayDueDate.setEnabled(false);


        if (dateChooserSearchDate != null) {
            dateChooserSearchDate.setDateFormatString("EEEE, dd MMMM yyyy");
            dateChooserSearchDate.setPreferredSize(new Dimension(200, 25));
            dateChooserSearchDate.getDateEditor().addPropertyChangeListener(evt -> {
                if ("date".equals(evt.getPropertyName())) {
                    filterByDate();
                }
            });
        }


        jList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean isSelected = !jList.isSelectionEmpty();
                MoveUp.setEnabled(isSelected && jList.getSelectedIndex() > 0);
                MoveDown.setEnabled(isSelected && jList.getSelectedIndex() < listModel.getSize() - 1);
                deleteToDo.setEnabled(isSelected);
                changeBoard.setEnabled(isSelected);
            }
        });


        addToDo.addActionListener(new ActionListener() {
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


        jList.addMouseListener(new MouseAdapter() {
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


        comboBoxBoards.addActionListener(new ActionListener() {
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


        todayDueDate.addActionListener(e -> {
            showTodosToday();
        });


        OrderByDueDate.addActionListener(new ActionListener() {
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


        orderToDoByTitle.addActionListener(new ActionListener() {
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

        MoveUp.addActionListener(new ActionListener() {
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

        MoveDown.addActionListener(new ActionListener() {
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

                    // Changed from ArrayList<ToDo> to List<ToDo>
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

        deleteToDo.addActionListener(new ActionListener() {
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

        changeBoard.addActionListener(new ActionListener() {
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
                            jList.repaint(); // Repaint after move
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

    private void createUIComponents() {
        dateChooserSearchDate = new JDateChooser();

        dateChooserSearchDate.setDateFormatString("EEEE, dd MMMM yyyy");
        dateChooserSearchDate.setPreferredSize(new Dimension(200, 25));
    }

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
                board.setBackground(new Color(87,255,116));
                campo1.setBackground(new Color(0,201,20));
                break;
            case "Orange":
                board.setBackground(new Color(255,176,76));
                campo1.setBackground(new Color(255,140,0));
                break;
            case "Violet":
                board.setBackground(new Color(217,165,255));
                campo1.setBackground(new Color(175,64,255));
                break;
            default:
                board.setBackground(new Color(160, 235, 219));
                campo1.setBackground(new Color(115, 207, 214));
                break;
        }
    }

    private BoardName getBoardNameFromDisplayName(String displayName) {
        for (BoardName name : BoardName.values()) {
            if (name.getDisplayName().equals(displayName)) {
                return name;
            }
        }
        return null;
    }

    private void filterToDoList() {
        String selectedBoardDisplayName = comboBoxBoards.getSelectedItem().toString();
        if ("Boards".equals(selectedBoardDisplayName)) {
            listModel.clear();
            // Do not reset highlightDate or dateChooserSearchDate here
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
            // Apply title filter
            boolean matchesTitle = todo.getTitle().toLowerCase().contains(searchText);

            // Apply date filter if a date is selected
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

        // Always update highlightDate when the date chooser changes
        if (selectedDate == null) {
            highlightDate = null;
        } else {
            highlightDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        // Re-apply both title and date filters
        filterToDoList();
    }

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
        // Do not clear textFieldSearchTitle here, as user might want to search today's todos by title
        jList.repaint();
    }

    public class ToDoListCellRenderer extends DefaultListCellRenderer {
        private Controller controller;
        private String currentBoardDisplayName;
        private BoardForm boardForm;

        public ToDoListCellRenderer(Controller controller, String initialBoardDisplayName, BoardForm boardForm) {
            this.controller = controller;
            this.currentBoardDisplayName = initialBoardDisplayName;
            this.boardForm = boardForm;
        }

        public void setCurrentBoard(String boardDisplayName) {
            this.currentBoardDisplayName = boardDisplayName;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            renderer.setForeground(list.getForeground());
            renderer.setBackground(list.getBackground());

            if (currentBoardDisplayName == null || "Boards".equals(currentBoardDisplayName)) {
                return renderer;
            }

            if (value instanceof String) {
                String toDoTitle = (String) value;

                ToDo toDo = controller.getToDoByTitle(toDoTitle, currentBoardDisplayName);

                if (toDo != null) {

                    if (toDo.getDueDate() != null) {
                        LocalDate today = LocalDate.now();
                        if (toDo.getDueDate().isBefore(today) && !"Completo".equals(toDo.getStatus()) && !"Complete".equals(toDo.getStatus())) {
                            renderer.setForeground(Color.RED);
                        }
                    }

                    if (boardForm.highlightDate != null && toDo.getDueDate() != null && toDo.getDueDate().equals(boardForm.highlightDate)) {

                        renderer.setBackground(new Color(200, 255, 200));
                    }
                }
            }

            if (isSelected) {
                renderer.setBackground(list.getSelectionBackground());
                renderer.setForeground(list.getSelectionForeground());
            }

            return renderer;
        }
    }
}