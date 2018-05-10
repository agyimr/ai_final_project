package sampleclients.room_heuristics;


import java.awt.*;
import java.util.ArrayList;

public class PathWithObstacles {
    public int distance;
    public ArrayList<Obstacle> obstacles;
    public Point arrivingPosition;

    PathWithObstacles(int distance, ArrayList<Obstacle> obstacles, Point arrivingPosition) {
        this.obstacles = obstacles;
        this.distance = distance;
        this.arrivingPosition = arrivingPosition;
    }
}