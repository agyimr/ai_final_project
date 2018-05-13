package sampleclients;

import java.awt.*;
import java.util.*;

import sampleclients.Strategy.*;
import sampleclients.Heuristic.*;
import sampleclients.room_heuristics.*;

import static sampleclients.RandomWalkClient.gameBoard;
import static sampleclients.RandomWalkClient.roomMaster;
import static sampleclients.room_heuristics.Estimator.estimatePath;

public class SearchClient {
    private StrategyBestFirst strategy;
    private Agent owner;
    private boolean pushingBox;
    private int goalX;
    private int goalY;
    private final int searchIncrement = MainBoard.MainBoardYDomain * MainBoard.MainBoardXDomain  + 10;
    private int searchRange =searchIncrement * 20;
    private boolean recursionTriggered = false;
    public LinkedList<LinkedList<Node>> immovableObstacles = new LinkedList<>();
    public boolean pathBlocked = false;
    public boolean pathInaccessible = false;
    public Point beforeFirstImmovableObstacle = null;
    boolean straightToGoal = false;
    private LinkedList<sampleclients.room_heuristics.Node> roomPath;
    private sampleclients.room_heuristics.Node currentRoom = null;
    private sampleclients.room_heuristics.Node nextRoom = null;

    public SearchClient(Agent owner) {
        this.owner = owner;
        strategy = new StrategyBestFirst(new AStar(owner));
    }
    private void initializeSearch(boolean pushing, int x, int y) {
        strategy.clear();
        strategy.heuristic.initializeSearch(pushing, x, y);
    }
    private void initializeSearch(boolean pushing, Section goalRoom) {
        strategy.clear();
        strategy.heuristic.initializeSearch(pushing, goalRoom);
    }
    public boolean inGoalRoom() {
        return straightToGoal;
    }
    public int getNextRoomPathLengthEstimate() {
        if(nextRoom != null) {
            return nextRoom.g;
        }
        else return 0;
    }
    public LinkedList<Node> continuePath() { //TODO overwrite path at the end
        System.err.println("trying to continue path");
        LinkedList<Node> localPath = getNextRoomPath();
        System.err.println(localPath);
        Agent possibleConflictingAgent = RandomWalkClient.anticipationPlanning.addPath(localPath, owner);
        return localPath;
    }
    private LinkedList<Node> getNextRoomPath() {
        if(pathBlocked) {
            pathBlocked = false;
            beforeFirstImmovableObstacle = null;
            owner.waitForObstacleToBeRemoved();
            LinkedList<Node> dummyPath = new LinkedList<>();
            dummyPath.add(new Node(null, new Command(), owner.getX(), owner.getY()));
            return dummyPath; // to prevent Agent from finding new path
        }
        if(currentRoom == null) return null;
        else if(nextRoom == null && getNextRoom()) {
            return getPathToNextRoom();
        }
        else if(currentRoom.through.contains(new Point(goalX, goalY))) {
            return getPathToGoal();
        }
        else if(nextRoom != null && (nextRoom.through.contains(owner.getCoordinates()) || (owner.isBoxAttached() && nextRoom.through.contains(owner.getAttachedBox().getCoordinates())))) {
            currentRoom = nextRoom;
            if(getNextRoom())
                return getPathToNextRoom();
            else
                return getPathToGoal();
        }
        else {
            if(nextRoom != null) {
                return getPathToNextRoom();
            }
            else {
                return getPathToGoal();
            }
        }
    }
    private LinkedList<Node> getPathToNextRoom() {
        straightToGoal = false;
        System.err.println("finding path to next room!");
        return FindRoomPath(pushingBox, nextRoom.through);
    }
    private LinkedList<Node> getPathToGoal() {
        straightToGoal = true;
        System.err.println("in the goal room!");
        return FindPath(pushingBox, goalX, goalY);
    }

    public boolean getPath(boolean pushingBox, int goalX, int goalY) {
        System.err.println("finding new path");
        straightToGoal = false;
        this.pushingBox = pushingBox;
        this.goalX = goalX;
        this.goalY = goalY;
        pathInaccessible = false;
        pathBlocked = false;
        beforeFirstImmovableObstacle = null;
        immovableObstacles.clear();
        roomPath = RandomWalkClient.roomMaster.getRoomPath(owner.getCoordinates(), new Point(goalX, goalY));
        if(roomPath == null) return false; //TODO impossible to get there
        if( !roomPath.getLast().obstacles.isEmpty()) {
//            System.err.println("Goal coordinates: " + goalX + "' " + goalY);
//            System.err.println(roomPath);
//            System.err.println(owner);
//            System.err.println(owner.getAttachedBox());
//            System.err.println(roomPath.getLast().obstacles);
            Point firstSafeSpace = ObstacleArbitrator.processObstacles(owner, roomPath.getLast().obstacles, gameBoard.getElement(goalX, goalY));
            if(firstSafeSpace != null) {
                //throw new NullPointerException();
                beforeFirstImmovableObstacle = firstSafeSpace;
            }
        }
        currentRoom = roomPath.poll();
        return true;
    }

    private boolean getNextRoom() {
        if(roomPath!= null && !roomPath.isEmpty()) {
            nextRoom = roomPath.poll();
            return true;
        }
        else {
            nextRoom = null;
            return false;
        }
    }

    private LinkedList<Node> FindPath(boolean pushing, int goalX, int goalY) {
        //System.err.format("Search starting for agent at pos: %d, %d, goal: %d, %d.\n", owner.getX(), owner.getY(), goalX, goalY);
        initializeSearch(pushing, goalX, goalY);
        if(pushing) {
            strategy.addToFrontier(new Node(owner.getX(), owner.getY(),
                    owner.getAttachedBox().getX(), owner.getAttachedBox().getY(),
                    owner.getColor(), MainBoard.allBoxes));
        }
        else {
            strategy.addToFrontier(new Node(owner.getX(), owner.getY(), owner.getColor(), MainBoard.allBoxes));
        }
        LinkedList<Node> result = conductSearch(searchRange, goalX, goalY, pushing);
        //System.err.println("Goal coordinates: " + goalX + ", " + goalY + result);
        if(result == null) {
            if(recursionTriggered) {
                return null;
            }
            if(beforeFirstImmovableObstacle != null) {
                pathBlocked = true;
                return findPathBeforeObstacle(pushing);
            }
            processObstacles(roomMaster.getObstacles(owner.getCoordinates(), new Point(goalX, goalY)));
            if(beforeFirstImmovableObstacle != null) {
                System.err.println("Path Blocked");
                return findPathBeforeObstacle(pushing);
            }
            else if(pathInaccessible)  {
                System.err.println("Path inaccessible");
                return null;
            }
            else {
                int oldRange = searchRange;
                searchRange *= 50;
                recursionTriggered = true;
                System.err.println("bigger range search: ");
                LinkedList<Node> mustBeTrue = FindPath(pushing, goalX, goalY);
                searchRange = oldRange;
                recursionTriggered = false;
                return mustBeTrue;
            }
        }
        return result;
    }

    private LinkedList<Node> findPathBeforeObstacle(boolean pushing) {
        recursionTriggered = true;
        System.err.println("POSITION BEFORE OBSTACLE: X: " + beforeFirstImmovableObstacle.x + ", Y: " + beforeFirstImmovableObstacle.y);
        LinkedList<Node> mustBeTrue = FindPath(pushing, beforeFirstImmovableObstacle.x, beforeFirstImmovableObstacle.y);
        System.err.println(mustBeTrue);
        if(mustBeTrue != null && mustBeTrue.isEmpty()) {
            mustBeTrue.add(new Node(null, new Command(), owner.getX(), owner.getY()));
        }
        recursionTriggered = false;
        return mustBeTrue;
    }
    public static boolean nextTo(int firstX, int firstY, int secondX, int secondY) {
        return (Math.abs(firstX - secondX) == 1) && (Math.abs(firstY - secondY) == 0)
                || (Math.abs(firstX - secondX) == 0) && (Math.abs(firstY - secondY) == 1);
    }
    private LinkedList<Node> conductSearch(int maxIterations, int x, int y, boolean pushing) { //FUCKING JAVA
        int iterations = 0;
        boolean goingToBox = false;
        if(gameBoard.isBox(x, y)) goingToBox = true;
        while (true) {
            if (strategy.frontierIsEmpty()) {
                return null;
            }
            Node leafNode = strategy.getAndRemoveLeaf();
            if (!pushing &&
                    ((goingToBox && nextTo(leafNode.agentX, leafNode.agentY, x, y)
                    || leafNode.agentX == x && leafNode.agentY == y))) {
                return leafNode.extractPlan();
            }
            else if(pushing && leafNode.boxX == x && leafNode.boxY == y){
                return leafNode.extractPlan();
            }
            strategy.addToExplored(leafNode);
            for (Node n : leafNode.getExpandedNodes()) { // The list of expanded nodes is shuffled randomly; see Node.java.
                if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
                    strategy.addToFrontier(n);
                }
            }
            if(++iterations > maxIterations) {
                return null;
            }
        }
    }


    private void processObstacles(ArrayList<Obstacle> result) {
        if(result != null) {
            Point firstSafeSpace = ObstacleArbitrator.processObstacles(owner, result, gameBoard.getElement(goalX, goalY));
            if(firstSafeSpace != null) {
                pathBlocked = true;
                beforeFirstImmovableObstacle = firstSafeSpace;
            }
        }
        else {
            pathInaccessible = true;
            System.err.println(owner);
            System.err.println(owner.getAttachedBox());
            System.err.println("Goal coords: " + goalX + ", " + goalY);
            throw new NullPointerException();
        }
    }

    private LinkedList<Node> FindRoomPath(boolean pushingBox, Section goalRoom) {
        initializeSearch(pushingBox, goalRoom);
        if(pushingBox) {
            strategy.addToFrontier(new Node(owner.getX(), owner.getY(),
                    owner.getAttachedBox().getX(), owner.getAttachedBox().getY(),
                    owner.getColor(), MainBoard.allBoxes));
        }
        else {
            strategy.addToFrontier(new Node(owner.getX(), owner.getY(), owner.getColor(), MainBoard.allBoxes));
        }
        LinkedList<Node> result = conductRoomSearch(searchRange,goalRoom, pushingBox);
        if(result == null) {
            if(recursionTriggered) {
                return null;
            }
            if(beforeFirstImmovableObstacle != null) {
                pathBlocked = true;
                return findPathBeforeObstacle(pushingBox);
            }
            processObstacles(roomMaster.getObstacles(owner.getCoordinates(), goalRoom));
            if(beforeFirstImmovableObstacle != null) {
                return findPathBeforeObstacle(pushingBox);
            }
            else if(pathInaccessible)  {
                return null;
            }
            else {
                int oldRange = searchRange;
                searchRange *= 30;
                recursionTriggered = true;
                LinkedList<Node> mustBeTrue = FindRoomPath(pushingBox, goalRoom);
                searchRange = oldRange;
                recursionTriggered = false;
                return mustBeTrue;
            }
        }
        return result;
    }
    private LinkedList<Node> conductRoomSearch(int maxIterations, Section goalRoom, boolean pushing) {//Callable (FUCK YOU JAVA)
        int iterations = 0;
        while (true) {
            if (strategy.frontierIsEmpty()) {
                return null;
            }
            Node leafNode = strategy.getAndRemoveLeaf();
            if (!pushing && goalRoom.contains(new Point(leafNode.agentX, leafNode.agentY))) {
                return leafNode.extractPlan();
            }
            else if (pushing && goalRoom.contains(new Point(leafNode.boxX, leafNode.boxY))) {
                return leafNode.extractPlan();
            }
            strategy.addToExplored(leafNode);
            for (Node n : leafNode.getExpandedNodes()) { // The list of expanded nodes is shuffled randomly; see Node.java.
                if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
                    strategy.addToFrontier(n);
                }
            }
            if(++iterations > maxIterations) {
                return null;
            }
        }
    }
}
