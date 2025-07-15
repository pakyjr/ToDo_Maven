package gui;

import controller.Controller;
import models.ToDo;
import models.board.BoardName; // Keep this import for getBoardNameFromString if still used
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ToDoForm {
    private JPanel todoPanel;
    private JTextField nameField;
    private JTextField descriptionField;
    private JTextField dueDateField;
    private JTextField urlField;
    private JButton buttonSave;
    private JLabel image;
    private JComboBox <String> colorChange;
    private JPanel campo1;
    private JPanel campo2;
    private JTextField statusField;
    private JPanel panelActivity;
    private JButton deleteButton;
    private JButton openURL;
    private JTextField ownerfield; // This field will display the creator's username
    public JFrame frameToDoForm, frame;

    private String currentBoard;
    private Controller controller;
    private ToDo currentToDo; // This ToDo object now has an 'owner' field
    private String[] imageNames = {"read.jpg", "art.jpg", "happybirthday.jpg", "happyhalloween.jpg", "happynewyear.jpg",
            "santa.jpg", "music.jpg", "choco.jpg", "coffee.jpg", "sweet.jpg","film.jpg",
            "filo.jpg","game.jpg", "graduated.jpg", "mountain.jpg", "pool.jpg", "sher.jpg",
            "theatre.jpg", "sport.jpg", "study.jpg", "martial.jpg"};
    private int currentImageIndex = 0;


    public ToDoForm(JFrame parent, Controller c, String cu, ToDo toDoToEdit){
        this.frame = parent;
        this.controller = c;
        this.currentBoard = cu;
        this.currentToDo = toDoToEdit; // currentToDo's 'owner' field will be used

        frameToDoForm = new JFrame(toDoToEdit == null ? "ToDo Creation" : "Edit ToDo");
        frameToDoForm.setContentPane(todoPanel);
        frameToDoForm.pack();

        frameToDoForm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // When closing the ToDoForm, refresh the BoardForm's list
                // Ensure the list is repopulated based on the current board
                if (BoardForm.listModel != null) {
                    BoardForm.listModel.clear();
                    BoardForm.listModel.addAll(controller.getToDoListString(currentBoard));
                }
                frame.setVisible(true); // Make the BoardForm visible again
                frameToDoForm.dispose(); // Close the ToDoForm
            }
        });

        this.colorChange.addItem("Blue");
        this.colorChange.addItem("Red");
        this.colorChange.addItem("Yellow");
        this.colorChange.addItem("Green");
        this.colorChange.addItem("Orange");
        this.colorChange.addItem("Violet");

        colorChange.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        setPanelColors((String) colorChange.getSelectedItem());

        panelActivity.setLayout(new BoxLayout(panelActivity, BoxLayout.Y_AXIS));

        loadImage(imageNames[currentImageIndex]);

        image.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentImageIndex = (currentImageIndex + 1) % imageNames.length;
                loadImage(imageNames[currentImageIndex]);
            }
        });

        // Set owner field as non-editable as it represents the fixed creator of the ToDo
        ownerfield.setEditable(false);
        statusField.setEditable(false);

        // --- Initialization Logic ---
        if (currentToDo != null) {
            // Editing an existing ToDo
            nameField.setText(currentToDo.getTitle());
            descriptionField.setText(currentToDo.getDescription());

            if (currentToDo.getDueDate() != null) {
                dueDateField.setText(currentToDo.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            urlField.setText(currentToDo.getUrl());

            // Set the owner field from the current ToDo's owner
            ownerfield.setText(currentToDo.getOwner());

            // Disable editing for all fields if the current user is NOT the owner
            // This prevents recipients of shared ToDos from altering the original.
            if (!controller.isCurrentUserToDoCreator(currentToDo)) { // Use the new Controller method
                nameField.setEditable(false);
                descriptionField.setEditable(false);
                dueDateField.setEditable(false);
                urlField.setEditable(false);
                colorChange.setEnabled(false);
                image.setEnabled(false); // Disable image clicking
                panelActivity.setEnabled(false); // Disable adding activities
                buttonSave.setEnabled(false); // Disable saving
                deleteButton.setEnabled(false); // Disable deleting activities
            }


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

            if (currentToDo.getActivityList() != null && !currentToDo.getActivityList().isEmpty()) {
                for (Map.Entry<String, Boolean> entry : currentToDo.getActivityList().entrySet()) {
                    JCheckBox checkBox = new JCheckBox(entry.getKey());
                    checkBox.setSelected(entry.getValue());
                    // Only allow activity status changes if the current user is the creator or if you want shared users to track their own progress
                    // For now, let's assume only creator can change activities.
                    if (!controller.isCurrentUserToDoCreator(currentToDo)) {
                        checkBox.setEnabled(false);
                    } else {
                        checkBox.addItemListener(e -> checkCompletionStatus());
                    }
                    panelActivity.add(checkBox);
                }
                panelActivity.revalidate();
                panelActivity.repaint();
                checkCompletionStatus();
            } else {
                statusField.setText("Not Started");
            }
        } else {
            // Creating a new ToDo
            statusField.setText("Not Started");
            // Set the owner field to the currently logged-in user's username
            ownerfield.setText(controller.user.getUsername());
        }


        openURL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String urlText = urlField.getText().trim();
                if (urlText.isEmpty()) {
                    JOptionPane.showMessageDialog(frameToDoForm, "URL field is empty. Please enter a link.", "No URL Provided", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                if (!urlText.startsWith("http://") && !urlText.startsWith("https://")) {
                    urlText = "http://" + urlText;
                }

                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(urlText));
                    } else {
                        JOptionPane.showMessageDialog(frameToDoForm, "Your system does not support opening web links automatically.", "Feature Not Supported", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frameToDoForm,
                            "Failed to open URL: " + ex.getMessage() +
                                    "\nPlease ensure the link is a valid web address (e.g., www.google.com or https://example.com).",
                            "Error Opening URL", JOptionPane.ERROR_MESSAGE);
                    System.err.println("Error attempting to open URL: " + urlText + " - " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = nameField.getText().trim();
                String description = descriptionField.getText().trim();
                String dueDateString = dueDateField.getText().trim();
                String url = urlField.getText().trim();
                String owner = ownerfield.getText(); // This is the creator's username

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

                String calculatedStatus;
                if (activitiesMap.isEmpty()) {
                    calculatedStatus = "Not Started";
                } else {
                    boolean allChecked = activitiesMap.values().stream().allMatch(Boolean::booleanValue);
                    if (allChecked) {
                        calculatedStatus = "Complete";
                    } else {
                        calculatedStatus = "Incomplete";
                    }
                }

                String selectedColor = (String) colorChange.getSelectedItem();
                String selectedImageName = imageNames[currentImageIndex];

                if (currentToDo == null) {
                    // Creating a new ToDo
                    // Pass the owner (current logged-in user) to the controller
                    controller.addToDo(currentBoard, title, description, dueDateString, url, selectedColor, selectedImageName, activitiesMap, calculatedStatus, owner); // ADDED OWNER
                    JOptionPane.showMessageDialog(frameToDoForm, "ToDo added successfully.");
                } else {
                    // Updating an existing ToDo
                    // Ensure the current user is the owner before updating
                    if (!controller.isCurrentUserToDoCreator(currentToDo)) {
                        JOptionPane.showMessageDialog(frameToDoForm, "You can only edit ToDos you created.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    String oldTitle = currentToDo.getTitle();
                    // Pass the existing owner (currentToDo.getOwner()) to the controller
                    controller.updateToDo(currentBoard, oldTitle, title, description, dueDateString, url, selectedColor, selectedImageName, activitiesMap, calculatedStatus, currentToDo.getOwner()); // ADDED CURRENT_TODO.GETOWNER()

                    // Update the in-memory currentToDo object
                    currentToDo.setTitle(title);
                    currentToDo.setDescription(description);
                    currentToDo.setDueDate(dueDate);
                    currentToDo.setUrl(url);
                    currentToDo.setActivityList(activitiesMap);
                    currentToDo.setColor(selectedColor);
                    currentToDo.setImage(selectedImageName);
                    // No need to update owner for existing ToDo, it's fixed.

                    JOptionPane.showMessageDialog(frameToDoForm, "ToDo updated successfully.");
                }

                // Refresh the BoardForm's JList
                if (BoardForm.listModel != null) {
                    BoardForm.listModel.clear();
                    BoardForm.listModel.addAll(controller.getToDoListString(currentBoard));
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

        // Event listener for adding activities
        panelActivity.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Only allow adding activities if the current user is the owner
                if (currentToDo != null && !controller.isCurrentUserToDoCreator(currentToDo)) {
                    JOptionPane.showMessageDialog(frameToDoForm, "You can only add activities to ToDos you created.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                    return;
                }

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
                        currentToDo.addActivity(label); // This will update the ToDo's activity list in memory
                    }
                    // For a new ToDo, activities are just added to the UI and will be saved when "Save" is clicked.
                }
                checkCompletionStatus();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Only allow deleting activities if the current user is the owner
                if (currentToDo != null && !controller.isCurrentUserToDoCreator(currentToDo)) {
                    JOptionPane.showMessageDialog(frameToDoForm, "You can only delete activities from ToDos you created.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                ArrayList<JCheckBox> selectedCheckBoxes = new ArrayList<>();
                for (Component comp : panelActivity.getComponents()) {
                    if (comp instanceof JCheckBox) {
                        JCheckBox cb = (JCheckBox) comp;
                        if (cb.isSelected()) {
                            selectedCheckBoxes.add(cb);
                        }
                    }
                }

                if (selectedCheckBoxes.isEmpty()) {
                    JOptionPane.showMessageDialog(frameToDoForm, "Please select at least one activity to delete.", "No Activity Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int confirmResult = JOptionPane.showConfirmDialog(frameToDoForm,
                        "Are you sure you want to delete the selected activity(ies)?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION);

                if (confirmResult == JOptionPane.YES_OPTION) {
                    for (JCheckBox cb : selectedCheckBoxes) {
                        panelActivity.remove(cb);
                        if (currentToDo != null) {
                            currentToDo.deleteActivity(cb.getText()); // This will update the ToDo's activity list in memory
                        }
                    }
                    panelActivity.revalidate();
                    panelActivity.repaint();
                    checkCompletionStatus();
                    JOptionPane.showMessageDialog(frameToDoForm, "Selected activity(ies) deleted successfully.");
                }
            }
        });
    }

    private void setPanelColors(String colorSelected) {
        if (colorSelected == null) return;

        switch (colorSelected) {
            case "Blue":
                todoPanel.setBackground(new Color(160, 235, 219));
                campo1.setBackground(new Color(115, 207, 214));
                campo2.setBackground(new Color(115, 207, 214));
                break;
            case "Yellow":
                todoPanel.setBackground(new Color(248, 255, 98));
                campo1.setBackground(new Color(252, 214, 9));
                campo2.setBackground(new Color(252, 214, 9));
                break;
            case "Red":
                todoPanel.setBackground(new Color(255, 87, 84));
                campo1.setBackground(new Color(214, 6, 11));
                campo2.setBackground(new Color(214, 6, 11));
                break;
            case "Green":
                todoPanel.setBackground(new Color(87,255,116));
                campo1.setBackground(new Color(0,201,20));
                campo2.setBackground(new Color(0,201,20));
                break;
            case "Orange":
                todoPanel.setBackground(new Color(255,176,76));
                campo1.setBackground(new Color(255,140,0));
                campo2.setBackground(new Color(255,140,0));
                break;
            case "Violet":
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

        if (checkBoxCount == 0) {
            statusField.setText("Not Started");
        } else if (allChecked) {
            statusField.setText("Complete");
        } else {
            statusField.setText("Incomplete");
        }

        if (currentToDo != null) {
            // Update the status in the currentToDo object if it exists
            currentToDo.setStatus(statusField.getText());
        }
        // No need to call controller.updateToDo here, it will be done on buttonSave click.
    }

    private void loadImage(String imageName) {
        try {
            URL imageUrl = getClass().getResource("/images/" + imageName);

            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(imageUrl);
                Image img = icon.getImage();

                Image scaledImg = img.getScaledInstance(image.getWidth() > 0 ? image.getWidth() : 180,
                        image.getHeight() > 0 ? image.getHeight() : 195,
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

    // This method is likely no longer needed if BoardName.fromDisplayName is used directly
    // or if the enum is directly mapped.
    // However, if you still use it somewhere, keep it.
    private BoardName getBoardNameFromString(String boardName) {
        String formattedBoardName = boardName.toUpperCase();
        if (formattedBoardName.equals("FREE TIME")) {
            formattedBoardName = "FREE_TIME";
        }
        return BoardName.valueOf(formattedBoardName);
    }
}