package sampleclients;

import java.util.LinkedList;

import static sampleclients.RandomWalkClient.gameBoard;

public class ObstacleArbitrator {

    public static void findSaviors(SearchClient engine) {
        for(LinkedList<Node> list : engine.immovableObstacles) {
            for(Node point : list) {
                BasicObject element = gameBoard.getElement(point.agentX, point.agentY);
                if(element instanceof Box) {
                    Box obstacle = (Box) element;
                    Agent closestAgent = null;
                    int closestDistance = Integer.MAX_VALUE;
                    for (Agent savior : MainBoard.AgentColorGroups.get(obstacle.getColor())) {
                        int distance = RandomWalkClient.roomMaster.getPathEstimate(savior.getCoordinates(), obstacle.getCoordinates());
                        if(distance < closestDistance) {
                            closestAgent = savior;
                            closestDistance = distance;
                        }
                    }
                    closestAgent.removeObstacle(obstacle, closestDistance - point.timeFrame);
                }

            }
        }
        engine.immovableObstacles.clear();
    }
}
