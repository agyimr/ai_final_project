package sampleclients;

import java.awt.*;
import java.util.*;
import sampleclients.Strategy.*;
import sampleclients.Heuristic.*;
import sampleclients.room_heuristics.*;

import static sampleclients.RandomWalkClient.gameBoard;

public class SearchClient {
    private StrategyBestFirst strategy;
    private Agent owner;
    private boolean pushingBox;
    private int goalX;
    private int goalY;
    private final int searchIncrement = MainBoard.MainBoardYDomain * MainBoard.MainBoardXDomain ;
    private int searchRange =searchIncrement * 5;
    public LinkedList<LinkedList<Box>> immovableObstacles = new LinkedList<>();
    public boolean pathBlocked = false;
    private Node beforeFirstImmovableObstacle = null;

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
    public LinkedList<Node> continuePath() {
        if(currentRoom == null) return null;
        pathBlocked = false;
        beforeFirstImmovableObstacle = null;
        immovableObstacles.clear();
        //before scheduling any path, maybe you just advanced to a nwe room?
        if(nextRoom!= null && nextRoom.contains(owner.getCoordinates())) {
            currentRoom = nextRoom;
        }

        if(currentRoom.contains(new Point(goalX, goalY))) {
            return FindPath(pushingBox, goalX, goalY);
        }
        else if(getNextRoom()) {
            return FindRoomPath(pushingBox, nextRoom);
        }
        else {
            return FindPath(pushingBox, goalX, goalY);
        }
    }
    public LinkedList<Node> getPath(boolean pushingBox, int goalX, int goalY) {
        this.pushingBox = pushingBox;
        this.goalX = goalX;
        this.goalY = goalY;
        roomPath = RandomWalkClient.roomMaster.getRoomPath(owner.getCoordinates(), new Point(goalX, goalY));
        System.err.println(roomPath);
        if(roomPath == null) return FindPath(pushingBox, goalX, goalY);
        currentRoom = roomPath.poll().through;
        return continuePath();
    }

    private boolean getNextRoom() {
        if(!roomPath.isEmpty()) {
            nextRoom = roomPath.poll().through;
            return true;
        }
        else {
            nextRoom = null;
            return false;
        }
    }
    public int getPathEstimate(Point originCoordinates, Point goalCoordinates) {
        LinkedList<sampleclients.room_heuristics.Node> result = RandomWalkClient.roomMaster.getRoomPath(originCoordinates, goalCoordinates);
        if(result == null) return Integer.MAX_VALUE;
        else return result.poll().g;

    }
    private LinkedList<Node> FindPath(boolean pushingBox, int goalX, int goalY) {
        //System.err.format("Search starting for agent at pos: %d, %d, goal: %d, %d.\n", owner.getX(), owner.getY(), goalX, goalY);

        initializeSearch(pushingBox, goalX, goalY);

        if(pushingBox) {
            strategy.addToFrontier(new Node(owner.getX(), owner.getY(),
                    owner.getAttachedBox().getX(), owner.getAttachedBox().getY(),
                    owner.getColor(), MainBoard.boxes.values()));
        }
        else {
            strategy.addToFrontier(new Node(owner.getX(), owner.getY(), owner.getColor(), MainBoard.boxes.values()));
        }
        LinkedList<Node> result = conductSearch(searchRange, goalX, goalY, pushingBox);
        if(result == null) {
            findObstacles();
            if(pathBlocked) {
                System.err.println("Hello find obstacles");
                System.err.println("Hello find obstacles");
                System.err.println("Hello find obstacles");
                System.err.println(immovableObstacles);
                return FindPath(pushingBox, beforeFirstImmovableObstacle.agentX, beforeFirstImmovableObstacle.agentY);
            }
            else {
                int oldRange = searchRange;
                searchRange *= 20;
                LinkedList<Node> mustBeTrue = FindPath(pushingBox, goalX, goalY);
                searchRange = oldRange;
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
        initializeSearch(false, goalX, goalY);
        strategy.addToFrontier(new Node(owner.getX(), owner.getY(), owner.getColor(), Collections.emptyList()));
        LinkedList<Node> emptySearchResult = conductSearch(searchRange, goalX, goalY, false);
        handleEmptyPathResults(emptySearchResult);
    }
    private void examineBoxesOnPath(LinkedList<Node> path, LinkedList<Box> obstacles) {
        if(path == null || path.isEmpty()) return;
        Node previousNode = path.get(0);
        Node workaroundBegin = null;
        int workaroundLength = 0;
        for(Node point : path) {
            if(gameBoard.isBox(point.agentX, point.agentY)) {
                if(!((Box) gameBoard.getElement(point.agentX, point.agentY)).getColor().equals(owner.getColor())) {
                    obstacles.add((Box) gameBoard.getElement(point.agentX, point.agentY));
                }
                if(workaroundBegin == null) {
                    workaroundBegin = previousNode;
                    workaroundLength = 1;
                }
                else {
                    ++workaroundLength;
                }
            }
            else if(workaroundBegin != null) {
                initializeSearch(false, goalX, goalY);
                strategy.addToFrontier(new Node(workaroundBegin.agentX, workaroundBegin.agentY, owner.getColor(), obstacles));
                LinkedList<Node> partialSearchResult = conductSearch(100* workaroundLength, point.agentX, point.agentY, false);
                if(partialSearchResult != null) {
                    examineBoxesOnPath(partialSearchResult, obstacles);
                }
                else {
                    if(!pathBlocked) {
                        beforeFirstImmovableObstacle = workaroundBegin;
                        pathBlocked = true;
                    }
                    immovableObstacles.add(new LinkedList<>(obstacles));
                    obstacles.clear();
                }
                workaroundBegin = null;
                workaroundLength = 0;
            }
            previousNode = point;
        }

    }

    private LinkedList<Node> FindRoomPath(boolean pushingBox, Section goalRoom) {
        //System.err.format("Search starting for agent at pos: %d, %d, goal: %d, %d.\n", owner.getX(), owner.getY(), goalX, goalY);

        initializeSearch(pushingBox, goalRoom);
        if(pushingBox) {
            strategy.addToFrontier(new Node(owner.getX(), owner.getY(),
                    owner.getAttachedBox().getX(), owner.getAttachedBox().getY(),
                    owner.getColor(), MainBoard.boxes.values()));
        }
        else {
            strategy.addToFrontier(new Node(owner.getX(), owner.getY(), owner.getColor(), MainBoard.boxes.values()));
        }
        LinkedList<Node> result = conductRoomSearch(searchRange,goalRoom);
        if(result == null) {
            findRoomObstacles(goalRoom);
            if(pathBlocked) {
                System.err.println(immovableObstacles);
                return FindPath(pushingBox, beforeFirstImmovableObstacle.agentX, beforeFirstImmovableObstacle.agentY);
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
    private LinkedList<Node> conductRoomSearch(int maxIterations, Section goalRoom) {//Callable (FUCK YOU JAVA)
        int iterations = 0;
        while (true) {
            if (strategy.frontierIsEmpty()) {
                return null;
            }
            Node leafNode = strategy.getAndRemoveLeaf();
            if (goalRoom.contains(new Point(leafNode.agentX, leafNode.agentY))) {
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
        initializeSearch(false, goalRoom);
        strategy.addToFrontier(new Node(owner.getX(), owner.getY(), owner.getColor(), Collections.emptyList()));
        LinkedList<Node> emptySearchResult = conductRoomSearch(searchRange,goalRoom);
        handleEmptyPathResults(emptySearchResult);
    }

    private void handleEmptyPathResults(LinkedList<Node> emptySearchResult) {
        if(emptySearchResult != null) {
            LinkedList<Box> obstacles = new LinkedList<>();
            examineBoxesOnPath(emptySearchResult, obstacles);
        }
        else{
            if(pushingBox) {
                //owner.getAttachedBox().noGoalOnTheMap = true;
            }
            else {
                //TODO box is inaccessible, handle accordingly
            }
        }
    }
}
