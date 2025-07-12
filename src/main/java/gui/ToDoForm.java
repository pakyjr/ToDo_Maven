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
    private JComboBox <String> colorChange;
    private JPanel campo1;
    private JPanel campo2;
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

        this.colorChange.addItem("Blu");
        this.colorChange.addItem("Rosso");
        this.colorChange.addItem("Giallo");
        this.colorChange.addItem("Verde");
        this.colorChange.addItem("Arancione");
        this.colorChange.addItem("Viola");

        // Set initial colors when the form is created
        todoPanel.setBackground(new Color(160, 235, 219));
        campo1.setBackground(new Color(115, 207, 214));
        campo2.setBackground(new Color(115, 207, 214));


        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // The colors set here would be overwritten by the colorChange listener's default "Blu" case
                // if the user doesn't change the selection. It's better to manage initial colors once.
                // todoPanel.setBackground(new Color(160, 235, 219));
                // campo1.setBackground(new Color(115, 207, 214));
                // campo2.setBackground(new Color(115, 207, 214));

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

                // Assuming BoardForm.listModel is accessible and correctly manages the JList data
                if (BoardForm.listModel != null) {
                    BoardForm.listModel.clear();
                    BoardForm.listModel.addAll(controller.getToDoListString(BoardName.valueOf(cu)));
                } else {
                    System.err.println("BoardForm.listModel is null. Cannot update the list.");
                    // Optionally, show an error to the user or log it more robustly
                }

                frame.setVisible(true);
                frameToDoForm.setVisible(false);
                frameToDoForm.dispose();
            }
        });

        colorChange.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String colorSelected = (String) colorChange.getSelectedItem(); // Cast to String
                if (colorSelected != null) { // Check for null in case no item is selected
                    switch (colorSelected) {
                        case "Blu":
                            todoPanel.setBackground(new Color(160, 235, 219));
                            campo1.setBackground(new Color(115, 207, 214));
                            campo2.setBackground(new Color(115, 207, 214));
                            break;
                        case "Giallo":
                            todoPanel.setBackground(new Color(248, 255, 98));
                            campo1.setBackground(new Color(252, 214, 9));
                            campo2.setBackground(new Color(252, 214, 9));
                            break;
                        case "Rosso":
                            todoPanel.setBackground(new Color(255, 87, 84));
                            campo1.setBackground(new Color(214, 6, 11));
                            campo2.setBackground(new Color(214, 6, 11));
                            break;
                        case "Verde":
                            todoPanel.setBackground(new Color(87,255,116));
                            campo1.setBackground(new Color(0,201,20));
                            campo2.setBackground(new Color(0,201,20));
                            break;
                        case "Arancione":
                            todoPanel.setBackground(new Color(255,176,76));
                            campo1.setBackground(new Color(255,140,0));
                            campo2.setBackground(new Color(255,140,0));
                            break;
                        case "Viola":
                            todoPanel.setBackground(new Color(217,165,255));
                            campo1.setBackground(new Color(175,64,255));
                            campo2.setBackground(new Color(175,64,255));
                            break;
                    }
                }
            }
        });
    }
}