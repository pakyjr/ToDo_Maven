package gui;

import controller.Controller;
import models.ToDo; // Import ToDo model
import models.board.BoardName;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map; // Import Map for activities

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
    private JLabel image; // This JLabel displays the image
    private JComboBox <String> colorChange;
    private JPanel campo1;
    private JPanel campo2;
    private JTextField statusField;
    private JPanel panelActivity;
    public JFrame frameToDoForm, frame;

    private String currentBoard;
    private Controller controller;
    private ToDo currentToDo; // Holds the ToDo object being created or edited

    private String[] imageNames = {"lupo.png", "lettura.png", "immagine3.jpg", "immagine4.jpg", "immagine5.jpg"};
    private int currentImageIndex = 0;

    String finalStatus = "In Progress";

    public ToDoForm(JFrame parent, Controller c, String cu, ToDo toDoToEdit){
        this.frame = parent;
        this.controller = c;
        this.currentBoard = cu;
        this.currentToDo = toDoToEdit;

        frameToDoForm = new JFrame(toDoToEdit == null ? "ToDo Creation" : "Edit ToDo");
        frameToDoForm.setContentPane(todoPanel);
        frameToDoForm.pack();


        frameToDoForm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.setVisible(true);
                frameToDoForm.dispose();
            }
        });


        // Populate color combo box
        this.colorChange.addItem("Blu");
        this.colorChange.addItem("Rosso");
        this.colorChange.addItem("Giallo");
        this.colorChange.addItem("Verde");
        this.colorChange.addItem("Arancione");
        this.colorChange.addItem("Viola");

        setPanelColors("Blu");

        panelActivity.setLayout(new BoxLayout(panelActivity, BoxLayout.Y_AXIS));

        loadImage(imageNames[currentImageIndex]);

        image.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentImageIndex = (currentImageIndex + 1) % imageNames.length;
                loadImage(imageNames[currentImageIndex]);
            }
        });

        if (currentToDo != null) {
            nameField.setText(currentToDo.getTitle());
            descriptionField.setText(currentToDo.getDescription());

            if (currentToDo.getDueDate() != null) {
                dueDateField.setText(currentToDo.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            urlField.setText(currentToDo.getUrl());
            statusField.setText(currentToDo.getStatus());

            String storedImage = currentToDo.getImage();
            if (storedImage != null && !storedImage.isEmpty()) {
                for (int i = 0; i < imageNames.length; i++) {
                    if (imageNames[i].equals(storedImage)) {
                        currentImageIndex = i;
                        loadImage(imageNames[currentImageIndex]);
                        break;
                    }
                }
            } else {

                currentImageIndex = 0;
                loadImage(imageNames[currentImageIndex]);
            }


            String storedColor = currentToDo.getColor();
            if (storedColor != null && !storedColor.isEmpty()) {
                for (int i = 0; i < colorChange.getItemCount(); i++) {
                    if (colorChange.getItemAt(i).equals(storedColor)) {
                        colorChange.setSelectedIndex(i);
                        setPanelColors(storedColor);
                        break;
                    }
                }
            }



        }


        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = nameField.getText().trim();
                String description = descriptionField.getText().trim();
                String dueDateString = dueDateField.getText().trim();
                String url = urlField.getText().trim();

                if (title.isEmpty() || description.isEmpty() || dueDateString.isEmpty()) {
                    JOptionPane.showMessageDialog(frameToDoForm, "Please fill in all required fields (Title, Description, Due Date).", "Missing Data", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LocalDate dueDate;
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    dueDate = LocalDate.parse(dueDateString, formatter);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(frameToDoForm, "Due Date must be in format dd/MM/yyyy (e.g., 31/12/2025).", "Invalid Date", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                Map<String, Boolean> activitiesMap = new HashMap<>();
                for (Component comp : panelActivity.getComponents()) {
                    if (comp instanceof JCheckBox) {
                        JCheckBox cb = (JCheckBox) comp;
                        activitiesMap.put(cb.getText(), cb.isSelected());
                    }
                }


                String finalStatus = "In Progress";
                if (activitiesMap.isEmpty()) {
                    finalStatus = "In Progress";
                } else {
                    boolean allChecked = activitiesMap.values().stream().allMatch(Boolean::booleanValue);
                    if (allChecked) {
                        finalStatus = "Completo";
                    }
                }

                String selectedColor = (String) colorChange.getSelectedItem();
                String selectedImageName = imageNames[currentImageIndex]; // Get the currently selected image file name


                if (currentToDo == null) {

                    controller.addToDo(currentBoard, title, description, dueDateString, url);

                    ToDo newlyCreatedToDo = controller.getToDoByTitle(title, BoardName.valueOf(currentBoard));

                    if (newlyCreatedToDo != null) {

                        newlyCreatedToDo.setActivityList(activitiesMap);
                        newlyCreatedToDo.setStatus(finalStatus);
                        newlyCreatedToDo.setColor(selectedColor);
                        newlyCreatedToDo.setImage(selectedImageName);
                        JOptionPane.showMessageDialog(frameToDoForm, "ToDo added successfully.");
                    } else {
                        JOptionPane.showMessageDialog(frameToDoForm, "Failed to retrieve new ToDo for full setup. Data might be incomplete.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {

                    String oldTitle = currentToDo.getTitle(); // Keep track of old title for update in controller

                    controller.updateToDo(currentBoard, oldTitle, title, description, dueDateString, url);
                    currentToDo.setTitle(title);
                    currentToDo.setDescription(description);
                    currentToDo.setDueDate(dueDate);
                    currentToDo.setUrl(url);
                    currentToDo.setActivityList(activitiesMap);
                    currentToDo.setStatus(finalStatus);
                    currentToDo.setColor(selectedColor);
                    currentToDo.setImage(selectedImageName);
                    JOptionPane.showMessageDialog(frameToDoForm, "ToDo updated successfully.");
                }


                if (BoardForm.listModel != null) {
                    BoardForm.listModel.clear();
                    BoardForm.listModel.addAll(controller.getToDoListString(BoardName.valueOf(currentBoard)));
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
                setPanelColors(colorSelected);
            }
        });


        panelActivity.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String label = JOptionPane.showInputDialog(panelActivity, "Insert the Activity name:");
                if (label != null && !label.trim().isEmpty()) {
                    JCheckBox checkBox = new JCheckBox(label);


                    checkBox.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            checkCompletionStatus();
                        }
                    });
                    panelActivity.add(checkBox);
                    panelActivity.revalidate();
                    panelActivity.repaint();

                    if (currentToDo != null) {
                        currentToDo.addActivity(label);
                    }
                }
                checkCompletionStatus();
            }
        });
    }


    private void setPanelColors(String colorSelected) {
        if (colorSelected == null) return;

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
        } else if (checkBoxCount > 0 && !allChecked) {
            statusField.setText("In Progress");
        }

        if (currentToDo != null) {
            currentToDo.setStatus(statusField.getText());
        }
    }


    private void loadImage(String imageName) {
        try {
            URL imageUrl = getClass().getResource("/images/" + imageName);

            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(imageUrl);
                Image img = icon.getImage();

                Image scaledImg = img.getScaledInstance(image.getWidth() > 0 ? image.getWidth() : 150,
                        image.getHeight() > 0 ? image.getHeight() : 150,
                        Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImg);
                image.setIcon(icon);
                image.setText("");
            } else {
                System.err.println("Error: Image '" + imageName + "' not found in /images/");
                image.setText("Image not found!");
                image.setIcon(null); 
            }
        } catch (Exception ex) {
            System.err.println("Error loading image '" + imageName + "': " + ex.getMessage());
            ex.printStackTrace();
            image.setText("Loading error!");
            image.setIcon(null);
        }
    }
}