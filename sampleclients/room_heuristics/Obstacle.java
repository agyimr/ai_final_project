package sampleclients.room_heuristics;

import sampleclients.Agent;
import sampleclients.Box;

import java.awt.*;

public class Obstacle {
    public Box obstacle;
    public Agent rescueAgent;
    public Point waitingPosition;
    public int pathLengthUntilObstacle;

    Obstacle(Box obstacle, Agent rescueAgent, Point waitingPosition, int pathLengthUntilObstacle) {
        this.obstacle = obstacle;
        this.rescueAgent = rescueAgent;
        this.waitingPosition = waitingPosition;
        this.pathLengthUntilObstacle = pathLengthUntilObstacle;
    }

    @Override
    public String toString() {
        return "Box: " + obstacle.toString() + ", Rescue Agent: " + rescueAgent.toString() + ", Path length until obstacle: " +
                pathLengthUntilObstacle + ", Waiting Position: " + waitingPosition.toString();
    }
}
