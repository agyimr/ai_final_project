package sampleclients;

import java.util.*;

public class AnticipationPlanning {

    private Set[][] board;

    private int clock = 0;

    private int width;
    private int height;

    public AnticipationPlanning(MainBoard mainBoard) {

        this.width = mainBoard.getWidth();
        this.height = mainBoard.getHeight();

        this.board = new Set[height][width];

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                this.board[y][x] = new HashSet();
            }
        }

    }

    private void cleanup() {



    }

    public int getClock() {
        return this.clock;
    }

    public void incrementClock() {
        this.clock++;
    }

    public Agent addPath(LinkedList<Node> path, Agent agent) {

        if(path == null) {
            return null;
        }

        Iterator it = path.iterator();

        int localClock = getClock();

        int oldX = agent.getX();
        int oldY = agent.getY();

        Agent conflictAgent = null;

        while(it.hasNext()) {

            localClock++;

            Node node = (Node) it.next();

            conflictAgent = wouldBeInConflict(node.agentX, node.agentY, oldX, oldY, localClock);

            if (conflictAgent != null) {
                System.err.println("There will be an agent-agent conflict at cell " + node.agentX + "," + node.agentY);
                System.err.println("Conflict with agent : " + conflictAgent);
                System.err.println(conflictAgent);
                System.err.flush();
                System.exit(1);
                return conflictAgent;
            }

            if (node.boxY != -1 && node.boxX != -1) {

                conflictAgent = wouldBeInConflict(node.boxX, node.boxY, oldX, oldY, localClock);

                if (conflictAgent != null) {
                    System.err.println("There will be an agent-box conflict at cell " + node.boxX + "," + node.boxY);
                    System.err.println("Conflict with agent : " + conflictAgent);
                    System.err.flush();
                    System.exit(2);
                    return conflictAgent;
                }

            }

            oldX = node.agentX;
            oldY = node.agentY;

        }


        it = path.iterator();

        localClock = getClock();

        while(it.hasNext()) {

            localClock++;

            Node node = (Node) it.next();

            board[node.agentY][node.agentX].add(new Booking(agent, localClock));

            if (node.boxY != -1 && node.boxX != -1) {

                board[node.boxY][node.boxX].add(new Booking(agent, localClock));

            }
        }

        return null;

    }

    public void displayBoard() {

        String board = "";

        for(int y = 0; y < height; ++y) {

            for(int x = 0; x < width; ++x) {

                Iterator it = this.board[y][x].iterator();

                while(it.hasNext()) {
                    int instant = (int) it.next();
                    board += instant + " ";
                }

                board += ";";

            }

            board += "$";

        }

        System.err.println(board);

    }

    public Agent wouldBeInConflict(int x, int y, int oldX, int oldY, int instant) {

        if(boardContains(x, y, instant)) {
            return boardFindAgent(x, y, instant);
        } else if(boardContains(x, y, instant-1) && boardContains(oldX, oldY, instant)) {
            return boardFindAgent(x, y, instant-1);
        }
        /* else if(boardContains(x, y, instant-1)){ //if target was previously occuped

            if(x+1 < width && oldX == x+1 && boardContains(x+1, y, instant)) {
                return boardFindAgent(x, y, instant-1);
            }
            if(x-1 >= 0 && oldX == x-1 && boardContains(x-1, y, instant)) {
                return boardFindAgent(x, y, instant-1);
            }
            if(y+1 < height && oldY == y+1 && boardContains(x, y+1, instant)) {
                return boardFindAgent(x, y, instant-1);
            }
            if(y-1 >= 0 && oldY == y-1 && boardContains(x, y-1, instant)) {
                return boardFindAgent(x, y, instant-1);
            }

        }
        */

        return null;
    }

    private boolean boardContains(int x, int y, int instant) {
        return board[y][x].contains(new Booking(null, instant));
    }

    private Agent boardFindAgent(int x, int y, int instant) {

        Iterator it = board[y][x].iterator();

        while(it.hasNext()) {
            Booking booking = (Booking) it.next();

            if(booking.getInstant() == instant) {
                return booking.getAgent();
            }
        }
        return null;
    }

    private class Booking {

        private Agent agent;
        private int instant;

        public Booking(Agent agent, int instant) {
            this.agent = agent;
            this.instant = instant;
        }

        public int getInstant() {
            return instant;
        }

        public Agent getAgent() {
            return agent;
        }

        @Override
        public int hashCode() {
            return instant;
        }

        @Override
        public boolean equals(Object obj) {
            return instant == ((Booking) obj).getInstant();
        }
    }

}
