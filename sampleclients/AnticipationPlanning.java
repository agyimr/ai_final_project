package sampleclients;

import java.awt.*;
import java.util.*;

public class AnticipationPlanning {

    private HashMap<Cell, TreeMap<Integer, Booking>> board;

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

        this.board = new HashMap<Cell, TreeMap<Integer, Booking>>();

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                this.board.put(new Cell(x, y), new TreeMap<Integer, Booking>());
            }
        }

        this.cleanupBins = new HashMap<Integer, HashSet<Cell>>();

    }

    private TreeMap<Integer, Booking> getBoardCell(int x, int y) {
        return this.board.get(new Cell(x, y));
    }

    private void cleanup() {

        if(!this.cleanupBins.containsKey(this.clock)) {
            return;
        }

        Iterator binIterator = cleanupBins.get(this.clock).iterator();

        while(binIterator.hasNext()) {

            Cell cell = (Cell) binIterator.next();

            this.getBoardCell(cell.getX(), cell.getY()).remove(this.clock);

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
                return conflictAgent;
            }

            bookCell(node.agentX, node.agentY, agent, localClock);

            if (node.boxY != -1 && node.boxX != -1) {

                conflictAgent = wouldBeInConflict(node.boxX, node.boxY, oldX, oldY, localClock);

                if (conflictAgent != null) {
                    System.err.println("There will be an agent-box conflict at cell " + node.boxX + "," + node.boxY);
                    System.err.println("Conflict with agent : " + conflictAgent);
                    return conflictAgent;
                }

                bookCell(node.boxX, node.boxY, agent, localClock);
            }

            oldX = node.agentX;
            oldY = node.agentY;

        }
        /*
        it = path.iterator();

        localClock = getClock();

        while(it.hasNext()) {

            localClock++;

            Node node = (Node) it.next();

            bookCell(node.agentX, node.agentY, agent, localClock);

            if (node.boxY != -1 && node.boxX != -1) {

                bookCell(node.boxX, node.boxY, agent, localClock);

            }
        }*/

        return null;

    }

    public Agent removePath(LinkedList<Node> path, Agent agent, int originalClock) {

        if(path == null) {
            return null;
        }

        Iterator it = path.iterator();

        int localClock = originalClock;

        int oldX = agent.getX();
        int oldY = agent.getY();

        Agent conflictAgent = null;

        while(it.hasNext()) {

            localClock++;

            Node node = (Node) it.next();

            unbookCell(node.agentX, node.agentY, agent, localClock);

            if (node.boxY != -1 && node.boxX != -1) {


                unbookCell(node.boxX, node.boxY, agent, localClock);
            }

            oldX = node.agentX;
            oldY = node.agentY;

        }

        return null;

    }

    private int getEarliestOccupation(Point target) {

        Iterator iterator = this.getBoardCell((int) target.getX(), (int) target.getY()).keySet().iterator();

        if(iterator.hasNext()) {
            return ((Integer) iterator.next());
        } else {
            return -1;
        }

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

    private void bookCell(int x, int y, Agent agent, int clock) {

        this.getBoardCell(x, y).put(clock, new Booking(agent, clock));

        if(!this.cleanupBins.containsKey(clock)) {
            this.cleanupBins.put(clock, new HashSet<Cell>());
        }

        this.cleanupBins.get(clock).add(new Cell(x, y));
    }

    private void unbookCell(int x, int y, Agent agent, int clock) {

        this.getBoardCell(x, y).remove(clock);

        if(this.cleanupBins.containsKey(clock)) {
            this.cleanupBins.get(clock).remove(new Cell(x, y));
        }

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

        int oldAgentX = node.agentX - Command.dirToXChange(node.action.dir1);
        int oldAgentY = node.agentY - Command.dirToYChange(node.action.dir1);

        int newAgentX = node.agentX;
        int newAgentY = node.agentY;

        if(boardContains(newAgentX, newAgentY, instant) != null) {
            return true;
        } else {

            Booking destinationAgentCellCurrentBooking = boardContains(newAgentX, newAgentY, instant-1);
            Booking sourceAgentCellFuturBooking = boardContains(oldAgentX, oldAgentY, instant);

            if(destinationAgentCellCurrentBooking != null && sourceAgentCellFuturBooking != null && destinationAgentCellCurrentBooking.getAgent() == sourceAgentCellFuturBooking.getAgent()) {
                return true;
            }
        }

        int newBoxX = node.boxX;
        int newBoxY = node.boxY;

        int oldBoxX = node.boxX - Command.dirToXChange(node.action.dir2);
        int oldBoxY = node.boxY - Command.dirToYChange(node.action.dir2);

        if(node.action.actType == Command.type.Pull || node.action.actType == Command.type.Push) {

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
        return this.getBoardCell(x, y).get(instant);
    }

    private Agent boardFindAgent(int x, int y, int instant) {

        Iterator it = this.getBoardCell(x, y).keySet().iterator();

        while(it.hasNext()) {
            Integer bookingInstant = (Integer) it.next();

            if(bookingInstant == instant) {
                return this.getBoardCell(x, y).get(bookingInstant).getAgent();
            }
        }
        return null;
    }

    private class Booking implements Comparable {

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

        @Override
        public int compareTo(Object o) {
            return ((Booking) o).getInstant() - this.getInstant();
        }
    }

}
