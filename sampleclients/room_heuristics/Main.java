import java.awt.Point;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {
        RoomAStar a_star = new RoomAStar("MAHALnineK");
        a_star.passages.PrintMap();

        Point start = new Point(21, 1);
        Point finish = new Point(23, 46);
        LinkedList<Node> path = a_star.getShortestPath(start, finish);

        for (Node n: path) { System.out.print(n.toString()); }
    }
}
