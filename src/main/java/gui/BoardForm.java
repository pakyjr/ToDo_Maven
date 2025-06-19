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
    private JLabel DescriptionLabel;
    private JLabel DescriptionText;
    private JLabel DueDateLabel;
    private JLabel DueDateText;
    private JLabel UrlLabel;
    private JLabel UrlText;
    private JLabel TitleLabel;
    private JLabel TitleText;
    private JComboBox comboBoxBoards;
    private JButton shareBoardButton;
    private JButton addToDoButton;
    private JButton orderToDoButton;
    private JButton deleteToDoButton;
    private JButton dueDateButton;
    public JScrollPane ScrollPanel;
    public JList toDoList;
    public JPanel infoToDo;
    private JButton buttonModify;


    public static DefaultListModel<String> listModel;
    private Controller controller;
    public static JFrame BoardForm;


    public BoardForm(JFrame frame, Controller c){
        setVisibilityinfoToDo(false);
        BoardForm = new JFrame("Personal Area");
        BoardForm.setContentPane(new BoardForm().board);
        BoardForm.pack();
        BoardForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BoardForm.setVisible(true);
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
                ToDoForm newFrame = new ToDoForm(frameBoardForm, controller);
                toDoForm.frameToDoForm.setVisible(true);
            }
        });



        toDoList.addListSelectionListener(new ListSelectionListener(){

        public void valueChanged(ListSelectionEvent e){
            String title = ((String) toDoList.getSelectedValue());

            ToDo b = controller.getToDoByTitle(title);
            if(b != null){
                TitleText.setText(b.getTitle());//metodo set capire nome
                DescriptionText.setText(b.getDescription());
                DueDateText.setText(b.getDueDate());
                UrlText.setUrl(b.getUrl());
                BoardForm.repaint();
            }
            else{
                setVisibilityinfoToDo(false);
                BoardForm.repaint();
            }
        }

        });

    }

    private void setVisibilityinfoToDo(boolean status)
    {
        infoToDo.setVisible(status);
        for (Component c )
    }

}
