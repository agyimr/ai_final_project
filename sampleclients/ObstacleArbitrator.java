package sampleclients;

import sampleclients.room_heuristics.Obstacle;
import sampleclients.room_heuristics.PathWithObstacles;

import java.awt.*;
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
    public static Point processObstacles(Agent owner, ArrayList<Obstacle> obstacles) {
        Point anythingProcessed = null;
        for(Obstacle current : obstacles) {
            if(!owner.getColor().equals(current.obstacle.getColor())) {
                if(anythingProcessed == null) {
                    anythingProcessed = current.waitingPosition;

                    helpersDictionary.put(current.rescueAgent, owner);
                }
                else {
                    helpersDictionary.put(current.rescueAgent, null);
                }
                owner.obstacles.add(current.obstacle);
                current.rescueAgent.scheduleObstacleRemoval(current.obstacle, current.pathLengthUntilObstacle);

                System.err.println("owner:" + owner + "Offset: " + current.pathLengthUntilObstacle);
                System.err.println("currently disclosed obstacles: " + owner.obstacles);
                System.err.println("Rescue:" + current.rescueAgent + " BOX: " + current.obstacle);
                //throw new NullPointerException();
            }
        }
        return anythingProcessed;
    }
    public static void jobIsDone(Agent savior) {
        System.err.println("job is done!\n\n");
        Agent inTrouble = helpersDictionary.get(savior);


        if(inTrouble != null) {
            inTrouble.youShallPass();
            System.err.println();
            System.err.println(inTrouble.obstacles);
            if(savior.isBoxAttached()) {
                inTrouble.obstacles.remove(savior.getAttachedBox());
            }
            System.err.println("After removing!\n\n");
            System.err.println(inTrouble.obstacles);
            System.err.println(savior.safeSpot);
//            throw new NullPointerException();
        }
        else {
            //TODO you're not the one to free him, boi
            //throw new NegativeArraySizeException(); //hehe
        }
    }
}
