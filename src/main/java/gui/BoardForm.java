package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BoardForm {
    private JPanel board;
    private JComboBox comboBoxBoards;
    private JButton addBoardButton;
    private JButton shareBoardButton;
    private JButton addToDoButton;
    private JButton orderToDoButton;
    private JButton deleteToDoButton;
    private JButton dueDateButton;
    private JButton membersButton;
    public JFrame frameBoardForm;

    public BoardForm(JFrame frame){
    frameBoardForm = new JFrame("Personal Area");
    frameBoardForm.setContentPane(board);
    frameBoardForm.pack();
        frameBoardForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.comboBoxBoards.addItem("My boards");
        this.comboBoxBoards.addItem("University");
        this.comboBoxBoards.addItem("Work");
        this.comboBoxBoards.addItem("Free Time");


        addToDoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ToDoForm toDoForm = new ToDoForm(frameBoardForm);
                toDoForm.frameToDoForm.setVisible(true);
            }
        });
    }
}
