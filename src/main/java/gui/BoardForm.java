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
    public JList toDoList;
    public JPanel toDoInfo;
    private JButton buttonModify;
    private JLabel labelDescription;
    private JLabel descriptionText;
    private JLabel dueDateText;
    private JLabel urlText;
    private JLabel statusText;
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
        this.comboBoxBoards.addItem("FREE TIME");

        listModel = new DefaultListModel<String>();
        listModel.addAll(controller.toDoStringList(BoardName.valueOf(comboBoxBoards.getSelectedItem().toString())));
        toDoList.setModel(listModel);

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


        toDoList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String title = ((String) toDoList.getSelectedValue());
                /*
                Book b = controller.getBookByTitle(title);
                if(b != null) {
                    bookName.setText(b.getTitle());
                    bookAuthor.setText(b.getAuthor());
                    bookGenre.setText(b.getGenre());
                    bookYear.setText(b.getYear()+"");
                    setVisibilityContactInfo(true);
                    contactGuiFrame.setSize(500, 300);
                    contactGuiFrame.repaint();
                }
                else {
                    setVisibilityContactInfo(false);
                    frameBoardForm.setSize(300, 300);
                    frameBoardForm.repaint();


                }
                
                 */
            }


        });

    }

    private void setVisibilityContactInfo(boolean status)
    {
        toDoInfo.setVisible(status);
        for (Component c : toDoInfo.getComponents()) {
            c.setVisible(status);
        }
        toDoInfo.repaint();
    }
}
