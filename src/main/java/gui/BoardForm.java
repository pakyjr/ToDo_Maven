package gui;

import controller.Controller;
import models.ToDo;
import models.board.BoardName;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;

public class BoardForm {
    private JPanel board;
    private JComboBox comboBoxBoards;
    private JButton shareBoardButton;
    private JButton addToDoButton;
    private JButton orderToDoByTitleButton;
    private JButton deleteToDoButton;
    private JButton dueDateButton;
    public JScrollPane ScrollPanel;
    public JPanel toDoInfo;
    private JButton buttonModify;
    private JList jList;
    private JLabel labelText;
    private JLabel labelDescription;
    private JLabel labelUrl;
    private JLabel labelDueDate;
    private JLabel labelOwner;
    private JTextField textFieldSearchTitle;
    private JLabel labelSearch;
    private JTextField textFieldSearchDate;
    private JLabel labelSearchDate;
    private JButton buttonOrderByDate;
    public JFrame frameBoardForm;

    public static DefaultListModel<String> listModel;
    private Controller controller;


    public BoardForm(JFrame frame, Controller c){
        frameBoardForm = new JFrame("Personal Area");
        frameBoardForm.setContentPane(board);
        frameBoardForm.pack();
        frameBoardForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisibilityToDoInfo(false);
        this.controller = c;

        this.comboBoxBoards.addItem("UNIVERSITY");
        this.comboBoxBoards.addItem("WORK");
        this.comboBoxBoards.addItem("FREE_TIME");

        listModel = new DefaultListModel<String>();
        jList.setModel(listModel);

        buttonModify.setEnabled(false);
        deleteToDoButton.setEnabled(false);
        jList.addListSelectionListener(e -> {
            buttonModify.setEnabled(!jList.isSelectionEmpty());
            deleteToDoButton.setEnabled(!jList.isSelectionEmpty());
        });

        addToDoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentBoard = comboBoxBoards.getSelectedItem().toString();
                ToDoForm toDoForm = new ToDoForm(frameBoardForm, controller, currentBoard);
                toDoForm.frameToDoForm.setVisible(true);
            }
        });


        jList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String title = ((String) jList.getSelectedValue());

                ToDo todo = controller.getToDoByTitle(title, BoardName.valueOf(comboBoxBoards.getSelectedItem().toString()));
                if(todo != null) {
                    labelText.setText(todo.getTitle());
                    labelDescription.setText(todo.getDescription());
                    labelUrl.setText(todo.getUrl());
                    labelDueDate.setText(todo.getDueDate().toString());
                    labelOwner.setText(todo.getOwner());

                    setVisibilityToDoInfo(true);
                    frameBoardForm.setSize(500, 300);
                    frameBoardForm.repaint();
                }
                else {
                    setVisibilityToDoInfo(false);
                    frameBoardForm.setSize(300, 300);
                    frameBoardForm.repaint();


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

                setVisibilityToDoInfo(false);
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

                setVisibilityToDoInfo(false);
            }
        });

        buttonModify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentBoard = comboBoxBoards.getSelectedItem().toString();
                ModifyToDoForm toDoForm = new ModifyToDoForm(frameBoardForm, controller, currentBoard);
                toDoForm.frameModifyToDoForm.setVisible(true);
            }

        });
    }

    private void setVisibilityToDoInfo(boolean status) {
        toDoInfo.setVisible(status);
        for (Component c : toDoInfo.getComponents()) {
            c.setVisible(status);
        }
        toDoInfo.repaint();
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
    }

    private void filterByDate() {
        String dateText = textFieldSearchDate.getText().trim();
        if (dateText.isEmpty()) {
            filterToDoList();
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
        } catch (DateTimeParseException e) {

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
        setVisibilityToDoInfo(false);
        
    }

}
