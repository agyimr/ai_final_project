package sampleclients;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.*;
import java.util.Random;

import sampleclients.Command.type;

public class Node {
    private static final Random RND = new Random(3);
    public int agentY;
    public int agentX;
    public int boxY = -1;
    public int boxX = -1;
    public int timeFrame = -1;
    public static String ownerColor;
    public Box[][] boxes;
    public Node parent;
    public Command action;
    private int g;
    private int _hash = 0;

    public Node(int ownerX, int ownerY, String color, Collection<Box> consideredBoxes) {
        action = new Command();
        ownerColor = color;
        agentX = ownerX;
        agentY = ownerY;
        this.g = 0;
        timeFrame = RandomWalkClient.anticipationPlanning.getClock();
        initializeBoxes(consideredBoxes);
    }

    private void initializeBoxes(Collection<Box> consideredBoxes) {
        boxes = new Box[MainBoard.MainBoardYDomain][MainBoard.MainBoardXDomain];

        for (int y = 0; y < MainBoard.MainBoardYDomain; y ++)
            for (int x = 0; x < MainBoard.MainBoardXDomain; x++)
                boxes[y][x] = null;

        for(Box current: consideredBoxes) {
            boxes[current.getY()][current.getX()] = current;
        }
    }

    public Node(int ownerX, int ownerY, int boxX, int boxY, String color, Collection<Box> consideredBoxes) {
        ownerColor = color;
        agentX = ownerX;
        agentY = ownerY;
        this.g = 0;
        initializeBoxes(consideredBoxes);
        this.boxX = boxX;
        this.boxY = boxY;
        action = new Command();
        timeFrame = RandomWalkClient.anticipationPlanning.getClock();
    }
    public Node(Node parent, Command action, int agentX, int agentY) {
        boxes = new Box[MainBoard.MainBoardYDomain][MainBoard.MainBoardXDomain];
        this.parent = parent;
        this.action = action;
        this.agentX = agentX;
        this.agentY = agentY;
        if (parent == null) {
            this.g = 0;
        } else {
            this.timeFrame = parent.timeFrame + 1;
            this.g = parent.g() + 1;
        }
    }
    public Node(Node parent, Command action, int agentX, int agentY, int boxX, int boxY) {
        boxes = new Box[MainBoard.MainBoardYDomain][MainBoard.MainBoardXDomain];
        this.parent = parent;
        this.action = action;
        this.agentX = agentX;
        this.agentY = agentY;
        this.boxX = boxX;
        this.boxY = boxY;
        if (parent == null) {
            this.g = 0;
        } else {
            this.timeFrame = parent.timeFrame + 1;
            this.g = parent.g() + 1;
        }
    }
    public int g() {
        return this.g;
    }
    public boolean isInitialState() {
        return this.parent == null;
    }

    public ArrayList<Node> getExpandedNodes() {
        ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.every.length + 1);
        expandedNodes.add(this.childNode(new Command(), agentX, agentY, -1, -1));
        for (Command c : Command.every) {
            // Determine applicability of action
            int newAgentY = this.agentY + Command.dirToYChange(c.dir1);
            int newAgentX = this.agentX + Command.dirToXChange(c.dir1);

            if (c.actType == type.Move) {
                // Check if there's a wall or box on the cell to which the agent is moving
                if (this.cellIsFree(newAgentY, newAgentX)) {
                    Node n = this.childNode(c, newAgentX, newAgentY, -1, -1);
                    expandedNodes.add(n);
                }
            } else if (c.actType == type.Push) {
                // Make sure that there's actually a box to move
                if (this.boxAt(newAgentX, newAgentY) && boxHasTheSameColor(newAgentX, newAgentY)) {
                    int newBoxRow = newAgentY + Command.dirToYChange(c.dir2);
                    int newBoxCol = newAgentX + Command.dirToXChange(c.dir2);
                    // .. and that new cell of box is free
                    if (this.cellIsFree(newBoxRow, newBoxCol)) {
                        Node n = this.childNode(c, newAgentX, newAgentY, newBoxCol, newBoxRow);
                        n.boxes[newBoxRow][newBoxCol] = this.boxes[newAgentY][newAgentX];
                        n.boxes[newAgentY][newAgentX] = null;
                        expandedNodes.add(n);
                    }
                }
            } else if (c.actType == type.Pull) {
                // Cell is free where agent is going
                if (this.cellIsFree(newAgentY, newAgentX)) {
                    int boxY = this.agentY + Command.dirToYChange(c.dir2);
                    int boxX = this.agentX + Command.dirToXChange(c.dir2);
                    // .. and there's a box in "dir2" of the agent
                    if (this.boxAt(boxX, boxY) && boxHasTheSameColor(boxX, boxY)) {
                        Node n = this.childNode(c, newAgentX, newAgentY, this.agentX, this.agentY);
                        n.boxes[this.agentY][this.agentX] = this.boxes[boxY][boxX];
                        n.boxes[boxY][boxX] = null;
                        expandedNodes.add(n);
                    }
                }
            }
        }
        Collections.shuffle(expandedNodes, RND);
        return expandedNodes;
    }

    private boolean cellIsFree(int y, int x) {
        return (this.boxes[y][x] == null) && !RandomWalkClient.gameBoard.isWall(x, y);
    }

    public boolean boxAt(int x, int y) {
        return this.boxes[y][x] != null;
    }

    private boolean boxHasTheSameColor(int x, int y) { return boxes[y][x].getColor().equals((ownerColor));}
    private Node childNode(Command action, int agentX, int agentY, int boxX, int boxY) {
        Node copy = new Node(this, action, agentX, agentY, boxX, boxY);
        for (int y = 0; y < MainBoard.MainBoardYDomain; y++) {
            System.arraycopy(this.boxes[y], 0, copy.boxes[y], 0, MainBoard.MainBoardXDomain);
        }
        return copy;
    }

    public LinkedList<Node> extractPlan() {
        LinkedList<Node> plan = new LinkedList<Node>();
        Node n = this;
        while (!n.isInitialState()) {
            plan.addFirst(n);
            n = n.parent;
        }
        return plan;
    }

    @Override
    public int hashCode() {
        if (this._hash == 0) {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.agentX;
            result = prime * result + this.agentY;
            result = prime * result + Arrays.deepHashCode(this.boxes);
            this._hash = result;
        }
        return this._hash;
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
        if (this.agentY != other.agentY || this.agentX != other.agentX)
            return false;
        if(this.action.actType == type.Noop && other.action.actType == type.Noop &&
                this.timeFrame != other.timeFrame)
            return false;
        return Arrays.deepEquals(this.boxes, other.boxes);
    }

    @Override
    public String toString() {
        return "Node:" + action + " agent: " + agentX + ", " + agentY + " box: " + boxX + ", " + boxY;
    }
}