package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BoardForm {
    private JPanel board;
    private JComboBox comboBox1;
    private JButton addBoardButton;
    private JButton shareBoardButton;
    private JButton addToDoButton;
    private JButton modifyToDoButton;
    private JButton deleteToDoButton;
    private JButton membersButton;
    public JFrame frameBoardForm;

    public BoardForm(JFrame frame){
    frameBoardForm = new JFrame("Personal Area");
    frameBoardForm.setContentPane(board);
    frameBoardForm.pack();
        frameBoardForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addBoardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

    }
}
