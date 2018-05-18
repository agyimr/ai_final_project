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
    public static HashMap<Agent, HashSet<Agent>> additionalRescue = initializeAdditionalRescue();
    private static HashMap<Agent, HashSet<Agent>> initializeAdditionalRescue() {
        HashMap<Agent, HashSet<Agent>> dict = new HashMap<>();
        for(Agent agent: MainBoard.agents) {
            dict.put(agent, new HashSet<>());
        }
        return dict;
    }
    public static Point processObstacles(Agent owner, ArrayList<Obstacle> obstacles) {
        Point anythingProcessed = null;
        for(Obstacle current : obstacles) {
            if(owner.isMyBox(current.obstacle)) continue;
            if(agentDictionary.containsKey(current.obstacle)) {
                scheduledAgents currentSchedule = agentDictionary.get(current.obstacle);
                if(anythingProcessed == null) {
                    if(currentSchedule.savior == owner) {
                        owner.changeObstacle(current.obstacle);
                    }
                    else {
                        owner.rescueIsNotNeeded(); //TODO rescue is needed, this information has to be stored in scheduled obstacles
                    }
                    anythingProcessed = current.waitingPosition;
                }
                if(currentSchedule.inTrouble != owner && currentSchedule.savior != null && currentSchedule.savior != owner) {
                    additionalRescue.get(currentSchedule.savior).add(owner);
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
                        if(true) {
                            agentDictionary.put(current.obstacle, new scheduledAgents(current.rescueAgent, owner));
                            current.rescueAgent.forceObstacleRemoval(current.obstacle, owner, current.pathLengthUntilObstacle);
                        }

                        anythingProcessed = current.waitingPosition;
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
                agentDictionary.put(current.obstacle, new scheduledAgents(owner, owner));
                if(anythingProcessed == null) {
                    anythingProcessed = current.waitingPosition;
                    owner.forceObstacleRemoval(current.obstacle, owner, 0);

                }
                else {
                    owner.scheduleObstacleRemoval(current.obstacle, owner, current.pathLengthUntilObstacle);
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
                System.err.println(savior);
                System.err.println(inTrouble);
                System.err.println(savior.getAttachedBox());
                //throw new NegativeArraySizeException();
            }
            System.err.println("After removal: " + agentDictionary);
        }
        else {
            throw new NullPointerException();
        }
        if(inTrouble != null) {
            inTrouble.youShallPass();
            for(Agent additionals : additionalRescue.get(savior)) {
                additionals.youShallPass();
            }
            additionalRescue.get(savior).clear();
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
