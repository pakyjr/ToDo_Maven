package gui;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ToDoForm {
    private JPanel todoPanel;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JButton shareButton;
    private JButton changeShareButton;
    private JComboBox comboBox1;
    private JTextField textField5;
    private JCheckBox londraCheckBox;
    private JCheckBox milanoCheckBox;
    private JButton buttonSave;
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
    }
}
