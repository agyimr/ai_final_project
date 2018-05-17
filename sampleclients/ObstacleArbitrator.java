package sampleclients;

import sampleclients.room_heuristics.Obstacle;
import sampleclients.room_heuristics.PathWithObstacles;

import java.awt.*;
import java.util.*;

import static sampleclients.RandomWalkClient.gameBoard;

public class ObstacleArbitrator {
    public static Set<Box> scheduledObstacles= new HashSet<>();

    public static Point processObstacles(Agent owner, ArrayList<Obstacle> obstacles) {
        Point anythingProcessed = null;
        for(Obstacle current : obstacles) {
            if(!owner.getColor().equals(current.obstacle.getColor())) {
                if(scheduledObstacles.contains(current.obstacle)) {
                    continue;
                }
                if(!current.obstacle.isBeingMoved()) {
                    scheduledObstacles.add(current.obstacle);
                    if(owner.inTrouble == current.rescueAgent) {
                        current.rescueAgent.forceObstacleRemoval(current.obstacle, owner, current.pathLengthUntilObstacle);
                    }
                    else {
                        current.rescueAgent.scheduleObstacleRemoval(current.obstacle, owner, current.pathLengthUntilObstacle);
                    }
                }
                if(anythingProcessed == null) {
                    if(current.obstacle.isBeingMoved()) {
                        //anythingProcessed = FindSafeSpot.safeSpotBFS(current.waitingPosition);
                        //if(anythingProcessed == null) {
                        anythingProcessed = current.waitingPosition;
                        //}
                        owner.rescueIsNotNeeded();
                    }
                    else {
                        //anythingProcessed = FindSafeSpot.safeSpotBFS(owner.getCoordinates()); //TODO experimantal
                        if(anythingProcessed == null) anythingProcessed = current.waitingPosition;
                    }

                }
                System.err.println("owner:" + owner + "Offset: " + current.pathLengthUntilObstacle);
                System.err.println("currently scheduled obstacles: " + scheduledObstacles);
                System.err.println("Rescue:" + current.rescueAgent + " BOX: " + current.obstacle);
                System.err.println("waiting position: " + anythingProcessed);
                //throw new NullPointerException();
            }
        }
        return anythingProcessed;
    }
    public static void jobIsDone(Agent savior, Agent inTrouble) {
        System.err.println("job is done!\n\n");
        //Agent inTrouble = helpersDictionary.get(savior);
        if(savior.isBoxAttached()) {
            System.err.println("Obstacles before removal: " + scheduledObstacles);
            scheduledObstacles.remove(savior.getAttachedBox());
            System.err.println("After removal: " + scheduledObstacles);
        }
        else {
            throw new NullPointerException();
        }
        if(inTrouble != null) {
            inTrouble.youShallPass();
//            if(!inTrouble.obstacles.isEmpty()){
//                throw new NullPointerException();
//            }
        }
        else {
            //you're not the one to free him, boi
            //throw new NegativeArraySizeException(); //hehe
        }
    }
}
