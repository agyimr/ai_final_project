package sampleclients.room_heuristics;


import java.awt.*;
import java.util.ArrayList;

public class PathWithObstacles {
    public int distance;
    public int punishment;
    public ArrayList<Obstacle> obstacles;
    public Point arrivingPosition;

    PathWithObstacles(int distance, int punishment, ArrayList<Obstacle> obstacles, Point arrivingPosition) {
        this.obstacles = obstacles;
        this.punishment = punishment;
        this.distance = distance;
        this.arrivingPosition = arrivingPosition;
    }
}