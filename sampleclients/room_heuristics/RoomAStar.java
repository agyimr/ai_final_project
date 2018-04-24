package sampleclients.room_heuristics;

import sampleclients.BasicObject;
import sampleclients.MainBoard;
import sampleclients.RandomWalkClient;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class RoomAStar {
    private Map map;
    public Passage passages;
    private ArrayList<Section> vertical_sections;
    private ArrayList<Section> horizontal_sections;

    public RoomAStar(MainBoard board) {
        PreProcessMap(board);
    }

    private void PreProcessMap(MainBoard board) {
        this.map = new Map(board.getGameBoard());
        this.vertical_sections = Divide.DivideMap(this.map, false);
        this.horizontal_sections = Divide.DivideMap(this.map, true);
        this.passages = new Passage(map, this.horizontal_sections, this.vertical_sections);
    }

    public LinkedList<Node> getRoomPath(Point from, Point to) {
        ArrayList<Node> closed_set = new ArrayList<>();
        PriorityQueue<Node> open_set = new PriorityQueue<>(10, Comparator.comparingInt((n) -> n.f));
        Node init_node = new Node(null, passages.section_map[from.y][from.x], from, null, 0, passages.getDistanceFrom(from, to));
        open_set.add(init_node);

        // main loop
        while(!open_set.isEmpty()) {

            Node current_node = open_set.poll();

            // checking for goal state
            for (Section s : current_node.sections) {
                if (s != null) {
                    if (s.contains(to)) {
                        return extractPlan(current_node, to, s);
                    }
                }
            }

            // putting current state to the closed set
            closed_set.add(current_node);

            // generating successor states
            ArrayList<Path> neighbour_sections = this.passages.getAllNeighbours(current_node.position);
            if (neighbour_sections == null) return null; // beginning section doesn't have any neighbours AND goal is not in there.

            for(Path s : neighbour_sections) {
                int travel_distance = s.to.getDistanceFromPoint(current_node.position);
                Point p = s.to.getClosestPoint(current_node.position);
                Node n = new Node(current_node, passages.section_map[p.y][p.x], p, s.through,
                        current_node.g + travel_distance, passages.getDistanceFrom(p, to));

                // if not in frontier yet or not explored yet...
                if (!open_set.contains(n) && !closed_set.contains(n)) {
                    open_set.add(n);
                }
            }
        }

        return null;
    }

    private LinkedList<Node> extractPlan(Node goal_node, Point to, Section goal_section) {
        // System.err.print(goal_node.g + this.getDistance(goal_node.position, to) + "\n");
        LinkedList<Node> plan = new LinkedList<>();
        Node goal = new Node(goal_node, this.passages.section_map[to.y][to.x], to, goal_section,
                goal_node.g + this.getDistance(goal_node.position, to), 0);
        plan.addFirst(goal);
        Node n = goal_node;
        while (n.parent != null) {
            plan.addFirst(n);
            n = n.parent;
        }
        return plan;
    }
    public int getPathEstimate(Point originCoordinates, Point goalCoordinates) {
        LinkedList<sampleclients.room_heuristics.Node> result = getRoomPath(originCoordinates, goalCoordinates);
        if(result == null) return Integer.MAX_VALUE;
        else return result.	pollLast().g;

    }
    private int getDistance(Point from, Point to) {
        return Math.abs(from.x - to.x) + Math.abs(from.y - to.y);
    }
}
