package gui;

import javax.swing.*;

public class BoardForm {
    private JPanel board;
    private JComboBox comboBox1;
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


    }
}
