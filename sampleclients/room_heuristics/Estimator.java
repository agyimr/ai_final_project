package sampleclients.room_heuristics;

import sampleclients.*;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Estimator {
    static int STRICT_PUNISHMENT_FOR_GOAL_DESTRUCTION = 10;
    static int STRICT_PUNISHMENT_FOR_AGENT_USAGE = 10;
    static int MILD_PUNISHMENT_FOR_AGENT_USAGE = 5;
    static int MILD_PUNISHMENT_FOR_SAME_COLOR_BOX_PUSHING = 3;

    public static PathWithObstacles estimatePath(Point from, Section to, Section through,
                                                 int beginning_path_length, String agentColor) {
        Point closest_point = to.getClosestPoint(from);
        if (!containsBoxes(through) && !RandomWalkClient.gameBoard.isBox(closest_point.x, closest_point.y)) {
            return new PathWithObstacles(to.getDistanceFromPoint(from), 0, new ArrayList<>(), closest_point);
        }

        RoomNode goal_node = search(from, to, through, beginning_path_length, agentColor);
        if (goal_node == null) return null;

        return new PathWithObstacles(goal_node.g - beginning_path_length, goal_node.punishment,
                goal_node.obstacles, goal_node.position);
    }

    public static PathWithObstacles estimatePath(Point from, Point to, Section through,
                                                 int beginning_path_length, String agentColor) {
        if (!containsBoxes(through) && !RandomWalkClient.gameBoard.isBox(to.x, to.y)) {
            return new PathWithObstacles(getDistance(to, from), 0, new ArrayList<>(), to);
        }

        RoomNode goal_node = search(from, new Section(to, to), through, beginning_path_length, agentColor);
        if (goal_node == null) return null;

        return new PathWithObstacles(goal_node.g - beginning_path_length, goal_node.punishment,
                goal_node.obstacles, to);
    }

    private static RoomNode search(Point from, Section to, Section through,
                                   int beginning_path_length, String agentColor) {
        MainBoard map = RandomWalkClient.gameBoard;
        ArrayList<RoomNode> closed_set = new ArrayList<>();
        PriorityQueue<RoomNode> open_set = new PriorityQueue<>(10, Comparator.comparingInt((n) -> n.f));

        // initial node
        RoomNode initial_node = new RoomNode(null, from, beginning_path_length, to.getDistanceFromPoint(from),
                0, new ArrayList<>());
        open_set.add(initial_node);

        // search
        while(!open_set.isEmpty()) {
            RoomNode current_node = open_set.poll();

            // leave search at the first time we encounter a goal node (greedy behaviour)
            if (to.contains(current_node.position)) {
                return current_node;
            }

            closed_set.add(current_node);

            ArrayList<Point> neighbours = getValidNeighbours(current_node.position, through, to);
            for (Point neighbour: neighbours) {
                if (map.isBox(neighbour.x, neighbour.y)) {
                    Box box = (Box)map.getElement(neighbour.x, neighbour.y);
                    AgentBoxDistance helper_agent = getClosestFreeAgent(box, current_node.g, agentColor);

                    if (helper_agent != null) { // if obstacle is movable...
                        ArrayList<Obstacle> obstacles = new ArrayList<>(current_node.obstacles);

                        int path_length_until_box = current_node.g;

                        // calculating waiting position
                        Point waiting_point = null;
                        boolean found = false;
                        RoomNode tmp = current_node;
                        while (tmp != null && !found) {
                            // if free or box with same color...
                            if (tmp.position != null &&
                                    (RandomWalkClient.gameBoard.isFree(tmp.position.x, tmp.position.y) ||
                                            (RandomWalkClient.gameBoard.isBox(tmp.position.x, tmp.position.y) &&
                                                    ((Box)RandomWalkClient.gameBoard.getElement(tmp.position.x, tmp.position.y)).getColor().equals(agentColor)))) {
                                found = true;
                                waiting_point = tmp.position;
                            }
                            tmp = tmp.parent;
                        }
                        // if no suitable position found, fall back to the one right before the obstacle
                        if (!found) waiting_point = current_node.position;
                        obstacles.add(new Obstacle(box, helper_agent.a, waiting_point, path_length_until_box));


                        // calculating punishment
                        int punishment;
                        if (map.getGoal(neighbour.x, neighbour.y) != null &&
                                map.getGoal(neighbour.x, neighbour.y).solved()) punishment = STRICT_PUNISHMENT_FOR_GOAL_DESTRUCTION;
                        else if (!helper_agent.sameColor) {
                            punishment = helper_agent.a.isJobless() || box.isBeingMoved()
                                ? MILD_PUNISHMENT_FOR_AGENT_USAGE
                                : STRICT_PUNISHMENT_FOR_AGENT_USAGE;
                        } else punishment = MILD_PUNISHMENT_FOR_SAME_COLOR_BOX_PUSHING;


                        RoomNode n = new RoomNode(current_node, neighbour,
                                current_node.g + 1, to.getDistanceFromPoint(neighbour),
                                current_node.punishment + punishment, obstacles);

                        if (!open_set.contains(n) && !closed_set.contains(n)) {
                            open_set.add(n);
                        }
                    }
                } else {
                    RoomNode n = new RoomNode(current_node, neighbour, current_node.g + 1,
                            to.getDistanceFromPoint(neighbour), current_node.punishment, current_node.obstacles);
                    if (!open_set.contains(n) && !closed_set.contains(n)) {
                        open_set.add(n);
                    }
                }
            }
        }

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

    private static AgentBoxDistance getClosestFreeAgent(Box box, int path_length, String agentColor) {
        if (box.getColor().equals(agentColor)) return new AgentBoxDistance(null, box, 0, true);
        List<Agent> agents = MainBoard.AgentColorGroups.get(box.getColor());
        if (agents == null) return null;
        AgentBoxDistance abd = new AgentBoxDistance(null, null, Integer.MAX_VALUE, false);

        for(Agent a : agents) {
            // Estimate helper agent distance with manhattan distance
            //int distance = getDistance(a.getCoordinates(), box.getCoordinates());
            // Estimate agent distance with empty room heuristics
            int distance = RandomWalkClient.roomMaster.getEmptyPathEstimate(a.getCoordinates(), box.getCoordinates());

            if (distance != Integer.MAX_VALUE) {
                if (abd.a != null) {
                    if (abd.a.isJobless()) {
                        if (distance < abd.distance && a.isJobless()) {
                            abd.a = a;
                            abd.distance = distance;
                            abd.b = box;
                        }
                    } else {
                        if (a.isJobless() && distance < path_length) {
                            abd.a = a;
                            abd.distance = distance;
                            abd.b = box;
                        } else if (!a.isJobless() && distance < abd.distance) {
                            abd.a = a;
                            abd.distance = distance;
                            abd.b = box;
                        }
                    }
                } else {
                    abd.a = a;
                    abd.distance = distance;
                    abd.b = box;
                }
            }
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
    boolean sameColor;

    AgentBoxDistance(Agent a, Box b, int distance, boolean sameColor) {
        this.a = a;
        this.b = b;
        this.distance = distance;
        this.sameColor = sameColor;
    }
}
