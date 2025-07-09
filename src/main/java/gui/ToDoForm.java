package gui;

import controller.Controller;
import models.board.BoardName;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = nameField.getText();
                String description = descriptionField.getText();
                String dueDateString = dueDateField.getText();
                String url = urlField.getText();

                if (title.isEmpty() || description.isEmpty() || dueDateString.isEmpty()) {
                    JOptionPane.showMessageDialog(frameToDoForm, "Please fill in all required fields.", "Missing Data", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LocalDate dueDate;
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    dueDate = LocalDate.parse(dueDateString, formatter);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(frameToDoForm, "Due Date must be in format dd/MM/yyyy.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                controller.addToDo(currentBoard, title, description, dueDateString, url);
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
