package sampleclients;

import sampleclients.room_heuristics.Obstacle;

import java.util.*;

import static sampleclients.RandomWalkClient.gameBoard;

public class ObstaclePathFinding {

    public ObstaclePathFinding() {

    }
    static int goalX;
    static int goalY;
    static BasicObject[][] boxesArray;
    static int maxIterations = MainBoard.MainBoardXDomain * MainBoard.MainBoardYDomain * 50;
    static int getDistanceToGoal(int x1, int y1) {
        return Math.abs(x1 - goalX) + Math.abs(y1 - goalY);
    }

    public static List<Obstacle> findObstacles(int startX, int startY, int goalX, int goalY) {
        initializeBoxesArray();
        List<ObstacleLightNode>emptyPath = findEmptyPath(startX,  startY,  goalX,  goalY);
        System.err.println(emptyPath);
        if(emptyPath != null) {
            processEmptyPath(emptyPath);
        }
        return null;
    }

    private static void processEmptyPath(List<ObstacleLightNode> emptyPath) {
        int obstaclePathLength = 0;
        boolean obstaclesStarted = false;
        ArrayList<Box> obstacles = new ArrayList<>();
        for(ObstacleLightNode node : emptyPath) {
            if(gameBoard.isBox(node.x, node.y)) {
                ++obstaclePathLength;
                Box obstacle = (Box)gameBoard.getElement(node.x, node.y);
                boxesArray[node.x][node.y] = obstacle;
            }
        }
    }

    private static void initializeBoxesArray() {
        boxesArray = new BasicObject[MainBoard.MainBoardXDomain][MainBoard.MainBoardYDomain];
        for(BasicObject[] array : boxesArray) {
            for(BasicObject obj : array) {
                obj = null;
            }
        }
    }
    private static List<ObstacleLightNode> findEmptyPath(int startX, int startY, int goalX, int goalY) {
        PriorityQueue<ObstacleLightNode> frontier = new PriorityQueue<>(10, (ObstacleLightNode first, ObstacleLightNode second)-> {return first.h - second.h;});
        HashSet<ObstacleLightNode> frontierSet = new HashSet<>();
        HashSet<ObstacleLightNode> explored = new HashSet<>();
        ObstacleLightNode startNode = new ObstacleLightNode(startX, startY);
        frontier.add(startNode);
        frontierSet.add(startNode);
        int iterator = 0;
        while(true) {
            if(frontier.isEmpty()) {
                return null;
            }
            ObstacleLightNode leaf = frontier.remove();
            frontierSet.remove(leaf);
            if(leaf.x == goalX && leaf.y == goalY) {
                return leaf.extractPlan();
            }
            explored.add(leaf);
            for(ObstacleLightNode node : leaf.getNeighbours()) {
                if(!explored.contains(node) && !frontierSet.contains(node)) {
                    frontier.add(node);
                    frontierSet.add(node);
                }
            }
            if(++iterator > maxIterations) {
                return null;
            }
        }
    }

}
class ObstacleLightNode{
    int x = -1;
    int y = -1;
    int time = 0;
    int h = 0;
    ObstacleLightNode parent;
    public ObstacleLightNode(int x, int y) {
        this.x = x;
        this.y = y;
        time = 0;
        h = ObstaclePathFinding.getDistanceToGoal(x, y) + time;
        parent = null;
    }
    public ObstacleLightNode(ObstacleLightNode parent, int x, int y) {
        this.x = x;
        this.y = y;
        time = parent.time + 1;
        h = ObstaclePathFinding.getDistanceToGoal(x, y) + time;
        this.parent = parent;
    }
    List<ObstacleLightNode> getNeighbours() {
        ArrayList<ObstacleLightNode> neighbours = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if ((i == x && j == y) //x position
                        || i < 0 || j < 0 // too small
                        || j >= MainBoard.MainBoardYDomain || i >= MainBoard.MainBoardXDomain //too large
                        || ((i != x && j != y))) //Diagonals! double checked, it's correct
                    continue;

                else if (gameBoard.isWall(i, j)) {
                    continue;
                }
                neighbours.add(new ObstacleLightNode(this, i, j));
            }
        }
        return neighbours;
    }
    public LinkedList<ObstacleLightNode> extractPlan() {
        LinkedList<ObstacleLightNode> plan = new LinkedList<>();
        ObstacleLightNode n = this;
        while (n.parent != null) {
            plan.addFirst(n);
            n = n.parent;
        }
        return plan;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        ObstacleLightNode other = (ObstacleLightNode) obj;
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