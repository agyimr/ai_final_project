package sampleclients;

import java.io.*;
import java.util.*;


public class Node {
    private final int x, y;
    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public double getHeuristic(Node goal) {
        return Math.abs(x - goal.x) + Math.abs(y - goal.y);
    }

    public double getTraversalCost(Node neighbour) {
        return 1;
    }
    public int getX() { return x;}
    public int getY() {return y;}
    public Set<Node> getNeighbours() {
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
                else if (RandomWalkClient.isBox(RandomWalkClient.MainBoard[j][i])
                        || RandomWalkClient.isWall(RandomWalkClient.MainBoard[j][i])) {
                    continue;
                }
                neighbours.add(new Node(i, j));
            }
        }
        return neighbours;
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