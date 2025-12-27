package org.example.ui;

import org.example.client.PlayerGame;
import org.example.actions.CloseAction;

import javax.swing.*;
import java.awt.*;

public class PlayerFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel cards;
    private final PlayerStartPanel startPanel;
    private final ScorePanel scorePanel;
    private final PlayerGame gamePanel;

    public PlayerFrame(int boardWidth, int boardHeight, int tileSize) {
        setTitle("paper.io");
        setVisible(true);
        setSize(boardWidth, boardHeight);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new CloseAction(this));

        startPanel = new PlayerStartPanel(this);
        scorePanel = new ScorePanel(this);
        gamePanel = new PlayerGame(this, boardWidth, boardHeight, tileSize);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        cards.add(startPanel, "START");
        cards.add(scorePanel, "SCORE");
        cards.add(gamePanel, "GAME");

        add(cards);
        showStart();
        setVisible(true);
        pack();

    }

    public void showStart() {
        scorePanel.updateData();
        cardLayout.show(cards, "START");
    }

    public void showScore() {
        cardLayout.show(cards, "SCORE");
    }

    public void startGame(String playerName) {
        cardLayout.show(cards, "GAME");
        gamePanel.setPlayerName(playerName);
        gamePanel.start();
    }

    public PlayerGame getGamePanel() {
        return gamePanel;
    }
}
