package gui;

import controller.Controller;

import javax.swing.*;


public class ModifyToDoForm {
    public JFrame frameModifyToDoForm, frame;
    private String currentBoard;
    private Controller controller;
    private JPanel principal;
    private JLabel labelTitle;
    private JTextField textFieldTitle;
    private JLabel labelDescription;
    private JTextField textFieldDescription;
    private JLabel labeDueDate;
    private JTextField textFieldDueDate;
    private JLabel labelUrl;
    private JTextField textFieldUrl;
    private JButton buttonSave;

    public ModifyToDoForm(JFrame parent, Controller c, String cu){
        this.currentBoard = cu;
        this.frame = parent;
        this.controller = c;

        frameModifyToDoForm = new JFrame("ToDo Modify");
        frameModifyToDoForm.setContentPane(principal);
        frameModifyToDoForm.pack();
        
    }
}
