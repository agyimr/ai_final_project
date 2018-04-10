import java.awt.*;
import java.io.*;
import java.util.*;

public class Map {
    char[][] character_map;
    int width;
    int height;

    Map(String level_name) {
        Size size = GetSize(level_name);
        this.width = size.width;
        this.height = size.height;
        this.character_map = new char[this.height][this.width];

        try {

            // reading map line by line
            BufferedReader bufferreader = new BufferedReader(new FileReader("C:\\Egyetem\\Msc\\2. félév\\room_heuristics\\levels\\" + level_name + ".lvl"));
            String line = bufferreader.readLine();
            while(!line.contains("+")) {
                line = bufferreader.readLine();
            }

            int row = 0;

            // processing each line
            while (line != null) {

                // each character
                for (int col = 0; col < line.length(); col++) {
                    this.character_map[row][col] = line.charAt(col);
                }
                row++;
                line = bufferreader.readLine();
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private Size GetSize(String level_name) {
        int row = 0;
        int col_length = 0;

        try {
            // reading map line by line
            BufferedReader bufferreader = new BufferedReader(new FileReader("C:\\Egyetem\\Msc\\2. félév\\room_heuristics\\levels\\" + level_name + ".lvl"));
            String line = bufferreader.readLine();
            while(!line.contains("+")) {
                line = bufferreader.readLine();
            }

            // processing each line
            while (line != null) {
                if (line.length() > col_length) col_length = line.length();
                row++;
                line = bufferreader.readLine();
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return new Size(col_length, row);
    }

    public boolean isWall(int x, int y) {
        return this.character_map[y][x] == '+';
    }

    public boolean isEmpty(int x, int y) {
        return !this.isWall(x, y);
    }
}
