package sampleclients.room_heuristics;

import sampleclients.BasicObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Map {
    char[][] character_map;
    int width;
    int height;

    Map(List<List<BasicObject>> gameboard) {
        Size size = GetSize(gameboard);
        this.width = size.width;
        this.height = size.height;
        System.err.println("Size: " + size.width + ", " + size.height);
        this.character_map = new char[this.height][this.width];

        for (int y = 0; y < gameboard.size(); ++y) {
            for (int x = 0; x < gameboard.get(y).size(); ++x) {
                if (gameboard.get(y).get(x) != null) {
                    this.character_map[y][x] = gameboard.get(y).get(x).getID();
                } else {
                    this.character_map[y][x] = ' ';
                }
            }
            System.err.println();
        }
    }

    private Size GetSize(List<List<BasicObject>> gameboard) {
        int width = 0;

        for (List<BasicObject> row : gameboard) {
            if (row.size() > width) width = row.size();
        }
        int height = gameboard.size();

        return new Size(width, height);
    }

    public boolean isWall(int x, int y) {
        return this.character_map[y][x] == '+';
    }

    public boolean isEmpty(int x, int y) {
        return !this.isWall(x, y);
    }
}
