package gui;

import controller.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    public JFrame frameBoardForm;

    private Controller controller;


    public BoardForm(JFrame frame, Controller c){
    frameBoardForm = new JFrame("Personal Area");
    frameBoardForm.setContentPane(board);
    frameBoardForm.pack();
        frameBoardForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.controller = c;

        this.comboBoxBoards.addItem("My boards");
        this.comboBoxBoards.addItem("University");
        this.comboBoxBoards.addItem("Work");
        this.comboBoxBoards.addItem("Free Time");


        addToDoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ToDoForm toDoForm = new ToDoForm(frameBoardForm);
                toDoForm.frameToDoForm.setVisible(true);
            }
        });
    }
}
