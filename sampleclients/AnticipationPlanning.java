package sampleclients;

import java.util.*;

public class AnticipationPlanning {

    private HashMap<Cell, HashMap<Booking, Booking>> board;

    private int clock = 0;

    private HashMap<Integer, HashSet<Cell>> cleanupBins;

    static private int width;
    private int height;

    public AnticipationPlanning() {

        this.width = 50;
        this.height = 50;

        this.initliaze();
    }

    public AnticipationPlanning(MainBoard mainBoard) {

        this.width = mainBoard.getWidth();
        this.height = mainBoard.getHeight();

        this.initliaze();
    }

    private void initliaze() {

        this.board = new HashMap<Cell, HashMap<Booking, Booking>>();

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                this.board.put(new Cell(x, y), new HashMap<Booking, Booking>());
            }
        }

        this.cleanupBins = new HashMap<Integer, HashSet<Cell>>();

    }

    private HashMap<Booking, Booking> getBoardCell(int x, int y) {
        return this.board.get(new Cell(x, y));
    }

    private void cleanup() {

        if(!this.cleanupBins.containsKey(this.clock)) {
            return;
        }

        Iterator binIterator = cleanupBins.get(this.clock).iterator();

        while(binIterator.hasNext()) {

            Cell cell = (Cell) binIterator.next();

            this.getBoardCell(cell.getX(), cell.getY()).remove(new Booking(null, this.clock));

        }

        this.cleanupBins.remove(this.clock);

    }

    public int getClock() {
        return this.clock;
    }

    public void incrementClock() {

        this.cleanup();

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
                //System.err.flush();
                //System.exit(1);
                return conflictAgent;
            }

            if (node.boxY != -1 && node.boxX != -1) {

                conflictAgent = wouldBeInConflict(node.boxX, node.boxY, oldX, oldY, localClock);

                if (conflictAgent != null) {
                    System.err.println("There will be an agent-box conflict at cell " + node.boxX + "," + node.boxY);
                    System.err.println("Conflict with agent : " + conflictAgent);
                    //System.err.flush();
                    //System.exit(2);
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

            bookCell(node.agentX, node.agentY, agent, localClock);

            if (node.boxY != -1 && node.boxX != -1) {

                bookCell(node.boxX, node.boxY, agent, localClock);

            }
        }

        return null;

    }

    private class Cell {

        private int x;
        private int y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            return y * AnticipationPlanning.width + x;
        }

        public boolean is(int x, int y) {
            return this.x == x && this.y == y;
        }

        @Override
        public boolean equals(Object obj) {
            return ((Cell) obj).is(x, y);
        }

        public int getY() {
            return y;
        }

        public int getX() {
            return x;
        }
    }

    public void bookCell(int x, int y, Agent agent, int clock) {

        this.getBoardCell(x, y).put(new Booking(agent, clock), new Booking(agent, clock));

        if(!this.cleanupBins.containsKey(clock)) {
            this.cleanupBins.put(clock, new HashSet<Cell>());
        }

        this.cleanupBins.get(clock).add(new Cell(x, y));
    }

    public void displayBoard() {

        String board = "";

        for(int y = 0; y < height; ++y) {

            for(int x = 0; x < width; ++x) {

                Iterator it = this.getBoardCell(x, y).keySet().iterator();

                while(it.hasNext()) {
                    Booking booking = (Booking) it.next();
                    board += booking.getInstant() + " ";
                }

                board += ";";

            }

            board += "$";

        }

        System.err.println(board);

    }

    public Agent wouldBeInConflict(int x, int y, int oldX, int oldY, int instant) {

        if(boardContains(x, y, instant) != null) {
            return boardFindAgent(x, y, instant);
        } else {

            Booking destinationCellCurrentBooking = boardContains(x, y, instant-1);
            Booking sourceCellFuturBooking = boardContains(oldX, oldY, instant);

            if(destinationCellCurrentBooking != null && sourceCellFuturBooking != null && destinationCellCurrentBooking.getAgent() == sourceCellFuturBooking.getAgent()) {
                return sourceCellFuturBooking.getAgent();
            }

        }

        return null;
    }

    public boolean isConflicting(Node node, int instant) {

        int oldAgentX = node.agentX;
        int oldAgentY = node.agentY;

        int newAgentX = node.agentX;
        int newAgentY = node.agentY;

        if(node.action.dir1 == Command.dir.N) {
            newAgentY -= 1;
        } else if(node.action.dir1 == Command.dir.S) {
            newAgentY += 1;
        } else if(node.action.dir1 == Command.dir.E) {
            newAgentX += 1;
        } else if(node.action.dir1 == Command.dir.W) {
            newAgentX -= 1;
        }

        if(boardContains(newAgentX, newAgentY, instant) != null) {
            return true;
        } else {

            Booking destinationAgentCellCurrentBooking = boardContains(newAgentX, newAgentY, instant-1);
            Booking sourceAgentCellFuturBooking = boardContains(oldAgentX, oldAgentY, instant);

            if(destinationAgentCellCurrentBooking != null && sourceAgentCellFuturBooking != null && destinationAgentCellCurrentBooking.getAgent() == sourceAgentCellFuturBooking.getAgent()) {
                return true;
            }
        }

        int oldBoxX = node.boxX;
        int oldBoxY = node.boxY;

        if(oldBoxX != -1 && oldBoxY != -1) {

            int newBoxX = node.boxX;
            int newBoxY = node.boxY;

            if (node.action.dir2 == Command.dir.N) {
                newBoxY -= 1;
            } else if (node.action.dir2 == Command.dir.S) {
                newBoxY += 1;
            } else if (node.action.dir2 == Command.dir.E) {
                newBoxX += 1;
            } else if (node.action.dir2 == Command.dir.W) {
                newBoxX -= 1;
            }

            if(boardContains(newBoxX, newBoxY, instant) != null) {
                return true;
            } else {

                Booking destinationBoxCellCurrentBooking = boardContains(newBoxX, newBoxY, instant-1);
                Booking sourceCellBoxFuturBooking = boardContains(oldBoxX, oldBoxY, instant);

                if(destinationBoxCellCurrentBooking != null && sourceCellBoxFuturBooking != null && destinationBoxCellCurrentBooking.getAgent() == sourceCellBoxFuturBooking.getAgent()) {
                    return true;
                }
            }
        }

        return false;
    }

    private Booking boardContains(int x, int y, int instant) {
        return this.getBoardCell(x, y).get(new Booking(null, instant));
    }

    private Agent boardFindAgent(int x, int y, int instant) {

        Iterator it = this.getBoardCell(x, y).keySet().iterator();

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