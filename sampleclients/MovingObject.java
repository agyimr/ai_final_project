package sampleclients;

import java.io.*;
import java.util.*;
import static sampleclients.Command.dir;
import static sampleclients.Command.type;

public class MovingObject extends BasicObject {
    private String color;
    private char id;
    public LinkedList<Node> path;
    public MovingObject ( char id, String color, int currentRow, int currentColumn , String ObjectType) {
        super(currentRow, currentColumn, ObjectType);
        this.color = color;
        this.id = id;
    }
    public char getID() { return id;}
    public String getColor(){ return color;}

    public String move(dir Direction) throws UnsupportedOperationException {
        if(RandomWalkClient.MainBoard[getY()][getX()] != getID()) return "NoOp";

        try{
            switch (Direction) {
                case N:
                    changePosition(getY() - 1, getX());
                    break;

                case S:
                    changePosition(getY() + 1, getX());
                    break;

                case E:
                    changePosition(getY(), getX() + 1);
                    break;

                case W:
                    changePosition(getY(), getX() - 1);
                    break;

            }
        }
        catch(UnsupportedOperationException exc) {

            return "NoOp";
        }
        return type.Move + "(" + Direction + ")";
    }
    //Overloaded method, beware!
    public String move(int x, int y) throws UnsupportedOperationException {
        if(RandomWalkClient.MainBoard[getY()][getX()] != getID()) return "NoOp";
        dir Direction = dir.N;
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
        try{
            changePosition(y, x);
        }
        catch(UnsupportedOperationException exc) {
            return "NoOp";
        }
        System.err.println(Direction);
        return type.Move + "(" + Direction + ")";
    }
    void changePosition(int y, int x) throws UnsupportedOperationException {
        if(yOutOfBounds(y)
                || xOutOfBounds(x)
                || !spaceEmpty(y,x)) throw new UnsupportedOperationException();
        RandomWalkClient.MainBoard[getY()][getX()] = ' ';
        setY(y);
        setX(x);
        RandomWalkClient.MainBoard[y][x] = getID();
    }
    boolean yOutOfBounds(int y) { return (y > (RandomWalkClient.MainBoardYDomain - 1) || y < 0);}
    boolean xOutOfBounds(int x) {return (x >= (RandomWalkClient.MainBoardXDomain) || x < 0);}
    boolean spaceEmpty(int y, int x) {return RandomWalkClient.MainBoard[y][x] == ' '; }


    public LinkedList<Node> findPath(int xGoal, int yGoal) {
        path = doAStar(new Node(getX(), getY()), new Node(xGoal, yGoal));
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

            for (Node neighbour : current.getNeighbours()) {
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