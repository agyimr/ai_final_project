package sampleclients.room_heuristics;

import sampleclients.Box;

import java.awt.Point;
import java.util.ArrayList;

public class RoomNode {
    public Point position;
    public int h, g, f, punishment, p;
    public ArrayList<Obstacle> obstacles;
    public RoomNode parent;

    RoomNode(RoomNode parent, Point position, int g, int h, int punishment, ArrayList<Obstacle> obstacles) {
        this.parent = parent;
        this.position = position;
        this.h = h;
        this.punishment = punishment;
        this.g = g;
        this.p = g + punishment;
        this.f = h + g + punishment;
        this.obstacles = obstacles;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof RoomNode)) return false;
        RoomNode n = (RoomNode) o;
        return n.position.equals(this.position);
    }

    @Override
    public String toString() {
        return "X: " + position.x + ", Y: " + position.y + "; " + "Room: " + ", g: " + g + ", h: " + h + ", f: " + f +
                " Obstacles: \n" + obstacles.toString();
    }
}
