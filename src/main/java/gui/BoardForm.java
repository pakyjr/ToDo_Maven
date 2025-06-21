package gui;

import controller.Controller;
import models.ToDo;
import models.board.BoardName;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class BoardForm {
    private JPanel board;
    private JComboBox comboBoxBoards;
    private JButton shareBoardButton;
    private JButton addToDoButton;
    private JButton orderToDoButton;
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
        this.comboBoxBoards.addItem("FREE TIME");

        listModel = new DefaultListModel<String>();
        listModel.addAll(controller.getToDoListString(BoardName.valueOf(comboBoxBoards.getSelectedItem().toString())));
        jList.setModel(listModel);

        buttonModify.setEnabled(false);
        deleteToDoButton.setEnabled(false);

        jList.addListSelectionListener(e -> {
            buttonModify.setEnabled(!jList.isSelectionEmpty());
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

    }

    private void setVisibilityToDoInfo(boolean status) {
        toDoInfo.setVisible(status);
        for (Component c : toDoInfo.getComponents()) {
            c.setVisible(status);
        }
        toDoInfo.repaint();
    }
}
