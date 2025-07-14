package gui;

import controller.Controller;
import models.ToDo;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

public class BoardForm {
    private JPanel board;
    private JComboBox comboBoxBoards;
    private JButton shareBoardButton;
    private JButton addToDoButton;
    private JButton orderToDoByTitleButton;
    private JButton deleteToDoButton;
    private JButton dueDateButton;
    public JScrollPane ScrollPanel;
    private JButton MoveUp;
    private JList jList;
    private JTextField textFieldSearchTitle;
    private JTextField textFieldSearchDate;
    private JButton buttonOrderByDate;
    private JButton MoveDown;
    private JButton changeBoard;
    public JFrame frameBoardForm;

    public static DefaultListModel<String> listModel;
    private Controller controller;


    public BoardForm(JFrame frame, Controller c){
        frameBoardForm = new JFrame("Personal Area");
        frameBoardForm.setContentPane(board);
        frameBoardForm.pack();
        frameBoardForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.controller = c;

        // Populate the combo box with board names
        for (BoardName name : BoardName.values()) {
            this.comboBoxBoards.addItem(name.toString());
        }

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

        jList.setCellRenderer(new ToDoListCellRenderer(controller, (String) comboBoxBoards.getSelectedItem()));

        MoveUp.setEnabled(false);
        MoveDown.setEnabled(false);
        deleteToDoButton.setEnabled(false); // Initially disabled
        changeBoard.setEnabled(false); // Initially disabled

        String initialBoard = comboBoxBoards.getSelectedItem().toString();
        listModel.addAll(controller.getToDoListString(BoardName.valueOf(initialBoard)));


        jList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean isSelected = !jList.isSelectionEmpty();
                MoveUp.setEnabled(isSelected && jList.getSelectedIndex() > 0);
                MoveDown.setEnabled(isSelected && jList.getSelectedIndex() < listModel.getSize() - 1);
                deleteToDoButton.setEnabled(isSelected);
                changeBoard.setEnabled(isSelected);
            }
        });


        addToDoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentBoard = comboBoxBoards.getSelectedItem().toString();

                ToDoForm toDoForm = new ToDoForm(frameBoardForm, controller, currentBoard, null);
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
                        ToDo selectedToDo = controller.getToDoByTitle(selectedToDoTitle, BoardName.valueOf(comboBoxBoards.getSelectedItem().toString()));

                        if (selectedToDo != null) {

                            ToDoForm toDoForm = new ToDoForm(frameBoardForm, controller, comboBoxBoards.getSelectedItem().toString(), selectedToDo);
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
                String selectedBoard = comboBoxBoards.getSelectedItem().toString();
                ArrayList<String> todos = controller.getToDoListString(BoardName.valueOf(selectedBoard));

                listModel.clear();
                listModel.addAll(todos);

                jList.clearSelection();
                MoveUp.setEnabled(false); // Disable after board change
                MoveDown.setEnabled(false); // Disable after board change
                deleteToDoButton.setEnabled(false); // Disable after board change
                changeBoard.setEnabled(false); // Disable after board change

                ((ToDoListCellRenderer) jList.getCellRenderer()).setCurrentBoard(selectedBoard);
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


        textFieldSearchDate.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterByDate();
            }
            public void removeUpdate(DocumentEvent e) {
                filterByDate();
            }
            public void changedUpdate(DocumentEvent e) {
                filterByDate();
            }
        });


        dueDateButton.addActionListener(e -> {
            showTodosToday();
        });


        buttonOrderByDate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedBoard = comboBoxBoards.getSelectedItem().toString();

                ArrayList<ToDo> todos = controller.user.getBoard(BoardName.valueOf(selectedBoard)).getTodoList();

                todos.sort(Comparator.comparing(ToDo::getDueDate, Comparator.nullsLast(LocalDate::compareTo)));

                listModel.clear();
                for (ToDo todo : todos) {
                    listModel.addElement(todo.getTitle());
                }
                jList.clearSelection();
                MoveUp.setEnabled(false);
                MoveDown.setEnabled(false);
                deleteToDoButton.setEnabled(false);
                changeBoard.setEnabled(false);
            }
        });


        orderToDoByTitleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedBoard = comboBoxBoards.getSelectedItem().toString();

                ArrayList<ToDo> todos = controller.user.getBoard(BoardName.valueOf(selectedBoard)).getTodoList();

                todos.sort(Comparator.comparing(todo -> todo.getTitle().toLowerCase()));

                listModel.clear();
                for (ToDo todo : todos) {
                    listModel.addElement(todo.getTitle());
                }
                jList.clearSelection();
                MoveUp.setEnabled(false);
                MoveDown.setEnabled(false);
                deleteToDoButton.setEnabled(false);
                changeBoard.setEnabled(false);
            }
        });

        MoveUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = jList.getSelectedIndex();
                if (selectedIndex > 0) {
                    String selectedTitle = listModel.getElementAt(selectedIndex);
                    String boardName = comboBoxBoards.getSelectedItem().toString();
                    BoardName currentBoard = BoardName.valueOf(boardName);

                    String elementToMove = listModel.remove(selectedIndex);
                    listModel.add(selectedIndex - 1, elementToMove);

                    ArrayList<ToDo> todos = controller.user.getBoard(currentBoard).getTodoList();
                    // Find the ToDo object by title, assuming titles are unique within a board for simplicity
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
                }
            }
        });

        MoveDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = jList.getSelectedIndex();
                if (selectedIndex < listModel.getSize() - 1 && selectedIndex != -1) {
                    String selectedTitle = listModel.getElementAt(selectedIndex);
                    String boardName = comboBoxBoards.getSelectedItem().toString();
                    BoardName currentBoard = BoardName.valueOf(boardName);

                    String elementToMove = listModel.remove(selectedIndex);
                    listModel.add(selectedIndex + 1, elementToMove);

                    ArrayList<ToDo> todos = controller.user.getBoard(currentBoard).getTodoList();

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
                }
            }
        });

        deleteToDoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = jList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedToDoTitle = listModel.getElementAt(selectedIndex);
                    String currentBoardName = comboBoxBoards.getSelectedItem().toString();
                    BoardName boardNameEnum = BoardName.valueOf(currentBoardName);

                    int confirmResult = JOptionPane.showConfirmDialog(frameBoardForm,
                            "Are you sure you want to delete '" + selectedToDoTitle + "'?",
                            "Confirm Deletion",
                            JOptionPane.YES_NO_OPTION);

                    if (confirmResult == JOptionPane.YES_OPTION) {
                        controller.deleteToDo(boardNameEnum, selectedToDoTitle);

                        listModel.remove(selectedIndex);

                        jList.clearSelection();
                        MoveUp.setEnabled(false);
                        MoveDown.setEnabled(false);
                        deleteToDoButton.setEnabled(false);
                        changeBoard.setEnabled(false);

                        JOptionPane.showMessageDialog(frameBoardForm, "'" + selectedToDoTitle + "' deleted successfully.");
                    }
                } else {
                    JOptionPane.showMessageDialog(frameBoardForm, "Please select a ToDo to delete.", "No ToDo Selected", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        changeBoard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = jList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedToDoTitle = listModel.getElementAt(selectedIndex);
                    String currentBoardName = comboBoxBoards.getSelectedItem().toString();
                    BoardName sourceBoard = BoardName.valueOf(currentBoardName);

                    ArrayList<BoardName> availableBoards = new ArrayList<>();
                    for (BoardName name : BoardName.values()) {
                        if (!name.equals(sourceBoard)) {
                            availableBoards.add(name);
                        }
                    }

                    if (availableBoards.isEmpty()) {
                        JOptionPane.showMessageDialog(frameBoardForm, "No other boards available to move this ToDo to.", "No Destination Boards", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    String[] boardOptions = new String[availableBoards.size()];
                    for (int i = 0; i < availableBoards.size(); i++) {
                        boardOptions[i] = availableBoards.get(i).toString();
                    }

                    String destinationBoardString = (String) JOptionPane.showInputDialog(
                            frameBoardForm,
                            "Select the destination board for '" + selectedToDoTitle + "':",
                            "Move ToDo",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            boardOptions,
                            boardOptions[0]);

                    if (destinationBoardString != null) { // User made a selection
                        BoardName destinationBoard = BoardName.valueOf(destinationBoardString);

                        boolean moved = controller.moveToDo(selectedToDoTitle, sourceBoard, destinationBoard);

                        if (moved) {
                            listModel.remove(selectedIndex); // Remove from the current board's list
                            jList.clearSelection();
                            MoveUp.setEnabled(false);
                            MoveDown.setEnabled(false);
                            deleteToDoButton.setEnabled(false);
                            changeBoard.setEnabled(false);
                            JOptionPane.showMessageDialog(frameBoardForm, "'" + selectedToDoTitle + "' moved successfully to " + destinationBoardString + " board.");
                        } else {
                            JOptionPane.showMessageDialog(frameBoardForm, "Failed to move '" + selectedToDoTitle + "'.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frameBoardForm, "Please select a ToDo to move.", "No ToDo Selected", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

    }

    private void filterToDoList() {
        String searchText = textFieldSearchTitle.getText().toLowerCase();
        String selectedBoard = comboBoxBoards.getSelectedItem().toString();

        ArrayList<String> allTodos = controller.getToDoListString(BoardName.valueOf(selectedBoard));
        listModel.clear();

        for (String todoTitle : allTodos) {
            if (todoTitle.toLowerCase().contains(searchText)) {
                listModel.addElement(todoTitle);
            }
        }
        jList.clearSelection();
        MoveUp.setEnabled(false);
        MoveDown.setEnabled(false);
        deleteToDoButton.setEnabled(false);
        changeBoard.setEnabled(false);
    }

    private void filterByDate() {
        String dateText = textFieldSearchDate.getText().trim();
        String selectedBoard = comboBoxBoards.getSelectedItem().toString();
        BoardName boardNameEnum = BoardName.valueOf(selectedBoard);

        if (dateText.isEmpty()) {
            ArrayList<String> allTodos = controller.getToDoListString(boardNameEnum);
            listModel.clear();
            listModel.addAll(allTodos);
            jList.clearSelection();
            MoveUp.setEnabled(false);
            MoveDown.setEnabled(false);
            deleteToDoButton.setEnabled(false);
            changeBoard.setEnabled(false);
            return;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Corrected pattern for month
            LocalDate searchDate = LocalDate.parse(dateText, formatter);

            ArrayList<ToDo> todos = controller.user.getBoard(boardNameEnum).getTodoList();

            listModel.clear();
            for (ToDo todo : todos) {
                if (todo.getDueDate() != null && todo.getDueDate().equals(searchDate)) {
                    listModel.addElement(todo.getTitle());
                }
            }
            jList.clearSelection();
            MoveUp.setEnabled(false);
            MoveDown.setEnabled(false);
            deleteToDoButton.setEnabled(false);
            changeBoard.setEnabled(false);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(frameBoardForm, "Invalid date format. Please use dd/MM/yyyy.", "Error", JOptionPane.ERROR_MESSAGE);
            listModel.clear();
            jList.clearSelection();
            MoveUp.setEnabled(false);
            MoveDown.setEnabled(false);
            deleteToDoButton.setEnabled(false);
            changeBoard.setEnabled(false);
        }
    }

    private void showTodosToday() {
        LocalDate today = LocalDate.now();

        String selected = comboBoxBoards.getSelectedItem().toString();
        BoardName boardEnum = BoardName.valueOf(selected);

        ArrayList<ToDo> todos = controller.user.getBoard(boardEnum).getTodoList();

        listModel.clear();
        for (ToDo t : todos) {
            if (t.getDueDate() != null && t.getDueDate().equals(today)) {
                listModel.addElement(t.getTitle());
            }
        }
        jList.clearSelection();
        MoveUp.setEnabled(false);
        MoveDown.setEnabled(false);
        deleteToDoButton.setEnabled(false);
        changeBoard.setEnabled(false);
    }

    private class ToDoListCellRenderer extends DefaultListCellRenderer {
        private Controller controller;
        private String currentBoardName;

        public ToDoListCellRenderer(Controller controller, String initialBoardName) {
            this.controller = controller;
            this.currentBoardName = initialBoardName;
        }

        public void setCurrentBoard(String boardName) {
            this.currentBoardName = boardName;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof String) {
                String toDoTitle = (String) value;
                BoardName boardNameEnum = BoardName.valueOf(currentBoardName);

                ToDo toDo = controller.getToDoByTitle(toDoTitle, boardNameEnum);

                if (toDo != null && toDo.getDueDate() != null) {
                    LocalDate today = LocalDate.now();

                    if (toDo.getDueDate().isBefore(today) && !"Completo".equals(toDo.getStatus())) {
                        renderer.setForeground(Color.RED);
                    } else {

                        renderer.setForeground(list.getForeground());
                    }
                } else {
                    renderer.setForeground(list.getForeground());
                }
            }

            if (isSelected) {
                renderer.setBackground(list.getSelectionBackground());
                renderer.setForeground(list.getSelectionForeground());
            } else {

                renderer.setBackground(list.getBackground());
            }
            return renderer;
        }
    }
}