package gui;

import controller.Controller;
import models.board.BoardName;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ToDoForm {
    private JPanel todoPanel;
    private JTextField nameField;
    private JTextField descriptionField;
    private JTextField dueDateField;
    private JTextField urlField;
    private JButton shareButton;
    private JButton changeShareButton;
    private JComboBox members;
    private JButton buttonSave;
    private JPanel panelActivity;
    private JLabel image;
    public JFrame frameToDoForm, frame;

    private String currentBoard;
    private Controller controller;

    public ToDoForm(JFrame parent, Controller c, String cu){
        this.frame = parent;
        this.controller = c;
        this.currentBoard = cu;

        frameToDoForm = new JFrame("ToDo Creation");
        frameToDoForm.setContentPane(todoPanel);
        frameToDoForm.pack();

        frameToDoForm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                frame.setVisible(true);
                frameToDoForm.setVisible(false);
                frameToDoForm.dispose();
            }
        });


        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.addToDo(currentBoard, nameField.getText(), descriptionField.getText(), dueDateField.getText(), urlField.getText());
                JOptionPane.showMessageDialog(frameToDoForm, "Book added successfully.");

                BoardForm.listModel.clear();
                BoardForm.listModel.addAll(controller.getToDoListString(BoardName.valueOf(cu)));
                //creaiamo un nuovo oggetto toDo da salvare nel database
                frame.setVisible(true);
                frameToDoForm.setVisible(false);
                frameToDoForm.dispose();
            }
        });
    }
}
