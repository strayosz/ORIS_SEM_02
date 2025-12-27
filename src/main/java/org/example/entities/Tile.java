package org.example.entities;

import org.example.enums.TILESTATUS;

import java.util.Objects;

public class Tile {
    private final int x;
    private final int y;
    private Integer playerId;
    private TILESTATUS status;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
        this.status = TILESTATUS.EMPTY;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public TILESTATUS getStatus() {
        return status;
    }

    public void setStatus(TILESTATUS status) {
        this.status = status;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return x == tile.x && y == tile.y && Objects.equals(playerId, tile.playerId) && status == tile.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, playerId, status);
    }
}
