package org.example.server;

import org.example.db.DBConnection;
import org.example.entities.Grid;
import org.example.entities.Player;
import org.example.entities.Tile;
import org.example.enums.DIRECTION;
import org.example.enums.MESSAGETYPE;
import org.example.enums.TILESTATUS;
import org.example.repositories.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

    Logger logger = LoggerFactory.getLogger(Server.class);


    private static final int PORT = 50000;
    private static final int BUFFER_SIZE = 4096;
    private DatagramSocket socket;
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private boolean running;
    private final List<Player> players;
    private final Tile[][] grid;
    private final int delay;
    private ScheduledExecutorService gameLoop;
    private final List<Color> colors = new ArrayList<>();
    private final PlayerRepository repository = new PlayerRepository();

    public Server(int gridX, int gridY, int delay) {
        this.delay = delay;
        this.grid = new Grid(gridX, gridY).getGrid();
        this.players = Collections.synchronizedList(new ArrayList<>());
        setColors();

        try {
            socket = new DatagramSocket(PORT);
            running = true;
        } catch (SocketException e) {
            System.exit(1);
        }
    }

    public void start() {
        startNetworkThread();
        startGameLoop();
    }

    private void startNetworkThread() {
        Thread networkThread = new Thread(() -> {
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    processMessage(packet);
                } catch (IOException e) {
                    logger.error("Ошибка сети", e);
                } catch (SQLException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        networkThread.setDaemon(true);
        networkThread.start();
    }

    private void processMessage(DatagramPacket receivePacket) throws IOException, SQLException, ClassNotFoundException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(receivePacket.getData(), 0, receivePacket.getLength()));
        byte msgType = dis.readByte();

        if (MESSAGETYPE.CONNECT.getCode() == msgType) {
            handleConnect(dis, receivePacket);
        } else if(MESSAGETYPE.MOVE.getCode() == msgType) {
            handleMove(dis, receivePacket);
        } else if (MESSAGETYPE.DISCONNECT.getCode() == msgType) {
            handleDisconnect(dis);
        }
    }

    private void handleConnect(DataInputStream dis, DatagramPacket receivePacket) throws IOException, SQLException, ClassNotFoundException {
        int length = dis.readInt();
        byte[] buf = new byte[length];
        dis.readFully(buf, 0, length);
        String name = new String(buf, StandardCharsets.UTF_8);
        int playerId = repository.getNextId();

        Random random = new Random();
        int randX = random.nextInt(2, grid[0].length - 2);
        int randY = random.nextInt(2, grid.length - 2);
        Tile head = grid[randY][randX];
        Set<Tile> owned = new HashSet<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Tile tile = grid[randY + i][randX + j];
                tile.setStatus(TILESTATUS.OWNED);
                tile.setPlayerId(playerId);
                owned.add(tile);
            }
        }

        DIRECTION direction = DIRECTION.values()[random.nextInt(4)];

        Color color = colors.get(new Random().nextInt(colors.size()));

        Player player = new Player(receivePacket.getAddress(), receivePacket.getPort(), playerId, name, head, owned, direction, color);
        players.add(player);
        logger.info("Игрок {} подключился", player);
        sendId(player);
    }

    private void handleMove(DataInputStream dis, DatagramPacket packet) throws IOException {
        DIRECTION dir = DIRECTION.values()[dis.readByte()];
        Player player = findPlayer(packet);
        DIRECTION playerDir = Objects.requireNonNull(player).getDirection();
        if (dir == DIRECTION.DOWN && playerDir != DIRECTION.UP
                || dir == DIRECTION.UP && playerDir != DIRECTION.DOWN
                || dir == DIRECTION.RIGHT && playerDir != DIRECTION.LEFT
                || dir == DIRECTION.LEFT && playerDir != DIRECTION.RIGHT) {
            player.setDirection(dir);
        }
    }

    private void handleDisconnect(DataInputStream dis) throws IOException, SQLException, ClassNotFoundException {
        int playerId = dis.readInt();
        Player player = findPlayer(playerId);

        if (player != null) {
            clearPlayer(player);
            players.remove(player);
            logger.info("Игрок {} отключился", playerId);
        }
    }

    private void startGameLoop() {
        gameLoop = Executors.newSingleThreadScheduledExecutor();
        gameLoop.scheduleAtFixedRate(
                this::tick,
                0,
                delay,
                TimeUnit.MILLISECONDS
        );
    }

    private void tick() {
        try {
            if (!players.isEmpty()) {
                move();
                logic();
                broadcastUpdate();
            }
        } catch (Exception e) {
            logger.error("Ошибка в game loop", e);
        }
    }

    private void move() {
        for (Player player : players) {
            if (!player.isActive()) {
                continue;
            }
            player.setHead(grid[player.getHead().getY() + player.getDirection().getY()][player.getHead().getX() + player.getDirection().getX()]);
        }
    }

    private void logic() throws SQLException, ClassNotFoundException {
        for (Player player : players) {
            if (!player.isActive()) {
                continue;
            }
            Tile head = player.getHead();
            Integer curTilePlayerId = head.getPlayerId();
            int curPlayerId = player.getId();
            TILESTATUS headStatus = head.getStatus();

            //Если игрок наступил на чей-то след
            if (headStatus == TILESTATUS.TAILED) {
                //Если след свой
                if (curTilePlayerId == curPlayerId) {
                    clearPlayer(player);
                    //Если чужой
                } else {
                    clearPlayer(findPlayer(curTilePlayerId));
                    player.getTailed().add(head);
                    head.setStatus(TILESTATUS.TAILED);
                    head.setPlayerId(curPlayerId);
                }
                //Если игрок наступил на свою территорию
            } else if (headStatus == TILESTATUS.OWNED && curTilePlayerId == curPlayerId) {
                for (Tile tile : player.getTailed()) {
                    player.getOwned().add(tile);
                    tile.setStatus(TILESTATUS.OWNED);
                    tile.setPlayerId(curPlayerId);
                    logger.info("player {} получил: {}", curPlayerId, tile);
                }
                player.getTailed().clear();

            } else {
                int x = head.getX();
                int y = head.getY();
                logger.info("xCor: {}, yCor: {}, right: {}, top: {}", x, y, grid[0].length - 1, grid.length - 1);
                //Если игрок врезался в стену
                if (x == 0 || x == grid[0].length - 1 || y == 0 || y == grid.length - 1) {
                    logger.info("мы на границе");
                    clearPlayer(player);
                    //Если игрок наступил на пустую или чьб-то территорию
                } else {
                    player.getTailed().add(head);
                    head.setStatus(TILESTATUS.TAILED);
                    head.setPlayerId(curPlayerId);
                    logger.info("player {} ведет: {}", curPlayerId, head);
                }
            }
        }
    }

    private void broadcastUpdate() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        dos.writeByte(MESSAGETYPE.UPDATE.getCode());
        dos.writeInt(players.size());
        for (Player p : players) {
            writePlayer(dos, p);
        }

        byte[] data = bos.toByteArray();
        int length = bos.size();

        for (Player player : players) {
            DatagramPacket packet = new DatagramPacket(
                    data,
                    length,
                    player.getAddress(),
                    player.getPort()
            );

            socket.send(packet);
        }
    }

    private void writePlayer(DataOutputStream dos, Player player) throws IOException {
        dos.writeInt(player.getId());

        byte[] addr = player.getAddress().getAddress();
        dos.writeInt(addr.length);
        dos.write(addr);

        dos.writeInt(player.getPort());

        Tile head = player.getHead();
        dos.writeInt(head.getX());
        dos.writeInt(head.getY());

        dos.writeByte(player.getDirection().ordinal());

        Set<Tile> owned = player.getOwned();
        dos.writeInt(owned.size());
        for (Tile t : owned) {
            dos.writeInt(t.getX());
            dos.writeInt(t.getY());
        }

        Set<Tile> tailed = player.getTailed();
        dos.writeInt(tailed.size());
        for (Tile t : tailed) {
            dos.writeInt(t.getX());
            dos.writeInt(t.getY());
        }

        dos.writeInt(player.getColor().getRGB());

        dos.writeBoolean(player.isActive());
    }


    private void clearPlayer(Player player) throws SQLException, ClassNotFoundException {
        repository.addPlayer(player);

        for (Tile tile : player.getTailed()) {
            tile.setStatus(TILESTATUS.EMPTY);
            tile.setPlayerId(null);
        }

        for (Tile tile : player.getOwned()) {
            tile.setStatus(TILESTATUS.EMPTY);
            tile.setPlayerId(null);
        }

        player.getTailed().clear();
        player.getOwned().clear();
        player.setActive(false);
    }

    private Player findPlayer(DatagramPacket packet) {
        InetAddress address = packet.getAddress();
        int port = packet.getPort();

        for (Player player : players) {
            if (player.getAddress().equals(address) && player.getPort() == port) {
                return player;
            }
        }
        return null;
    }

    private Player findPlayer(int playerId) {
        for (Player player : players) {
            if (player.getId() == playerId) {
                return player;
            }
        }
        return null;
    }

    private void sendId(Player player) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        dos.writeByte(MESSAGETYPE.CONNECT.getCode());
        dos.writeInt(player.getId());

        byte[] data = bos.toByteArray();
        int length = bos.size();

        DatagramPacket packet = new DatagramPacket(
                data,
                length,
                player.getAddress(),
                player.getPort()
        );

        socket.send(packet);

    }

    private void setColors() {
        colors.add(Color.YELLOW);
        colors.add(Color.PINK);
        colors.add(Color.ORANGE);
    }
}
