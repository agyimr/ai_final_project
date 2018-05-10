package sampleclients.room_heuristics;

import sampleclients.Agent;
import sampleclients.Box;

import java.awt.*;

public class Obstacle {
    public Box obstacle;
    public Agent rescueAgent;
    public Point waitingPosition;
    public int estimatedWaitingTime;

    Obstacle(Box obstacle, Agent rescueAgent, Point waitingPosition, int estimatedWaitingTime) {
        this.obstacle = obstacle;
        this.rescueAgent = rescueAgent;
        this.waitingPosition = waitingPosition;
        this.estimatedWaitingTime = estimatedWaitingTime;
    }

    @Override
    public String toString() {
        return "Box: " + obstacle.toString() + ", Rescue Agent: " + rescueAgent.toString() + ", Waiting time: " +
                estimatedWaitingTime + ", Waiting Position: " + waitingPosition.toString();
    }
}
