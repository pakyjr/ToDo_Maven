package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ToDoForm {
    private JPanel todoPanel;
    private JTextField nameField;
    private JTextField DescriptionField;
    private JTextField dueDateField;
    private JTextField urlField;
    private JButton shareButton;
    private JButton changeShareButton;
    private JComboBox members;
    private JTextField statusField;
    private JButton buttonSave;
    private JPanel panelActivity;
    private JLabel image;
    public JFrame frameToDoForm, frame;

    public ToDoForm(JFrame parent){
        frame = parent;

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
                //creaiamo un nuovo oggetto toDo da salvare nel database
                frame.setVisible(true);
                frameToDoForm.setVisible(false);
                frameToDoForm.dispose();
            }
        });
    }
}
