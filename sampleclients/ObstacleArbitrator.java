package sampleclients;

import sampleclients.room_heuristics.Obstacle;
import sampleclients.room_heuristics.PathWithObstacles;

import java.awt.*;
import java.util.*;

import static sampleclients.RandomWalkClient.gameBoard;

public class ObstacleArbitrator {
    public static Map<Agent, Agent> helpersDictionary= new HashMap<>();

    public static Point processObstacles(Agent owner, ArrayList<Obstacle> obstacles) {
        Point anythingProcessed = null;
        for(Obstacle current : obstacles) {
            if(owner.getAttachedBox() != current.obstacle) {
                owner.obstacles.add(current.obstacle);
                if(!current.obstacle.isBeingMoved()) {
                    helpersDictionary.put(current.rescueAgent, owner);
                    current.rescueAgent.scheduleObstacleRemoval(current.obstacle, current.pathLengthUntilObstacle);
                }
                if(anythingProcessed == null) {
                    if(current.obstacle.isBeingMoved()) {
                        anythingProcessed = FindSafeSpot.safeSpotBFS(current.waitingPosition);
                        owner.rescueIsNotNeeded();
                    }
                    else {
                        anythingProcessed = current.waitingPosition;
                    }

                }
                else {
                    helpersDictionary.put(current.rescueAgent, null);
                }
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
