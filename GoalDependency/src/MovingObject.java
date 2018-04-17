import java.util.*;

import static Command.dir;
import static Command.type;

public class MovingObject extends BasicObject {
    private String color;
    public LinkedList<Node> path;
    Goal steppedOnGoal = null;
    public MovingObject ( char id, String color, int y, int x , String ObjectType) {
        super(y, x, id,  ObjectType);
        this.color = color;

    }

    public String getColor(){ return color;}

    public String move(dir Direction) throws UnsupportedOperationException {
        if(RandomWalkClient.MainBoard[getY()][getX()] != getID()) return "NoOp";

        try{
            switch (Direction) {
                case N:
                    changePosition(getX(), getY() - 1);
                    break;

                case S:
                    changePosition(getX(), getY() + 1);
                    break;

                case E:
                    changePosition(getX()+ 1, getY());
                    break;

                case W:
                    changePosition(getX() - 1, getY());
                    break;

            }
        }
        catch(UnsupportedOperationException exc) {
            throw exc;
        }
        return type.Move + "(" + Direction + ")";
    }
    //Overloaded method, beware!
    public String move(int x, int y) throws UnsupportedOperationException {
//        if(RandomWalkClient.MainBoard[getY()][getX()] != getID()) return "NoOp";

        String move = type.Move + "(" + getDirection(x, y) + ")";
        try{
            changePosition(x, y);
        }
        catch(UnsupportedOperationException exc) {
            throw exc;
        }
        return move;
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
    public void changePosition(int x, int y) throws UnsupportedOperationException {
        if(yOutOfBounds(y)
                || xOutOfBounds(x)
                || !spaceEmpty(y,x)) throw new UnsupportedOperationException();
        if(steppedOnGoal != null) {
            RandomWalkClient.MainBoard[getY()][getX()] = steppedOnGoal.getID();
            steppedOnGoal = null;
        }
        if(RandomWalkClient.isGoal(RandomWalkClient.MainBoard[y][x])) {
            steppedOnGoal = RandomWalkClient.goals.get(RandomWalkClient.MainBoard[y][x]);
        }
        forceNewPosition(x, y);
    }
    public void forceNewPosition(int x, int y) {
        //make sure you know what you're doing
        if(RandomWalkClient.MainBoard[getY()][getX()] == getID())
            RandomWalkClient.MainBoard[getY()][getX()] = ' ';
        setY(y);
        setX(x);
        RandomWalkClient.MainBoard[y][x] = getID();
    }
    boolean yOutOfBounds(int y) { return (y > (RandomWalkClient.MainBoardYDomain - 1) || y < 0);}
    boolean xOutOfBounds(int x) {return (x >= (RandomWalkClient.MainBoardXDomain) || x < 0);}
    boolean spaceEmpty(int y, int x) {return RandomWalkClient.isGoal(RandomWalkClient.MainBoard[y][x]) || RandomWalkClient.MainBoard[y][x] == ' '; }

    @Override
    public String toString() {
        return getObjectType() + " id:" + getID() + " color: " + getColor() + " at position: (" + getX() + ", " + getY() + ")";
    }

    public LinkedList<Node> findPath(int xGoal, int yGoal) {
        path = doAStar(new Node(getX(), getY()), new Node(xGoal, yGoal));
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