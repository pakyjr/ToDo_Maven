package gui;

import javax.swing.*;

public class BoardForm {
    private JPanel board;
    private JButton addToDoButton;
    private JComboBox comboBox1;
    private JButton membersBoardButton;
    private JButton addBoardButton;
    private JButton shareBoardButton;
    private JButton membersButton;
    private JButton button1;
    public JFrame frameBoardForm;

    public BoardForm(JFrame frame){
    frameBoardForm = new JFrame("Personal Area");
    frameBoardForm.setContentPane(board);
    frameBoardForm.pack();
        frameBoardForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
