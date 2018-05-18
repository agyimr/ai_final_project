package sampleclients;

import sampleclients.room_heuristics.Obstacle;
import sampleclients.room_heuristics.PathWithObstacles;

import java.awt.*;
import java.util.*;

import static sampleclients.RandomWalkClient.gameBoard;

public class ObstacleArbitrator {
    private static class scheduledAgents {
        Agent savior;
        Agent inTrouble;
        public scheduledAgents(Agent savior, Agent inTrouble) {
            this.savior = savior;
            this.inTrouble = inTrouble;
        }

        @Override
        public String toString() {
            return "scheduledAgents{" +
                    "savior=" + savior +
                    ", inTrouble=" + inTrouble +
                    '}';
        }
    }
    public static HashMap<Box, scheduledAgents> agentDictionary = new HashMap<>();
    public static Point processObstacles(Agent owner, ArrayList<Obstacle> obstacles) {
        Point anythingProcessed = null;
        for(Obstacle current : obstacles) {
            if(owner.isMyBox(current.obstacle)) continue;
            if(agentDictionary.containsKey(current.obstacle)) {
                if(anythingProcessed == null) {
                    if(agentDictionary.get(current.obstacle).savior == owner) {
                        owner.changeObstacle(current.obstacle);
                    }
                    anythingProcessed = current.waitingPosition;
                    owner.rescueIsNotNeeded(); //TODO rescue is needed, this information has to be stored in scheduled obstacles
                }
                continue;
            }
            if(!owner.getColor().equals(current.obstacle.getColor())) {
                if(!current.obstacle.isBeingMoved()) {
                    agentDictionary.put(current.obstacle, new scheduledAgents(current.rescueAgent, owner));
                    if(owner.inTrouble == current.rescueAgent) {
                        current.rescueAgent.forceObstacleRemoval(current.obstacle, owner, current.pathLengthUntilObstacle);
                    }
                    else {
                        current.rescueAgent.scheduleObstacleRemoval(current.obstacle, owner, current.pathLengthUntilObstacle);
                    }
                }
                if(anythingProcessed == null) {
                    if(current.obstacle.isBeingMoved()) {
                        agentDictionary.put(current.obstacle, new scheduledAgents(current.rescueAgent, owner));
                        //anythingProcessed = FindSafeSpot.safeSpotBFS(current.waitingPosition);
                        //if(anythingProcessed == null) {
                        current.rescueAgent.forceObstacleRemoval(current.obstacle, owner, current.pathLengthUntilObstacle);
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
                System.err.println("currently scheduled obstacles: " + agentDictionary);
                System.err.println("Rescue:" + current.rescueAgent + " BOX: " + current.obstacle);
                System.err.println("waiting position: " + anythingProcessed);
                //throw new NullPointerException();
            }
            else {
                agentDictionary.put(current.obstacle, new scheduledAgents(current.rescueAgent, owner));
                if(anythingProcessed == null) {
                    anythingProcessed = current.waitingPosition;
                    if(MainBoard.singleAgentMap) {

                    }
                    else {
                        owner.forceObstacleRemoval(current.obstacle, owner, 0);
                    }
                }
            }
        }
        return anythingProcessed;
    }
    public static void jobIsDone(Agent savior, Agent inTrouble) {
        System.err.println("job is done!\n\n");
        //Agent inTrouble = helpersDictionary.get(savior);
        if(savior.isBoxAttached()) {
            System.err.println("Obstacles before removal: " + agentDictionary);
            if(agentDictionary.remove(savior.getAttachedBox()) == null) {
                throw new NegativeArraySizeException();
            }
            System.err.println("After removal: " + agentDictionary);
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
