package org.example.entities;

public class PlayerDTO {
    private final int id;
    private final String name;
    private final int score;

    public PlayerDTO(int id, String name, int score) {
        this.id = id;
        this.name = name;
        this.score = score;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }
}
