package org.example.entities;

public class Grid {
    private final Tile[][] grid;

    public Grid(int width, int height){
        grid = new Tile[height][width];
        System.out.println(grid[0].length);
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                grid[y][x] = new Tile(x, y);
            }
        }
    }

    public Tile[][] getGrid() {
        return grid;
    }
}
