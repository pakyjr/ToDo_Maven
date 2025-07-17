package gui;

import controller.Controller;
import models.ToDo;
import models.User;
import models.board.BoardName;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    private JTextField ownerfield;
    private JButton shareToDo;
    private JButton changeSharing;
    private JComboBox<String> membersToDo;
    public JFrame frameToDoForm, frame;

    private String currentBoard;
    private Controller controller;
    private ToDo currentToDo;
    private String[] imageNames = {"read.jpg", "art.jpg", "happybirthday.jpg", "happyhalloween.jpg", "happynewyear.jpg",
            "santa.jpg", "music.jpg", "choco.jpg", "coffee.jpg", "sweet.jpg","film.jpg",
            "filo.jpg","game.jpg", "graduated.jpg", "mountain.jpg", "pool.jpg", "sher.jpg",
            "theatre.jpg", "sport.jpg", "study.jpg", "martial.jpg"};
    private int currentImageIndex = 0;


    public ToDoForm(JFrame parent, Controller c, String cu, ToDo toDoToEdit){
        this.frame = parent;
        this.controller = c;
        this.currentBoard = cu;
        this.currentToDo = toDoToEdit;

        frameToDoForm = new JFrame(toDoToEdit == null ? "ToDo Creation" : "Edit ToDo");
        frameToDoForm.setContentPane(todoPanel);
        todoPanel.setPreferredSize(new Dimension(800,600));
        frameToDoForm.pack();

        frameToDoForm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (BoardForm.listModel != null) {
                    BoardForm.listModel.clear();
                    BoardForm.listModel.addAll(controller.getToDoListString(currentBoard));
                }
                frame.setVisible(true);
                frameToDoForm.dispose();
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

        ownerfield.setEditable(false);
        statusField.setEditable(false);


        if (currentToDo != null) {

            nameField.setText(currentToDo.getTitle());
            descriptionField.setText(currentToDo.getDescription());

            if (currentToDo.getDueDate() != null) {
                dueDateField.setText(currentToDo.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            urlField.setText(currentToDo.getUrl());

            ownerfield.setText(currentToDo.getOwner());

            if (!controller.isCurrentUserToDoCreator(currentToDo)) {
                nameField.setEditable(false);
                descriptionField.setEditable(false);
                dueDateField.setEditable(false);
                urlField.setEditable(false);
                colorChange.setEnabled(false);
                image.setEnabled(false);
                panelActivity.setEnabled(false);
                buttonSave.setEnabled(false);
                deleteButton.setEnabled(false);
                shareToDo.setEnabled(false);
                changeSharing.setEnabled(false);
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
            if (controller.isCurrentUserToDoCreator(currentToDo)) {
                populateMembersComboBox();
            } else {
                membersToDo.setEnabled(false);
            }
        } else {
            // Creating a new ToDo
            statusField.setText("Not Started");
            ownerfield.setText(controller.user.getUsername());
            membersToDo.setEnabled(false);
            shareToDo.setEnabled(false);
            changeSharing.setEnabled(false);
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
                String owner = ownerfield.getText();

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

                    String newToDoId = controller.addToDo(currentBoard, title, description, dueDateString, url, selectedColor, selectedImageName, activitiesMap, calculatedStatus, owner);
                    if (newToDoId != null) {
                        currentToDo = controller.getToDoByTitle(title, currentBoard); // Fetch the newly created ToDo object
                        JOptionPane.showMessageDialog(frameToDoForm, "ToDo added successfully.");
                        shareToDo.setEnabled(true);
                        changeSharing.setEnabled(true);
                        membersToDo.setEnabled(true);
                        populateMembersComboBox();
                    } else {
                        JOptionPane.showMessageDialog(frameToDoForm, "Failed to add ToDo.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {

                    if (!controller.isCurrentUserToDoCreator(currentToDo)) {
                        JOptionPane.showMessageDialog(frameToDoForm, "You can only edit ToDos you created.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    String oldTitle = currentToDo.getTitle();
                    controller.updateToDo(currentBoard, oldTitle, title, description, dueDateString, url, selectedColor, selectedImageName, activitiesMap, calculatedStatus, currentToDo.getOwner());

                    currentToDo.setTitle(title);
                    currentToDo.setDescription(description);
                    currentToDo.setDueDate(dueDate);
                    currentToDo.setUrl(url);
                    currentToDo.setActivityList(activitiesMap);
                    currentToDo.setColor(selectedColor);
                    currentToDo.setImage(selectedImageName);

                    JOptionPane.showMessageDialog(frameToDoForm, "ToDo updated successfully.");
                }

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

        panelActivity.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
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
                        currentToDo.addActivity(label);
                    }
                }
                checkCompletionStatus();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                            currentToDo.deleteActivity(cb.getText());
                        }
                    }
                    panelActivity.revalidate();
                    panelActivity.repaint();
                    checkCompletionStatus();
                    JOptionPane.showMessageDialog(frameToDoForm, "Selected activity(ies) deleted successfully.");
                }
            }
        });

        shareToDo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentToDo == null) {
                    JOptionPane.showMessageDialog(frameToDoForm, "Please save the ToDo first before sharing.", "Cannot Share", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!controller.isCurrentUserToDoCreator(currentToDo)) {
                    JOptionPane.showMessageDialog(frameToDoForm, "Only the creator can share this ToDo.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Set<User> allUsers = controller.getAllUsers();
                Set<String> alreadySharedUsernames = new HashSet<>();
                for(User u : currentToDo.getUsers()){
                    alreadySharedUsernames.add(u.getUsername());
                }
                alreadySharedUsernames.add(currentToDo.getOwner());

                java.util.List<String> availableUsers = new ArrayList<>();
                for (User user : allUsers) {
                    if (!alreadySharedUsernames.contains(user.getUsername())) {
                        availableUsers.add(user.getUsername());
                    }
                }

                if (availableUsers.isEmpty()) {
                    JOptionPane.showMessageDialog(frameToDoForm, "No other users available to share with.", "No Users", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String[] userChoices = availableUsers.toArray(new String[0]);
                JList<String> userList = new JList<>(userChoices);
                userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

                JScrollPane scrollPane = new JScrollPane(userList);
                scrollPane.setPreferredSize(new Dimension(200, 150));

                int option = JOptionPane.showConfirmDialog(frameToDoForm, scrollPane, "Select Users to Share With", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (option == JOptionPane.OK_OPTION) {
                    java.util.List<String> selectedUsernames = userList.getSelectedValuesList();
                    if (selectedUsernames.isEmpty()) {
                        JOptionPane.showMessageDialog(frameToDoForm, "No users selected for sharing.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    boolean success = controller.shareToDoWithUsers(currentToDo, selectedUsernames, controller.user.getUsername());
                    if (success) {
                        JOptionPane.showMessageDialog(frameToDoForm, "ToDo shared successfully with selected users.");
                        populateMembersComboBox();
                    } else {
                        JOptionPane.showMessageDialog(frameToDoForm, "Failed to share ToDo.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        changeSharing.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentToDo == null || !controller.isCurrentUserToDoCreator(currentToDo)) {
                    JOptionPane.showMessageDialog(frameToDoForm, "Only the creator can manage sharing for this ToDo.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Set<String> sharedUsernames = new HashSet<>();
                for(User u : currentToDo.getUsers()){
                    sharedUsernames.add(u.getUsername());
                }

                if (sharedUsernames.isEmpty()) {
                    JOptionPane.showMessageDialog(frameToDoForm, "This ToDo is not currently shared with anyone.", "No Shared Users", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String[] sharedUserChoices = sharedUsernames.toArray(new String[0]);
                JList<String> userList = new JList<>(sharedUserChoices);
                userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

                JScrollPane scrollPane = new JScrollPane(userList);
                scrollPane.setPreferredSize(new Dimension(200, 150));

                int option = JOptionPane.showConfirmDialog(frameToDoForm, scrollPane, "Select Users to Remove Sharing From", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (option == JOptionPane.OK_OPTION) {
                    java.util.List<String> selectedUsernames = userList.getSelectedValuesList();
                    if (selectedUsernames.isEmpty()) {
                        JOptionPane.showMessageDialog(frameToDoForm, "No users selected to remove sharing.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    boolean success = controller.removeToDoSharing(currentToDo, selectedUsernames);
                    if (success) {
                        JOptionPane.showMessageDialog(frameToDoForm, "Sharing revoked successfully for selected users.");
                        populateMembersComboBox();
                    } else {
                        JOptionPane.showMessageDialog(frameToDoForm, "Failed to revoke sharing.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        membersToDo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
    }

    private void populateMembersComboBox() {
        membersToDo.removeAllItems();
        if (currentToDo != null && currentToDo.getUsers() != null) {
            if (currentToDo.getUsers().isEmpty()) {
                membersToDo.addItem("No members shared with");
            } else {
                for (User user : currentToDo.getUsers()) {
                    membersToDo.addItem(user.getUsername());
                }
            }
        } else {
            membersToDo.addItem("Not applicable");
        }
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
            currentToDo.setStatus(statusField.getText());
        }
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

    private BoardName getBoardNameFromString(String boardName) {
        String formattedBoardName = boardName.toUpperCase();
        if (formattedBoardName.equals("FREE TIME")) {
            formattedBoardName = "FREE_TIME";
        }
        return BoardName.valueOf(formattedBoardName);
    }
}