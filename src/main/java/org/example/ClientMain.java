package org.example;

import org.example.ui.PlayerFrame;


public class ClientMain {
    public static void main(String[] args) {
        int boardWidth = 800;
        int boardHeight = 800;
        int tileSize = 16;

        new PlayerFrame(boardWidth, boardHeight, tileSize);

    }
}
