package gui;

import controller.Controller;
import models.ToDo;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

    public static DefaultListModel<String> listModel;
    private Controller controller;

//prova1
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

        listModel = new DefaultListModel<String>();
        listModel.addAll(controller.getTodoTitles());

        toDoList.setModel(listModel);

        addToDoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ToDoForm toDoForm = new ToDoForm(frameBoardForm, controller, comboBoxBoards.getSelectedItem().toString());
                toDoForm.frameToDoForm.setVisible(true);
            }
        });


        toDoList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String title = ((String) toDoList.getSelectedValue());

                ToDo t = controller.(title);
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
                    contactGuiFrame.setSize(300, 300);
                    contactGuiFrame.repaint();
                }
            }
        });
    }

    private void setVisibilityContactInfo(boolean status)
    {
        contactInfo.setVisible(status);
        for (Component c : contactInfo.getComponents()) {
            c.setVisible(status);
        }
        contactInfo.repaint();
    }
            }
        });
    }
}
