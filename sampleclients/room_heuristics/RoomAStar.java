package sampleclients.room_heuristics;

import sampleclients.MainBoard;

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

        boolean explore;
        LinkedList<Node> solution = new LinkedList<>();
        // main loop
        while(!open_set.isEmpty()) {
            explore = true;

            Node current_node = open_set.poll();

            if (!solution.isEmpty() && current_node.g > solution.getLast().g) {
                return solution;
            }

            // checking for goal state
            for (Section s : current_node.sections) {
                if (s != null) {
                    if (s.contains(to)) {
                        LinkedList<Node> path = extractPlan(current_node, to, s);
                        // if first time we find a solution
                        if (solution.isEmpty()) {
                            solution = path;
                        }
                        // if we find a better solution than we already have...
                        if (!solution.isEmpty() && !path.isEmpty() && path.getLast().g < solution.getLast().g) {
                            solution = path;
                        }
                        explore = false;
                    }
                }
            }

            // putting current state to the closed set
            closed_set.add(current_node);

            if (explore) {
                // generating successor states
                ArrayList<Path> neighbour_sections = this.passages.getAllNeighbours(current_node.position);
                if (neighbour_sections == null) return null; // beginning section doesn't have any neighbours AND goal is not in there.

                for(Path s : neighbour_sections) {
                    int distance = Estimator.estimatePathLength(current_node.position, s.to, s.through);
                    if (distance != -1) { // if path exists...
                        Point p = s.to.getClosestPoint(current_node.position);
                        //System.err.println(distance);
                        Node n = new Node(current_node, passages.section_map[p.y][p.x], p, s.through,
                                current_node.g + distance, passages.getDistanceFrom(p, to));

                        // if not in frontier yet or not explored yet...
                        if (!open_set.contains(n) && !closed_set.contains(n)) {
                            open_set.add(n);
                        }
                    }
                }
            }
        }

        if (!solution.isEmpty()) return solution;
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
