package sampleclients;

import java.util.*;

public class GlobalPlanningBoard {

    private Set[][] board;

    private int clock = 0;

    private int width;
    private int height;

    public GlobalPlanningBoard(int width, int height) {

        this.width = width;
        this.height = height;

        this.board = new Set[height][width];

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                this.board[y][x] = new HashSet();
            }
        }

    }

    public int getClock() {
        return this.clock;
    }

    public void incrementClock() {
        this.clock++;
    }

    private String getDirectionFromMove(int ox, int oy, int dx, int dy) {

        if(oy == dy) {
            return dx < ox ? "W" : "E";
        } else {
            return dy < oy ? "N" : "S";
        }

    }

    public void addPath(LinkedList<Node> path, int ox, int oy) {

        if(path == null) {
            return;
        }

        Iterator it = path.iterator();

        int offset = 0;

        while(it.hasNext()){

            offset++;

            Node node = (Node) it.next();

            String direction = getDirectionFromMove(ox, oy, node.getX(), node.getY());

            if(wouldBeInConflict(node.getX(), node.getY(), clock + offset)) {
                System.err.println("This message must never happen : CRAAASH at " + node.getX() + " " + node.getY());

            }
            //if(this.board[node.getY()][node.getX()].contains(clock + offset)) {
            //}

            this.board[node.getY()][node.getX()].add(direction + (clock + offset));

            ox = node.getX();
            oy = node.getY();
        }

        System.err.println("Inline display global planning board");
        displayBoard();

    }

    public void displayBoard() {

        String board = "";

        for(int y = 0; y < height; ++y) {

            for(int x = 0; x < width; ++x) {
                ;

                Iterator it = this.board[y][x].iterator();

                while(it.hasNext()) {
                    String instant = (String) it.next();
                    board += instant + " ";
                }

                board += ";";

            }

            board += "N";

        }

        System.err.println(board);

    }

    public boolean wouldBeInConflict(int x, int y, int instant) {

        boolean conflictC = this.board[y][x].contains("N" + instant)
                || this.board[y][x].contains("S" + instant)
                || this.board[y][x].contains("E" + instant)
                || this.board[y][x].contains("W" + instant);

        boolean conflictN = (y > 0) ? this.board[y-1][x].contains("S" + instant) : false;
        boolean conflictS = (y < height - 1) ? this.board[y+1][x].contains("N" + instant) : false;

        boolean conflictE = (x < width - 1) ? this.board[y][x+1].contains("W" + instant) : false;
        boolean conflictW = (x > 0) ? this.board[y][x-1].contains("E" + instant) : false;

        return conflictC || conflictN || conflictS || conflictE || conflictW;
    }

    // TODO : agent trace when box-attached

    // TODO : NoOP

    // FIX : Agent 0, stop in front of B
}
