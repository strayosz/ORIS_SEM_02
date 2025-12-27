package org.example.ui;

import org.example.entities.PlayerDTO;
import org.example.repositories.PlayerRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ScorePanel extends JPanel {

    private final PlayerRepository repository = new PlayerRepository();
    private DefaultTableModel model;

    public ScorePanel(PlayerFrame frame) {
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
        try {
            players = repository.getAll();
            model.setRowCount(0);
            for (int i = 0; i < players.size(); i++) {
                PlayerDTO p = players.get(i);
                model.addRow(new Object[]{i+1, p.name(), p.score()});
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}