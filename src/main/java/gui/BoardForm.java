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
    private JLabel labelSearch;
    private JTextField textFieldSearchDate;
    private JLabel labelSearchDate;
    private JButton buttonOrderByDate;
    private JButton MoveDown;
    public JFrame frameBoardForm;

    public static DefaultListModel<String> listModel;
    private Controller controller;


    public BoardForm(JFrame frame, Controller c){
        frameBoardForm = new JFrame("Personal Area");
        frameBoardForm.setContentPane(board);
        frameBoardForm.pack();
        frameBoardForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        this.controller = c;

        this.comboBoxBoards.addItem("UNIVERSITY");
        this.comboBoxBoards.addItem("WORK");
        this.comboBoxBoards.addItem("FREE_TIME");

        listModel = new DefaultListModel<String>();
        jList.setModel(listModel);

        MoveUp.setEnabled(false);
        MoveDown.setEnabled(false);
        deleteToDoButton.setEnabled(false);


        jList.addListSelectionListener(e -> {
            boolean isSelected = !jList.isSelectionEmpty();
            MoveUp.setEnabled(isSelected && jList.getSelectedIndex() > 0);
            MoveDown.setEnabled(isSelected && jList.getSelectedIndex() < listModel.getSize() - 1);
            deleteToDoButton.setEnabled(isSelected);
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

                todos.sort(Comparator.comparing(ToDo::getDueDate));

                listModel.clear();
                for (ToDo todo : todos) {
                    listModel.addElement(todo.getTitle());
                }
                jList.clearSelection();
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
    }

    private void filterByDate() {
        String dateText = textFieldSearchDate.getText().trim();
        if (dateText.isEmpty()) {
            String selectedBoard = comboBoxBoards.getSelectedItem().toString();
            ArrayList<String> allTodos = controller.getToDoListString(BoardName.valueOf(selectedBoard));
            listModel.clear();
            listModel.addAll(allTodos);
            jList.clearSelection();
            return;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate searchDate = LocalDate.parse(dateText, formatter);
            String selectedBoard = comboBoxBoards.getSelectedItem().toString();
            ArrayList<ToDo> todos = controller.user.getBoard(BoardName.valueOf(selectedBoard)).getTodoList();

            listModel.clear();
            for (ToDo todo : todos) {
                if (todo.getDueDate().equals(searchDate)) {
                    listModel.addElement(todo.getTitle());
                }
            }
            jList.clearSelection();
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(frameBoardForm, "Invalid date format. Please use dd/MM/yyyy.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTodosToday() {
        LocalDate today = LocalDate.now();

        String selected = comboBoxBoards.getSelectedItem().toString();
        BoardName board = BoardName.valueOf(selected);

        ArrayList<ToDo> todos = controller.user.getBoard(board).getTodoList();

        listModel.clear();
        for (ToDo t : todos) {
            if (t.getDueDate().equals(today)) {
                listModel.addElement(t.getTitle());
            }
        }
        jList.clearSelection();
    }
}