package org.example.ui;

import org.example.entities.PlayerDTO;
import org.example.repositories.PlayerRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ScorePanel extends JPanel {

    private DefaultTableModel model;
    private final PlayerFrame frame;

    public ScorePanel(PlayerFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        createTable();
        JButton backButton = new JButton("Назад");

        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        add(backButton, BorderLayout.SOUTH);

        backButton.addActionListener(o -> frame.showStart());
    }

    private void createTable() {
        String[] columns = {"Позиция", "Игрок", "Очки"};
        this.model = new DefaultTableModel(null, columns);
    }

    public void updateData() {
        List<PlayerDTO> players = null;
        players = frame.getGamePanel().getScores();
        model.setRowCount(0);
        for (int i = 0; i < players.size(); i++) {
            PlayerDTO p = players.get(i);
            model.addRow(new Object[]{i + 1, p.name(), p.score()});
        }
    }
}