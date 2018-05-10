package sampleclients.room_heuristics;

import java.awt.*;
import java.util.ArrayList;

public class Node {
    public ArrayList<Obstacle> obstacles;
    public Node parent;
    public Section[] sections;
    public Point position;
    public Section through;
    public int f;
    public int g;
    public int h;

    Node(Node parent, Section[] sections, Point position, Section through, int g, int h, ArrayList<Obstacle> obstacles) {
        this.obstacles = obstacles;
        this.through = through;
        this.parent = parent;
        this.sections = sections;
        this.position = position;
        this.h = h;
        this.g = g;
        this.f = this.h + this.g;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Node)) return false;
        Node n = (Node) o;
        return n.position.equals(this.position);// && n.g == g;
    }

    @Override
    public String toString() {
        if (this.through != null ) {
            return "X: " + position.x + ", Y: " + position.y + "; " + "Room: " +
                    this.through.id.charAt(0) + ", g: " + g + ", h: " + h + ", f: " + f +
                    " Obstacles: \n" + obstacles.toString();
        }
        return "X: " + position.x + ", Y: " + position.y + "\n" + "Room: start";
    }
}
