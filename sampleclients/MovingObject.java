package sampleclients;

import java.io.*;
import java.util.*;
import static sampleclients.Command.dir;
import static sampleclients.Command.type;

public class MovingObject extends BasicObject {
    private String color;
    public LinkedList<Node> path;
    public LinkedList<Command> commandList;
    Goal steppedOnGoal = null;
    public MovingObject ( char id, String color, int y, int x , String ObjectType) {
        super(y, x, id,  ObjectType);
        this.color = color;

    }

    public String getColor(){ return color;}

    public Command tryToMove(int x, int y)  throws UnsupportedOperationException {
        updateMap(x, y, RandomWalkClient.NextMainBoard);
        return getMoveDirection(x, y);
    }
    public Command getMoveDirection(int x, int y){
        if(x == getX() && y == getY()) {
            return new Command();
        }
        return new Command(getDirection(x, y));
    }
    dir getDirection(int x,int y) {
        dir Direction = null;
        if(x!=getX()) {
            if(x>getX()) {
                Direction = dir.E;
            } else {
                Direction = dir.W;
            }
        }
        else if(y != getY()) {
            if(y>getY()) {
                Direction = dir.S;
            } else {
                Direction = dir.N;
            }
        }
        return Direction;
    }

    public void changePosition(int x, int y, char[][] board) throws UnsupportedOperationException {
        updateMap(x, y, board);
        setCoordinates(x, y);
    }
    public void updateMap(int x, int y, char[][] board) throws UnsupportedOperationException {
        if(yOutOfBounds(y)
                || xOutOfBounds(x)
                || !spaceEmpty(x,y, board)) throw new UnsupportedOperationException();
        forceMapUpdate(x, y, board);
    }
    public void forceMapUpdate(int x, int y, char[][] board) {
        //make sure you know what you're doing
        manageMovingThroughGoal(x, y, board);
        if(board[getY()][getX()] == getID())
            board[getY()][getX()] = ' ';
        board[y][x] = getID();
    }
    void manageMovingThroughGoal(int x, int y, char[][] board) {
        if(steppedOnGoal != null) {
            board[getY()][getX()] = steppedOnGoal.getID();
            steppedOnGoal = null;
        }
        if(RandomWalkClient.isGoal(board[y][x])) {
            steppedOnGoal = RandomWalkClient.goals.get(board[y][x]);
        }
    }
    boolean yOutOfBounds(int y) { return (y > (RandomWalkClient.MainBoardYDomain - 1) || y < 0);}
    boolean xOutOfBounds(int x) {return (x >= (RandomWalkClient.MainBoardXDomain) || x < 0);}
    boolean spaceEmpty(int x, int y, char[][] board) {return RandomWalkClient.isGoal(board[y][x]) || board[y][x] == ' '; }

    @Override
    public String toString() {
        return getObjectType() + " id:" + getID() + " color: " + getColor() + " at position: (" + getX() + ", " + getY() + ")";
    }

    public LinkedList<Node> findPath(int xGoal, int yGoal) {
//        System.err.println(this.toString() + " " + xGoal + " " + yGoal);
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        path = doAStar(new Node(getX(), getY(), new Command()), new Node(xGoal, yGoal, new Command()));
        if(path != null)
            path.removeFirst();
        return path;
    }

    public static LinkedList<Node> doAStar(Node start, Node goal) {
        Set<Node> closed = new HashSet<Node>();
        Map<Node, Node> fromMap = new HashMap<Node, Node>();
        LinkedList<Node> route = new LinkedList<Node>();
        Map<Node, Double> gScore = new HashMap<Node, Double>();
        final Map<Node, Double> fScore = new HashMap<Node, Double>();
        PriorityQueue<Node> open = new PriorityQueue<Node>(11, new Comparator<Node>() {
            public int compare(Node nodeA, Node nodeB) {
                return Double.compare(fScore.get(nodeA), fScore.get(nodeB));
            }
        });

        gScore.put(start, 0.0);
        fScore.put(start, start.getHeuristic(goal));
        open.offer(start);

        int startInstant = RandomWalkClient.globalPlanningBoard.getClock();

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (current.equals(goal)) {
                while (current != null) {
                    route.add(0, current);
                    current = fromMap.get(current);
                }

                return route;
            }
            closed.add(current);
            for (Node neighbour : current.getNeighbours(startInstant + gScore.get(current).intValue())) {
                if (closed.contains(neighbour)) {
                    continue;
                }

                double tentG = gScore.get(current)
                        + current.getTraversalCost(neighbour);

                boolean contains = open.contains(neighbour);
                if (!contains || tentG < gScore.get(neighbour)) {
                    gScore.put(neighbour, tentG);
                    fScore.put(neighbour, tentG + neighbour.getHeuristic(goal));

                    if (contains) {
                        open.remove(neighbour);
                    }

                    open.offer(neighbour);
                    fromMap.put(neighbour, current);
                }
            }
        }

        return null;
    }
}