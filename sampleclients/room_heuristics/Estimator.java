package sampleclients.room_heuristics;

import sampleclients.*;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Estimator {

    public static int estimatePathLength(Point from, Section to, Section through) {
        Point closest_point = to.getClosestPoint(from);
        if (!containsBoxes(through) && !RandomWalkClient.gameBoard.isBox(closest_point.x, closest_point.y)) {
            return to.getDistanceFromPoint(from);
        }
        int length = search(from, to, through);
        //System.err.println("From " + from.toString() + ", to " + to.id.charAt(0) + ", through " + through.id.charAt(0) + " :   " + length);
        return length;
    }

    private static int search(Point from, Section to, Section through) {
        MainBoard map = RandomWalkClient.gameBoard;
        ArrayList<RoomNode> closed_set = new ArrayList<>();
        PriorityQueue<RoomNode> open_set = new PriorityQueue<>(10, Comparator.comparingInt((n) -> n.f));

        // initial node
        RoomNode initial_node = new RoomNode(null, from, 0, to.getDistanceFromPoint(from), new ArrayList<Box>());
        open_set.add(initial_node);
        RoomNode goalNode = null;
        boolean explore;

        // search
        while(!open_set.isEmpty()) {
            explore = true;
            RoomNode current_node = open_set.poll();

            // leave search only if all the possible open set nodes are worse than our solution
            if (goalNode != null && current_node.g > goalNode.g) {
                return goalNode.g;
            }

            // if a solution found, check whether it's better than our current one
            if (to.contains(current_node.position)) {
                if (goalNode == null) goalNode = current_node;
                if (goalNode != null && goalNode.g > current_node.g) goalNode = current_node;
                explore = false;
            }

            closed_set.add(current_node);

            if (explore) {
                ArrayList<Point> neighbours = getValidNeighbours(current_node.position, through, to);
                for (Point neighbour: neighbours) {
                    if (map.isBox(neighbour.x, neighbour.y)) {
                        Box box = (Box)map.getElement(neighbour.x, neighbour.y);
                        int help_manhattan_distance_estimate = getClosestAgentDistance(box);
                        if (help_manhattan_distance_estimate != -1) {
                            ArrayList<Box> boxes = new ArrayList<>(current_node.boxList);
                            boxes.add(box);
                            RoomNode n = new RoomNode(current_node, neighbour,
                                    current_node.g + help_manhattan_distance_estimate + 1,
                                    to.getDistanceFromPoint(neighbour), boxes);

                            if (!open_set.contains(n) && !closed_set.contains(n)) {
                                open_set.add(n);
                            }
                        }
                    } else {
                        RoomNode n = new RoomNode(current_node, neighbour, current_node.g + 1,
                                to.getDistanceFromPoint(neighbour), current_node.boxList);
                        if (!open_set.contains(n) && !closed_set.contains(n)) {
                            open_set.add(n);
                        }
                    }
                }
            }
        }

        if (goalNode != null) return goalNode.g;
        return -1;
    }

    private static ArrayList<Point> getValidNeighbours(Point position, Section through, Section to) {
        ArrayList<Point> neighbours = new ArrayList<>();
        ArrayList<Point> potential_neighbours = new ArrayList<>();

        potential_neighbours.add(new Point(position.x + 1, position.y));
        potential_neighbours.add(new Point(position.x - 1, position.y));
        potential_neighbours.add(new Point(position.x, position.y + 1));
        potential_neighbours.add(new Point(position.x, position.y - 1));

        for (Point p : potential_neighbours) {
            if (through.contains(p) || to.contains(p)) neighbours.add(p);
        }

        return neighbours;
    }

    private static int getClosestAgentDistance(Box box) {
        List<Agent> agents = RandomWalkClient.gameBoard.AgentColorGroups.get(box.getColor());
        int smallest_distance = Integer.MAX_VALUE;
        for(Agent a : agents) {
            int distance = getDistance(a.getCoordinates(), box.getCoordinates());
            if (distance < smallest_distance) {
                smallest_distance = distance;
            }
        }
        if (smallest_distance == Integer.MAX_VALUE) return -1;
        return smallest_distance;
    }

    private static int getDistance(Point a, Point b) {
        return (Math.abs(a.x - b.x) + Math.abs(a.y - b.y));
    }

    private static boolean containsBoxes(Section s) {
        int number_of_positions = (s.p2.x - s.p1.x) * (s.p2.y - s.p1.y);
        if (number_of_positions < RandomWalkClient.gameBoard.allBoxes.size()) {
            // check every position whether it is a box or not...
            for (int x = s.p1.x; x <= s.p2.x; ++x) {
                for (int y = s.p1.y; y <= s.p2.y; ++y) {
                    if (RandomWalkClient.gameBoard.isBox(x, y)) return true;
                }
            }
        } else {
            for (Box b : RandomWalkClient.gameBoard.allBoxes) {
                if (s.contains(b.getCoordinates())) return true;
            }
        }
        return false;
    }
}
