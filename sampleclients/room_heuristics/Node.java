import java.awt.Point;
import java.util.Arrays;

public class Node {
    public Node parent;
    public Section[] sections;
    public Point position;
    public Section through;
    public int f;
    public int g;
    public int h;

    Node(Node parent, Section[] sections, Point position, Section through, int g, int h) {
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
        return n.position.equals(this.position);
    }

    @Override
    public String toString() {
        if (this.through != null ) {
            return "X: " + position.x + ", Y: " + position.y + "\n" + "Room: " + this.through.id.charAt(0) + "\n" ;
        }
        return "X: " + position.x + ", Y: " + position.y + "\n" + "Room: start" + "\n";
    }
}
