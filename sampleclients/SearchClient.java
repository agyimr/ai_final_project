package sampleclients;

import java.io.IOException;
import java.util.LinkedList;
import sampleclients.Strategy.*;
import sampleclients.Heuristic.*;

public class SearchClient {
    StrategyBestFirst strategy;
    Agent owner;
    public SearchClient(Agent owner) {
        this.owner = owner;
        strategy = new StrategyBestFirst(new AStar());
    }
    public LinkedList<Node> FindPath(boolean pushingBox, int goalX, int goalY) {
        //System.err.format("Search starting for agent at pos: %d, %d, goal: %d, %d.\n", owner.getX(), owner.getY(), goalX, goalY);
        strategy.clear();
        strategy.heuristic.initializeSearch(owner, pushingBox, goalX, goalY);
        strategy.addToFrontier(new Node(pushingBox, owner));
        int iterations = 0;
        while (true) {
            if (strategy.frontierIsEmpty()) {
                return null;
            }
            Node leafNode = strategy.getAndRemoveLeaf();
            if (!pushingBox && leafNode.agentX == goalX && leafNode.agentY == goalY) {
                return leafNode.extractPlan();
            }
            else if(pushingBox && leafNode.boxX == goalX && leafNode.boxY == goalY){
                return leafNode.extractPlan();
            }
            strategy.addToExplored(leafNode);
            for (Node n : leafNode.getExpandedNodes()) { // The list of expanded nodes is shuffled randomly; see Node.java.
                if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
                    strategy.addToFrontier(n);
                }
            }
            iterations++;
        }
    }
}
