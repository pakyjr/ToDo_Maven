package gui;

import controller.Controller;
import models.board.BoardName;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
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
    private JLabel image;
    private JComboBox <String> colorChange;
    private JPanel campo1;
    private JPanel campo2;
    private JTextField statusField;
    private JPanel panelActivity;
    public JFrame frameToDoForm, frame;

    private String currentBoard;
    private Controller controller;

    private String[] imageNames = {"lupo.png", "lettura.png", "immagine3.jpg", "immagine4.jpg", "immagine5.jpg"};
    private int currentImageIndex = 0;

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

        todoPanel.setBackground(new Color(160, 235, 219));
        campo1.setBackground(new Color(115, 207, 214));
        campo2.setBackground(new Color(115, 207, 214));

        loadImage(imageNames[currentImageIndex]);

        image.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentImageIndex = (currentImageIndex + 1) % imageNames.length;
                loadImage(imageNames[currentImageIndex]);
            }
        });


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
                JOptionPane.showMessageDialog(frameToDoForm, "ToDo added successfully.");

                if (BoardForm.listModel != null) {
                    BoardForm.listModel.clear();
                    BoardForm.listModel.addAll(controller.getToDoListString(BoardName.valueOf(cu)));
                } else {
                    System.err.println("BoardForm.listModel is null. Cannot update the list.");
                }

                frame.setVisible(true);
                frameToDoForm.setVisible(false);
                frameToDoForm.dispose();
            }
        });

        colorChange.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String colorSelected = (String) colorChange.getSelectedItem();
                if (colorSelected != null) {
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
        panelActivity.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String label = JOptionPane.showInputDialog(panelActivity, "Inserert the Activity name:");
                if (label != null && !label.trim().isEmpty()) {
                    JCheckBox checkBox = new JCheckBox(label);

                    checkBox.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            checkCompletionStatus();
                        }
                    });
                    GridLayout b = new GridLayout();
                    b.setColumns(1);
                    b.setRows(100);
                    panelActivity.setLayout(b);
                    panelActivity.add(checkBox);
                    panelActivity.revalidate();
                    panelActivity.repaint();
                }
                checkCompletionStatus();
            }
        });
    }
    private void checkCompletionStatus() {
        boolean allChecked = true;
        int checkBoxCount = 0;

        for (Component comp : panelActivity.getComponents()) {
            if (comp instanceof JCheckBox) {
                checkBoxCount++;
                JCheckBox cb = (JCheckBox) comp;
                if (!cb.isSelected()) {
                    allChecked = false;
                    break;
                }
            }
        }


        if (checkBoxCount > 0 && allChecked) {
            statusField.setText("Completo");
        } else {
            statusField.setText(""); // Clear the status if not all are complete
        }
    }
    private void loadImage(String imageName) {
        try {

            URL imageUrl = getClass().getResource("/images/" + imageName);

            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(imageUrl);

                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImg);

                image.setIcon(icon);
                image.setText("");
            } else {
                System.err.println("Errore: Immagine '" + imageName + "' non trovata nel percorso /images/");
                image.setText("Immagine non trovata!");
            }
        } catch (Exception ex) {
            System.err.println("Errore durante il caricamento dell'immagine '" + imageName + "': " + ex.getMessage());
            ex.printStackTrace();
            image.setText("Errore caricamento!");
        }
    }
}