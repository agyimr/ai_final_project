import java.awt.*;
import java.util.UUID;

public class Section {
    public Point p1; // top left
    public Point p2; // bottom right
    public String id;

    Section(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.id = UUID.randomUUID().toString();
    }

    public int getDistanceFromPoint(Point from) {
        if (from.x < p1.x && from.y >= p1.y && from.y <= p2.y) return p1.x - from.x;
        if (from.x < p1.x && from.y < p1.y) return p1.x - from.x + p1.y - from.y;
        if (from.x < p1.x && from.y > p2.y) return p1.x - from.x + from.y - p2.y;
        if (from.x >= p1.x && from.x <= p2.x && from.y < p1.y) return p1.y - from.y;
        if (from.x >= p1.x && from.x <= p2.x && from.y > p2.y) return from.y - p2.y;
        if (from.x > p2.x && from.y < p1.y) return from.x - p2.x + p1.y - from.y;
        if (from.x > p2.x && from.y >= p1.y && from.y <= p2.y) return from.x - p2.x;
        if (from.x > p2.x && from.y > p2.y) return from.x - p2.x + from.y - p2.y;
        return 0; // in case it's inside the section
    }

    public Point getClosestPoint(Point from) {
        if (from.x < p1.x && from.y >= p1.y && from.y <= p2.y) return new Point(p1.x, from.y);
        if (from.x < p1.x && from.y < p1.y) return p1;
        if (from.x < p1.x && from.y > p2.y) return new Point(p1.x, p2.y);
        if (from.x >= p1.x && from.x <= p2.x && from.y < p1.y) return new Point(from.x, p1.y);
        if (from.x >= p1.x && from.x <= p2.x && from.y > p2.y) return new Point(from.x, p2.y);
        if (from.x > p2.x && from.y < p1.y) return new Point(p2.x, p1.y);
        if (from.x > p2.x && from.y >= p1.y && from.y <= p2.y) return new Point(p2.x, from.y);
        if (from.x > p2.x && from.y > p2.y) return p2;
        return from; // in case it's inside the section
    }

    public boolean contains(Point p) {
        return p.x >= p1.x && p.y >= p1.y && p.x <= p2.x && p.y <= p2.y;
    }

    public boolean contains(Section s) {
        return  (this.p1.x <= s.p1.x) &&
                (this.p1.y <= s.p1.y) &&
                (this.p2.x >= s.p2.x) &&
                (this.p2.y >= s.p2.y);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Section)) return false;
        Section s = (Section) o;
        return s.id.equals(this.id);
    }


}
