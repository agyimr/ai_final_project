package sampleclients;

import java.util.*;
import sampleclients.Strategy.*;
import sampleclients.Heuristic.*;

import static sampleclients.RandomWalkClient.gameBoard;

public class SearchClient {
    private StrategyBestFirst strategy;
    private Agent owner;
    private boolean pushingBox;
    private int goalX;
    private int goalY;
    private final int searchIncrement = MainBoard.MainBoardYDomain * MainBoard.MainBoardXDomain ;//TODO set this  * 2 for exception after conflict in MAthomasAppartment
    private int searchRange =searchIncrement * 10; //TODO set this to 2500 for exception after conflict in MAthomasAppartment   or multiply *2
    public LinkedList<LinkedList<Box>> immovableObstacles = new LinkedList<>();
    public boolean pathBlocked = false;
    private Node firstImmovableObstacle = null;
    public SearchClient(Agent owner) {
        this.owner = owner;
        strategy = new StrategyBestFirst(new AStar(owner));
    }
    private void initializeSearch(boolean pushing, int x, int y) {
        strategy.clear();
        strategy.heuristic.initializeSearch(pushing, x, y);


    }

    public LinkedList<Node> getPath(boolean pushingBox, int goalX, int goalY) {
        firstImmovableObstacle = null;
        pathBlocked = false;
        immovableObstacles.clear();
        return FindPath(pushingBox, goalX, goalY);
    }
    private LinkedList<Node> FindPath(boolean pushingBox, int goalX, int goalY) {
        //System.err.format("Search starting for agent at pos: %d, %d, goal: %d, %d.\n", owner.getX(), owner.getY(), goalX, goalY);
        this.pushingBox = pushingBox;
        this.goalX = goalX;
        this.goalY = goalY;

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
            if(firstImmovableObstacle != null) {
                System.err.println("Hello find obstacles");
                System.err.println("Hello find obstacles");
                System.err.println("Hello find obstacles");
                System.err.println(immovableObstacles);
                return FindPath(pushingBox, firstImmovableObstacle.agentX, firstImmovableObstacle.agentY);
            }
            else {
                int oldRange = searchRange;
                searchRange *= 10;
                LinkedList<Node> mustBeTrue = FindPath(pushingBox, goalX, goalY);
                searchRange = oldRange;
                return mustBeTrue;
            }
        }
        return result;
    }
    private LinkedList<Node> conductSearch(int maxIterations, int x, int y, boolean pushing) {
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
        if(emptySearchResult != null) {
            LinkedList<Box> obstacles = new LinkedList<>();
            examineBoxesOnPath(emptySearchResult, obstacles);
        }
        else{
            if(pushingBox) {
                owner.getAttachedBox().noGoalOnTheMap = true;
            }
            else {
                //TODO box is inaccessible, handle accordingly
            }
        }
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
                        firstImmovableObstacle = workaroundBegin;
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
}
