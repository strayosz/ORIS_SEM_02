package org.example.entities;

import org.example.enums.DIRECTION;

import java.awt.*;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Player {
    private InetAddress address;
    private int port;

    private int id;
    private String name;
    private Tile head;
    private Set<Tile> owned = new HashSet<>();
    private Set<Tile> tailed = new HashSet<>();
    private DIRECTION direction;
    private Color color;
    private boolean active = true;

    public Player() {
    }

    public Player(InetAddress address, int port, int id, String name, Tile head, Set<Tile> owned, DIRECTION direction, Color color) {
        this.address = address;
        this.port = port;
        this.id = id;
        this.name = name;
        this.head = head;
        this.owned = owned;
        this.direction = direction;
        this.color = color;

    }

    public Player(InetAddress address, int port, int id, Tile head, Set<Tile> owned, Set<Tile> tailed, DIRECTION direction, Color color, boolean active){
        this.address = address;
        this.port = port;
        this.id = id;
        this.head = head;
        this.owned = owned;
        this.tailed = tailed;
        this.direction = direction;
        this.color = color;
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tile getHead() {
        return head;
    }

    public void setHead(Tile head) {
        this.head = head;
    }

    public Set<Tile> getOwned() {
        return owned;
    }

    public void setOwned(Set<Tile> owned) {
        this.owned = owned;
    }

    public Set<Tile> getTailed() {
        return tailed;
    }

    public void setTailed(Set<Tile> tailed) {
        this.tailed = tailed;
    }

    public DIRECTION getDirection() {
        return direction;
    }

    public void setDirection(DIRECTION direction) {
        this.direction = direction;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return port == player.port && id == player.id && Objects.equals(address, player.address)
                && Objects.equals(name, player.name) && Objects.equals(head, player.head)
                && Objects.equals(owned, player.owned) && Objects.equals(tailed, player.tailed)
                && direction == player.direction && Objects.equals(color, player.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port, id, name, head, owned, tailed, direction, color);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", head=" + head +
                ", owned=" + owned +
                ", tailed=" + tailed +
                ", direction=" + direction +
                ", color=" + color +
                '}';
    }


}
