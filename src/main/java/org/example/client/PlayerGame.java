package org.example.client;

import org.example.actions.MoveAction;
import org.example.entities.Player;
import org.example.entities.PlayerDTO;
import org.example.entities.Tile;
import org.example.enums.DIRECTION;
import org.example.enums.MESSAGETYPE;
import org.example.ui.PlayerFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class PlayerGame extends JPanel {

    Logger logger = LoggerFactory.getLogger(PlayerGame.class);

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 50000;
    private static final int BUFFER_SIZE = 4096;

    private DatagramSocket socket;

    private InetAddress serverAddress;

    private final PlayerFrame frame;
    private final int boardWidth;
    private final int boardHeight;
    private final int tileSize;
    private final List<Player> players;
    private int myPlayerId;
    private boolean gameOverHandled = false;
    private Player myPlayer = new Player();

    public PlayerGame(PlayerFrame frame, int boardWidth, int boardHeight, int tileSize) {
        this.frame = frame;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.tileSize = tileSize;
        this.players = new ArrayList<>();
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        //setFocusable(true);
    }

    public void start() {
        try {

            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            gameOverHandled = false;
            setupKeyBindings();
            readDataThread();

            DatagramPacket sendPacket = getDatagramPacket();
            socket.send(sendPacket);
        } catch (IOException e) {
            System.err.println("Ошибка клиента: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            dos.writeByte(MESSAGETYPE.DISCONNECT.getCode());
            dos.writeInt(myPlayerId);

            DatagramPacket packet = new DatagramPacket(
                    bos.toByteArray(),
                    bos.size(),
                    serverAddress,
                    SERVER_PORT
            );

            socket.send(packet);
            socket.close();

        } catch (IOException e) {
            logger.warn("Ошибка при отключении", e);
        }
    }

    public List<PlayerDTO> getScores() {
        try {
            List<PlayerDTO> players = new ArrayList<>();
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(SERVER_ADDRESS);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            dos.writeByte(MESSAGETYPE.SCORE.getCode());

            DatagramPacket packet = new DatagramPacket(
                    bos.toByteArray(),
                    bos.size(),
                    serverAddress,
                    SERVER_PORT
            );

            socket.send(packet);

            while (true) {
                byte[] receiveData = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(receivePacket.getData(), 0, receivePacket.getLength()));

                byte msgType = dis.readByte();

                if (MESSAGETYPE.SCORE.getCode() == msgType) {
                    int rows = dis.readInt();
                    for (int i = 0; i < rows; i++) {
                        int id = dis.readInt();

                        byte[] data = new byte[dis.readInt()];
                        dis.readFully(data);
                        String name = new String(data, StandardCharsets.UTF_8);
                        int score = dis.readInt();

                        players.add(new PlayerDTO(id, name, score));
                    }
                    break;
                }
            }
            return players;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void readDataThread() {
        new Thread(() -> {
            try {
                while (true) {
                    byte[] receiveData = new byte[BUFFER_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);

                    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(receivePacket.getData(), 0, receivePacket.getLength()));

                    byte msgType = dis.readByte();

                    if (MESSAGETYPE.CONNECT.getCode() == msgType) {
                        myPlayerId = dis.readInt();
                    } else if (MESSAGETYPE.UPDATE.getCode() == msgType) {
                        readData(dis);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

    private void readData(DataInputStream dis) throws IOException {
        int playerCount = dis.readInt();
        List<Player> newPlayers = new ArrayList<>();
        for (int count = 0; count < playerCount; count++) {
            int playerId = dis.readInt();

            int lenAddr = dis.readInt();
            byte[] addr = new byte[lenAddr];
            dis.readFully(addr);

            InetAddress address = InetAddress.getByAddress(addr);
            int port = dis.readInt();

            int headX = dis.readInt();
            int headY = dis.readInt();

            Tile head = new Tile(headX, headY);

            DIRECTION dir = DIRECTION.values()[dis.readByte()];

            int ownedCount = dis.readInt();
            Set<Tile> owned = new HashSet<>();
            for (int i = 0; i < ownedCount; i++) {
                owned.add(new Tile(dis.readInt(), dis.readInt()));
            }

            int tailedCount = dis.readInt();
            Set<Tile> tailed = new HashSet<>();
            for (int i = 0; i < tailedCount; i++) {
                tailed.add(new Tile(dis.readInt(), dis.readInt()));
            }

            Color color = new Color(dis.readInt());

            boolean active = dis.readBoolean();

            Player player = new Player(address, port, playerId, head, owned, tailed, dir, color, active);
            newPlayers.add(player);

            if (myPlayerId == player.getId()) {
                myPlayer = player;
                if (!myPlayer.isActive() && !gameOverHandled) {
                    gameOverHandled = true;
                    SwingUtilities.invokeLater(frame::showStart);
                    break;
                }
            }

        }
        SwingUtilities.invokeLater(() -> {
            players.clear();
            players.addAll(newPlayers);
            repaint();
        });
    }

    private DatagramPacket getDatagramPacket() throws IOException {
        byte[] data = myPlayer.getName().getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(MESSAGETYPE.CONNECT.getCode());
        dos.writeInt(data.length);
        dos.write(data);

        return new DatagramPacket(
                bos.toByteArray(),
                bos.size(),
                serverAddress,
                SERVER_PORT
        );
    }


    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        draw(g2d);
    }

    public void draw(Graphics2D g2d) {
        int columns = boardWidth / tileSize;
        int rows = boardHeight / tileSize;

        for (int i = 0; i <= columns; i++) {
            int x = i * tileSize;
            g2d.drawLine(x, 0, x, boardHeight);
        }

        for (int j = 0; j <= rows; j++) {
            int y = j * tileSize;
            g2d.drawLine(0, y, boardWidth, y);
        }
        for (Player player : players) {
            if (!player.isActive()) {
                continue;
            }
            Set<Tile> owned = player.getOwned();
            Set<Tile> tailed = player.getTailed();
            Color playerColor = player.getColor();
            for (Tile tile : owned) {
                g2d.setColor(playerColor.darker());
                g2d.fillRect(tile.getX() * tileSize, tile.getY() * tileSize, tileSize, tileSize);
            }
            for (Tile tile : tailed) {
                g2d.setColor(playerColor.brighter());
                g2d.fillRect(tile.getX() * tileSize, tile.getY() * tileSize, tileSize, tileSize);
            }
            g2d.setColor(playerColor);
            g2d.fillRect(player.getHead().getX() * tileSize, player.getHead().getY() * tileSize, tileSize, tileSize);
        }

        AlphaComposite originalComposite = (AlphaComposite) g2d.getComposite();

        AlphaComposite translucentComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        g2d.setComposite(translucentComposite);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString(myPlayer.getOwned().size() + "", boardWidth - 75, 50);

        g2d.setComposite(originalComposite);

        g2d.setColor(Color.BLACK);
        for (int i = 0; i < columns; i++) {
            int x = i * tileSize;
            g2d.fillRect(x, 0, tileSize, tileSize);
            g2d.fillRect(x, (rows - 1) * tileSize, tileSize, tileSize);
        }

        for (int j = 0; j < rows; j++) {
            int y = j * tileSize;
            g2d.fillRect(0, y, tileSize, tileSize);
            g2d.fillRect((columns - 1) * tileSize, y, tileSize, tileSize);
        }
    }

    private void setupKeyBindings() {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("UP"), "up");
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "down");
        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "left");
        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "right");

        actionMap.put("up", new MoveAction(DIRECTION.UP, socket, serverAddress, SERVER_PORT));
        actionMap.put("down", new MoveAction(DIRECTION.DOWN, socket, serverAddress, SERVER_PORT));
        actionMap.put("left", new MoveAction(DIRECTION.LEFT, socket, serverAddress, SERVER_PORT));
        actionMap.put("right", new MoveAction(DIRECTION.RIGHT, socket, serverAddress, SERVER_PORT));
    }

    public void setPlayerName(String name) {
        myPlayer.setName(name);
    }
}