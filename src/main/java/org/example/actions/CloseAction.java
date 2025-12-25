package org.example.actions;

import org.example.ui.PlayerFrame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CloseAction extends WindowAdapter {
    private final PlayerFrame frame;

    public CloseAction(PlayerFrame frame) {
        this.frame = frame;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        frame.getGamePanel().disconnect();
        frame.dispose();
        System.exit(0);
    }
}
