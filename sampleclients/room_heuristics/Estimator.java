package sampleclients.room_heuristics;

import sampleclients.*;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Estimator {

    public static PathWithObstacles estimatePath(Point from, Section to, Section through, int beginning_path_length) {
        Point closest_point = to.getClosestPoint(from);
        if (!containsBoxes(through) && !RandomWalkClient.gameBoard.isBox(closest_point.x, closest_point.y)) {
            return new PathWithObstacles(to.getDistanceFromPoint(from), new ArrayList<>(), closest_point);
        }
        RoomNode goal_node = search(from, to, through, beginning_path_length);
        if (goal_node == null) return null;
        return new PathWithObstacles(goal_node.g - beginning_path_length, goal_node.obstacles, goal_node.position);
    }

    public static PathWithObstacles estimatePath(Point from, Point to, Section through, int beginning_path_length) {
        if (!containsBoxes(through) && !RandomWalkClient.gameBoard.isBox(to.x, to.y)) {
            return new PathWithObstacles(getDistance(to, from), new ArrayList<>(), to);
        }
        RoomNode goal_node = search(from, new Section(to, to), through, beginning_path_length);
        if (goal_node == null) return null;
        return new PathWithObstacles(goal_node.g - beginning_path_length, goal_node.obstacles, to);
    }

    private static RoomNode search(Point from, Section to, Section through, int beginning_path_length) {
        MainBoard map = RandomWalkClient.gameBoard;
        ArrayList<RoomNode> closed_set = new ArrayList<>();
        PriorityQueue<RoomNode> open_set = new PriorityQueue<>(10, Comparator.comparingInt((n) -> n.f));

        // initial node
        RoomNode initial_node = new RoomNode(null, from, beginning_path_length, to.getDistanceFromPoint(from), new ArrayList<>());
        open_set.add(initial_node);
        RoomNode goalNode = null;
        boolean explore;

        // search
        while(!open_set.isEmpty()) {
            explore = true;
            RoomNode current_node = open_set.poll();

            // leave search only if all the possible open set nodes are worse than our solution
            if (goalNode != null && current_node.g > goalNode.g) {
                return goalNode;
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
                        AgentBoxDistance helper_agent = getClosestFreeAgent(box);

                        if (helper_agent != null) {
                            ArrayList<Obstacle> obstacles = new ArrayList<>(current_node.obstacles);

                            int waiting_time = 0;
                            int path_length_until_box = current_node.g;
                            if (path_length_until_box < helper_agent.distance) {
                                // If we want to take into account the agent's job here is the chance
                                waiting_time = helper_agent.distance - path_length_until_box;
                            }

                            obstacles.add(new Obstacle(box, helper_agent.a, current_node.position, waiting_time));

                            RoomNode n = new RoomNode(current_node, neighbour,
                                    current_node.g + waiting_time + 1,
                                    to.getDistanceFromPoint(neighbour), obstacles);

                            if (!open_set.contains(n) && !closed_set.contains(n)) {
                                open_set.add(n);
                            }
                        }
                    } else {
                        RoomNode n = new RoomNode(current_node, neighbour, current_node.g + 1,
                                to.getDistanceFromPoint(neighbour), current_node.obstacles);
                        if (!open_set.contains(n) && !closed_set.contains(n)) {
                            open_set.add(n);
                        }
                    }
                }
            }
        }

        if (goalNode != null) return goalNode;
        return null;
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

    private static AgentBoxDistance getClosestFreeAgent(Box box) {
        List<Agent> agents = RandomWalkClient.gameBoard.AgentColorGroups.get(box.getColor());

        AgentBoxDistance abd = new AgentBoxDistance(null, null, Integer.MAX_VALUE);

        for(Agent a : agents) {
            //if (a.isAvailableToHelp()) {

            // Estimate agent distance with empty room heuristics
            int distance = RandomWalkClient.roomMaster.getEmptyPathEstimate(a.getCoordinates(), box.getCoordinates());

            // Estimate agent distance with manhattan distance
            // int distance = getDistance(a.getCoordinates(), box.getCoordinates());
            if (distance != Integer.MAX_VALUE && distance < abd.distance) {
                abd.distance = distance;
                abd.a = a;
                abd.b = box;
            }
            //}
        }
        if (abd.distance == Integer.MAX_VALUE) return null;
        return abd;
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

class AgentBoxDistance {
    Agent a;
    Box b;
    int distance;

    AgentBoxDistance(Agent a, Box b, int distance) {
        this.a = a;
        this.b = b;
        this.distance = distance;
    }
}
