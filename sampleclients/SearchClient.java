package sampleclients;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map;

import sampleclients.Strategy.*;
import sampleclients.Heuristic.*;
import sampleclients.room_heuristics.*;

import static sampleclients.RandomWalkClient.gameBoard;
import static sampleclients.room_heuristics.Estimator.estimatePath;

public class SearchClient {
    private StrategyBestFirst strategy;
    private Agent owner;
    private boolean pushingBox;
    private int goalX;
    private int goalY;
    private final int searchIncrement = MainBoard.MainBoardYDomain * MainBoard.MainBoardXDomain ;
    private int searchRange =searchIncrement * 10;
    private boolean biggerRangeTriggered = false;
    public LinkedList<LinkedList<Node>> immovableObstacles = new LinkedList<>();
    public boolean pathBlocked = false;
    public boolean pathInaccessible = false;
    public Point beforeFirstImmovableObstacle = null;

    private LinkedList<sampleclients.room_heuristics.Node> roomPath;
    private Section currentRoom = null;
    private Section nextRoom = null;

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

    public LinkedList<Node> continuePath() { //TODO overwrite path at the end
        System.err.println("trying to continue path");
        LinkedList<Node> localPath = getNextRoomPath();
        Agent possibleConflictingAgent  = RandomWalkClient.anticipationPlanning.addPath(localPath, owner);
        return localPath;
    }
    private LinkedList<Node> getNextRoomPath() {
        if(pathBlocked) {
            pathBlocked = false;
            owner.waitForObstacleToBeRemoved();
            LinkedList<Node> dummyPath = new LinkedList<>();
            dummyPath.add(new Node(null, new Command(), owner.getX(), owner.getY()));
            return dummyPath; // to prevent Agent from finding new path

        }

        if(currentRoom == null) return null;
        else if(nextRoom == null && getNextRoom()) {
            return getPathToNextRoom();
        }
        else if(currentRoom.contains(new Point(goalX, goalY))) {
            return getPathToGoal();
        }
        else if(nextRoom != null && (nextRoom.contains(owner.getCoordinates()) || (owner.isBoxAttached() && nextRoom.contains(owner.getAttachedBox().getCoordinates())))) {
            currentRoom = nextRoom;
            if(getNextRoom())
                return getPathToNextRoom();
            else
                return getPathToGoal();
        }
        else {
            return FindPath(pushingBox, goalX, goalY);
//            RandomWalkClient.roomMaster.passages.PrintMap();
//            System.err.println(roomPath);
//            System.err.println(currentRoom);
//            System.err.println(nextRoom);
//            throw new NegativeArraySizeException();
        }
    }
    private LinkedList<Node> getPathToNextRoom() {
        return FindRoomPath(pushingBox, nextRoom);
    }
    private LinkedList<Node> getPathToGoal() {
        return FindPath(pushingBox, goalX, goalY);
    }

    public boolean getPath(boolean pushingBox, int goalX, int goalY) {
        this.pushingBox = pushingBox;
        this.goalX = goalX;
        this.goalY = goalY;
        pathInaccessible = false;
        pathBlocked = false;
        beforeFirstImmovableObstacle = null;
        immovableObstacles.clear();
        roomPath = RandomWalkClient.roomMaster.getRoomPath(owner.getCoordinates(), new Point(goalX, goalY));
        if(!roomPath.getLast().obstacles.isEmpty()) {
            System.err.println(roomPath);
            System.err.println(owner);
            System.err.println(owner.getAttachedBox());
            System.err.println(roomPath.getLast().obstacles);
            ObstacleArbitrator.processObstacles(owner, roomPath.getLast().obstacles);
            //throw new NullPointerException();//TODO process
        }
        if(roomPath == null) return false; //TODO impossible to get there
        currentRoom = roomPath.poll().through;
        return true;
    }

    private boolean getNextRoom() {
        if(roomPath!= null && !roomPath.isEmpty()) {
            nextRoom = roomPath.poll().through;
            return true;
        }
        else {
            nextRoom = null;
            return false;
        }
    }

    private LinkedList<Node> FindPath(boolean pushingBox, int goalX, int goalY) {
        //System.err.format("Search starting for agent at pos: %d, %d, goal: %d, %d.\n", owner.getX(), owner.getY(), goalX, goalY);
        initializeSearch(pushingBox, goalX, goalY);
        if(pushingBox) {
            strategy.addToFrontier(new Node(owner.getX(), owner.getY(),
                    owner.getAttachedBox().getX(), owner.getAttachedBox().getY(),
                    owner.getColor(), MainBoard.allBoxes));
        }
        else {
            strategy.addToFrontier(new Node(owner.getX(), owner.getY(), owner.getColor(), MainBoard.allBoxes));
        }
        LinkedList<Node> result = conductSearch(searchRange, goalX, goalY, pushingBox);
        //System.err.println("Goal coordinates: " + goalX + ", " + goalY + result);
        if(result == null) {
            if(biggerRangeTriggered || pathBlocked) {
                return null;
            }
            findObstacles();
            if(pathBlocked) {
                System.err.println("Path Blocked");
                return FindPath(pushingBox, beforeFirstImmovableObstacle.x, beforeFirstImmovableObstacle.y);
            }
            else if(pathInaccessible)  {
                System.err.println("Path inaccessible");
                return null;
            }
            else {
                int oldRange = searchRange;
                searchRange *= 10;
                biggerRangeTriggered = true;
                System.err.println("bigger range search: ");
                LinkedList<Node> mustBeTrue = FindPath(pushingBox, goalX, goalY);
                searchRange = oldRange;
                biggerRangeTriggered = false;
                return mustBeTrue;
            }
        }
        return result;
    }
    private LinkedList<Node> conductSearch(int maxIterations, int x, int y, boolean pushing) { //FUCKING JAVA
        int iterations = 0;
        while (true) {
            if (strategy.frontierIsEmpty()) {
                return null;
            }
            Node leafNode = strategy.getAndRemoveLeaf();
            if (!pushing && Agent.nextTo(leafNode.agentX, leafNode.agentY, x, y)) {
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
    private void findObstacles() {
//        beforeFirstImmovableObstacle = null;
//        initializeSearch(false, goalX, goalY);
//        strategy.addToFrontier(new Node(owner.getX(), owner.getY(), owner.getColor(), Collections.emptyList()));
        //LinkedList<Node> emptySearchResult = conductSearch(searchRange, goalX, goalY, false);
        //handleEmptyPathResults(emptySearchResult);
        PathWithObstacles result = Estimator.estimatePath(owner.getCoordinates(), new Point(goalX, goalY), currentRoom, 0, true);
        processObstacles(result);
    }

    private void processObstacles(PathWithObstacles result) {
        if(result != null) {
            if(result.obstacles.isEmpty()) {
                pathBlocked = false;
            }
            else {
                beforeFirstImmovableObstacle = result.obstacles.get(0).waitingPosition;
                pathBlocked = true;
                ObstacleArbitrator.processObstacles(owner, result.obstacles);
            }
        }
        else {
            pathInaccessible = true;
        }
    }

    private void examineBoxesOnPath(LinkedList<Node> path, LinkedList<Box> obstacles, LinkedList<LinkedList<Node>> workaroundPaths) {
        if(path == null || path.isEmpty()) return;
        Node previousNode = path.get(0);
        Node workaroundBegin = null;
        int workaroundLength = 0;
        for(Node point : path) {
            if(gameBoard.isBox(point.agentX, point.agentY)) {
                if(workaroundBegin == null) {
                    workaroundPaths.push(new LinkedList<>());
                    workaroundBegin = previousNode;
                    workaroundLength = 1;
                }
                else {
                    ++workaroundLength;
                }
                if(!((Box) gameBoard.getElement(point.agentX, point.agentY)).getColor().equals(owner.getColor())) {
                    obstacles.add((Box) gameBoard.getElement(point.agentX, point.agentY));
                    workaroundPaths.peek().add(point);
                }
            }
            else if(workaroundBegin != null && !obstacles.isEmpty()) {
                System.err.println(workaroundLength);
                System.err.println(workaroundBegin + " end: " + point);
                System.err.println(obstacles);
                initializeSearch(false, point.agentX, point.agentY);
                strategy.addToFrontier(new Node(workaroundBegin.agentX, workaroundBegin.agentY, owner.getColor(), obstacles));
                LinkedList<Node> partialSearchResult = conductSearch(50* workaroundLength, point.agentX, point.agentY, false);
                if(partialSearchResult != null) {
                    examineBoxesOnPath(partialSearchResult, obstacles, workaroundPaths);
                }
                else {
                    if(!pathBlocked) { // setting a flag
                        pathBlocked = true;
                        //beforeFirstImmovableObstacle = workaroundBegin;
                    }
                    for(LinkedList<Node> list : workaroundPaths) {
                        if(!list.isEmpty())
                            immovableObstacles.add(list);
                    }
                    workaroundPaths.clear();
                    obstacles.clear();
                }
                workaroundBegin = null;
                workaroundLength = 0;
            }
            previousNode = point;
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
            findRoomObstacles(goalRoom);
            if(pathBlocked) {
                System.err.println(immovableObstacles);
                return FindPath(pushingBox, beforeFirstImmovableObstacle.x, beforeFirstImmovableObstacle.y);
            }
            else if(pathInaccessible)  {
                return null;
            }
            else {
                int oldRange = searchRange;
                searchRange *= 20;
                LinkedList<Node> mustBeTrue = FindRoomPath(pushingBox, goalRoom);
                searchRange = oldRange;
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
    private void findRoomObstacles(Section goalRoom) {
//        initializeSearch(false, goalRoom);
//        strategy.addToFrontier(new Node(owner.getX(), owner.getY(), owner.getColor(), Collections.emptyList()));
//        LinkedList<Node> emptySearchResult = conductRoomSearch(searchRange,goalRoom, false);
//        handleEmptyPathResults(emptySearchResult);
        PathWithObstacles result = Estimator.estimatePath(owner.getCoordinates(), goalRoom, currentRoom, 0, true);
        processObstacles(result);
    }

    private void handleEmptyPathResults(LinkedList<Node> emptySearchResult) {
        if(emptySearchResult != null) {
            LinkedList<Box> obstacles = new LinkedList<>();
            LinkedList<LinkedList<Node>> workaroundPaths = new LinkedList<>();
            examineBoxesOnPath(emptySearchResult, obstacles, workaroundPaths);
            if(!immovableObstacles.isEmpty()) {
                System.err.println(immovableObstacles);
                System.err.println(beforeFirstImmovableObstacle);
                System.err.println(owner);
                //throw new NegativeArraySizeException();
                ObstacleArbitrator.findSaviors(this, owner);
            }
        }
        else{
            pathInaccessible = true;
            throw new NullPointerException();
//            if(pushingBox) {
//                //owner.getAttachedBox().noGoalOnTheMap = true;
//            }
//            else {
//                //TODO box is inaccessible, handle accordingly
//            }
        }
    }
}
