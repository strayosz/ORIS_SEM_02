package org.example.actions;

import org.example.enums.DIRECTION;
import org.example.enums.MESSAGETYPE;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class MoveAction extends AbstractAction {
    private final DIRECTION direction;
    private final DatagramSocket socket;
    private final InetAddress serverAddress;
    private final int port;

    public MoveAction(DIRECTION direction, DatagramSocket socket, InetAddress serverAddress, int port) {
        this.direction = direction;
        this.socket = socket;
        this.serverAddress = serverAddress;
        this.port = port;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(MESSAGETYPE.MOVE.getCode());
            bos.write(direction.ordinal());
            DatagramPacket packet = new DatagramPacket(
                    bos.toByteArray(),
                    bos.size(),
                    serverAddress,
                    port
            );
            socket.send(packet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
