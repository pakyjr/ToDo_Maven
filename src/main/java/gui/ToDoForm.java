package gui;

import controller.Controller;
import models.ToDo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    private Controller controller;
    public ToDoForm(JFrame parent, Controller c, String n){
        frame = parent;
        this.controller = c;
        String nome = n;
        frameToDoForm=new JFrame("ToDo Creation");
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

        panelActivity.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String label = JOptionPane.showInputDialog(panelActivity, "Inserert the Activity name:");
                if (label != null && !label.trim().isEmpty()) {
                    JCheckBox checkBox = new JCheckBox(label);
                    checkBox.setBounds(e.getX(), e.getY(), 150, 20);
                    GridLayout b = new GridLayout();
                    b.setColumns(1);
                    b.setRows(1000);
                    panelActivity.setLayout(b);
                    panelActivity.add(checkBox);
                    panelActivity.revalidate();
                    panelActivity.repaint();
                }

            }
        });

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate localDate = LocalDate.parse(dueDateField.getText(), formatter);
                controller.addTodo(n , new ToDo(nameField.getText(), descriptionField.getText(), localDate, urlField.getText() ));
                JOptionPane.showMessageDialog(frameToDoForm, "ToDo created successfully!");
                //creaiamo un nuovo oggetto toDo da salvare nel database
                frame.setVisible(true);
                frameToDoForm.setVisible(false);
                frameToDoForm.dispose();
            }
        });
    }
}
