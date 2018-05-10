package sampleclients;

import sampleclients.room_heuristics.Obstacle;
import sampleclients.room_heuristics.PathWithObstacles;

import java.util.*;

import static sampleclients.RandomWalkClient.gameBoard;

public class ObstacleArbitrator {
    public static Map<Agent, Agent> helpersDictionary= new HashMap<>();
    public static void findSaviors(SearchClient engine, Agent inNeed) {
        for(LinkedList<Node> list : engine.immovableObstacles) {
            for(Node point : list) {
                BasicObject element = gameBoard.getElement(point.agentX, point.agentY);
                if(element instanceof Box) {
                    Box obstacle = (Box) element;
                    if(obstacle.assignedAgent != null) continue;
                    Agent closestAgent = null;
                    int closestDistance = Integer.MAX_VALUE;
                    for (Agent savior : MainBoard.AgentColorGroups.get(obstacle.getColor())) {
                        int distance = RandomWalkClient.roomMaster.getPathEstimate(savior.getCoordinates(), obstacle.getCoordinates());
                        if(distance < closestDistance) {
                            closestAgent = savior;
                            closestDistance = distance;
                        }
                    }
                    closestAgent.scheduleObstacleRemoval(obstacle, closestDistance - point.timeFrame);
                    helpersDictionary.put(closestAgent, inNeed);
                }

            }
        }
        engine.immovableObstacles.clear();
    }
    public static void processObstacles(Agent owner, ArrayList<Obstacle> obstacles) {
        for(Obstacle current : obstacles) {
            if(!owner.getColor().equals(current.obstacle.getColor()) &&current.obstacle.assignedAgent == null ) {
                current.rescueAgent.scheduleObstacleRemoval(current.obstacle, current.pathLengthUntilObstacle);
                helpersDictionary.put(current.rescueAgent, owner);
                System.err.println("owner:" + owner + " BOX: " + obstacles);
                System.err.println("Rescue:" + current.rescueAgent + " BOX: " + current.obstacle);
                //throw new NullPointerException();
            }
        }
    }
    public static void jobIsDone(Agent savior) {
        Agent inTrouble = helpersDictionary.get(savior);
        if(inTrouble != null) {
            inTrouble.youShallPass();
        }
        else {
            throw new NegativeArraySizeException(); //hehe
        }
    }
}
