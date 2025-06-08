package gui;

import controller.Controller;
import models.ToDo;
import models.board.BoardName;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
    public JList toDoList;
    public JPanel toDoInfo;
    private JButton buttonModify;
    public JFrame frameBoardForm;

    private Controller controller;


    public BoardForm(JFrame frame, Controller c){
        frameBoardForm = new JFrame("Personal Area");
        frameBoardForm.setContentPane(board);
        frameBoardForm.pack();
        frameBoardForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.controller = c;

        this.comboBoxBoards.addItem("University");
        this.comboBoxBoards.addItem("Work");
        this.comboBoxBoards.addItem("Free Time");

        ArrayList<ToDo> toDoList = controller.user.getBoard(BoardName.valueOf(comboBoxBoards.getSelectedItem().toString())).getTodoList();
        JList<ToDo> lista = new JList<>(toDoList.toArray(new ToDo[0]));
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        buttonModify.setEnabled(false);

        lista.addListSelectionListener(e -> {
            buttonModify.setEnabled(!lista.isSelectionEmpty());
        });

        addToDoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentBoard = comboBoxBoards.getSelectedItem().toString();
                ToDoForm toDoForm = new ToDoForm(frameBoardForm, controller, currentBoard);
                toDoForm.frameToDoForm.setVisible(true);
            }
        });
    }
}
