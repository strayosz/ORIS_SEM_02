package org.example.ui;

import javax.swing.*;
import java.awt.*;

public class PlayerStartPanel extends JPanel {
    private final JButton startButton;
    private final JButton scoreButton;
    private final JTextField nameField;

    public PlayerStartPanel(PlayerFrame frame) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Box.createVerticalGlue());
        JLabel nameLabel = new JLabel("Имя игрока");
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(200, 30));
        nameField.setHorizontalAlignment(JTextField.CENTER);

        startButton = new JButton("Начать игру");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(200, 1000));

        scoreButton = new JButton("Таблица рекордов");
        scoreButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreButton.setMaximumSize(new Dimension(200, 1000));

        add(nameLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(nameField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(startButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(scoreButton);

        add(Box.createVerticalGlue());

        startButton.addActionListener(o -> {
            String playerName = nameField.getText().trim();

            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Введите имя игрока",
                        "Ошибка",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            } else if(playerName.length() > 50){
                JOptionPane.showMessageDialog(
                        this,
                        "Имя игрока не может быть длиннее 50 символов",
                        "Ошибка",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            frame.startGame(playerName);
        });

        scoreButton.addActionListener(o -> frame.showScore());
    }

}