package sampleclients;

import java.util.*;
import static sampleclients.Command.dir;

public class Node {
    private final int x, y;
    private final Command action;
    public Node(int x, int y, Command a) {
        this.x = x;
        this.y = y;
        this.action = a;
    }
    public double getHeuristic(Node goal) {
        return Math.abs(x - goal.x) + Math.abs(y - goal.y);
    }

    public double getTraversalCost(Node neighbour) {
        return 1;
    }
    public int getX() { return x;}
    public int getY() {return y;}
    public Command getAction() { return action;}
    public Set<Node> getNeighbours(int instant) {
        Set<Node> neighbours = new HashSet<Node>();

        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if ((i == x && j == y) //x position
                        || i < 0 || j < 0 // too small
                        || j >= RandomWalkClient.MainBoardYDomain || i >= RandomWalkClient.MainBoardXDomain //too large
                        || ((i != x && j != y))) //Diagonals! double checked, it's correct
                {
                    continue;
                }
                else {

                    boolean incomingConflict = false;

                    //if(x == 11 && y == 7) {
                    if(instant != -1) {
                        incomingConflict = RandomWalkClient.globalPlanningBoard.wouldBeInConflict(x, y, instant);

                    }
//                        System.err.println("YAX from " + RandomWalkClient.globalPlanningBoard.getClock() + " to " + instant + " at " + x + "," + y);
                    //}

                    if (RandomWalkClient.isBox(RandomWalkClient.MainBoard[j][i])
                        || RandomWalkClient.isWall(RandomWalkClient.MainBoard[j][i])
                        || incomingConflict ) {
                        continue;
                    }
                }
                neighbours.add(new Node(i, j, new Command(getDirection(i, j))));
            }
        }
        return neighbours;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17;
        result = prime * result;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }



}