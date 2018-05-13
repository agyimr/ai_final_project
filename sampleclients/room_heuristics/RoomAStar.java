package sampleclients.room_heuristics;

import sampleclients.MainBoard;

import java.awt.Point;
import java.util.*;

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

    public ArrayList<Obstacle> getObstacles(Point from, Point to) {
        if (from.equals(to)) return new ArrayList<>();
        LinkedList<Node> path = this.getRoomPath(from, to);
        if (path == null) return null; // meaning there is no possible path there.
        if (path.size() == 0) throw new NullPointerException("Something is really wrong...");
        return path.getLast().obstacles;
    }

    public ArrayList<Obstacle> getObstacles(Point from, Section to) {
        if (to.contains(from)) return new ArrayList<>();
        LinkedList<Node> path = this.getRoomPathToSection(from, to);
        if (path == null) return null; // meaning no path possible there.
        if (path.size() == 0) throw new NullPointerException("Something is really wrong...");
        return path.getLast().obstacles;
    }

    private LinkedList<Node> getRoomPathToSection(Point from, Section to) {

        ArrayList<Node> closed_set = new ArrayList<>();
        PriorityQueue<Node> open_set = new PriorityQueue<>(10, Comparator.comparingInt((n) -> n.f));
        Node init_node = new Node(null, passages.section_map[from.y][from.x], from, null, 0,
                to.getDistanceFromPoint(from),0, new ArrayList<>());
        open_set.add(init_node);

        boolean explore;
        LinkedList<Node> solution = new LinkedList<>();
        // main loop
        while(!open_set.isEmpty()) {
            explore = true;

            Node current_node = open_set.poll();

            if (!solution.isEmpty() && current_node.p > solution.getLast().p) {
                return solution;
            }

            // checking for goal state
            if (to.contains(current_node.position)) {
                LinkedList<Node> path = extractRoomPathWithBoxesPlan(current_node);
                // if first time we find a solution
                if (solution.isEmpty()) {
                    solution = path;
                }
                // if we find a better solution than we already have...
                if (!solution.isEmpty() && !path.isEmpty() && path.getLast().p < solution.getLast().p) {
                    solution = path;
                }
                explore = false;
            }

            // putting current state to the closed set
            closed_set.add(current_node);

            if (explore) {
                // generating successor states
                ArrayList<Path> neighbour_sections = this.passages.getAllNeighbours(current_node.position);
                if (neighbour_sections != null) {
                    for(Path s : neighbour_sections) {
                        PathWithObstacles path = Estimator.estimatePath(current_node.position, s.to, s.through, current_node.g);
                        if (path != null) { // if path exists...
                            Point p = path.arrivingPosition;
                            //System.err.println(distance);
                            ArrayList<Obstacle> obstacles = new ArrayList<>(current_node.obstacles);

                            obstacles.addAll(path.obstacles);
                            Node n = new Node(current_node, passages.section_map[p.y][p.x], p, s.through,
                                    current_node.g + path.distance, to.getDistanceFromPoint(p),
                                    current_node.punishment + path.punishment, obstacles);

                            Node already_in_open_set = getAlreadyInList(open_set, n);
                            Node already_in_closed_set = getAlreadyInList(closed_set, n);

                            // Check if open set contains a node which equals with current one but it's g values is less
                            if (already_in_open_set != null) {
                                if (already_in_open_set.p > n.p) {
                                    open_set.remove(already_in_open_set);
                                    open_set.add(n);
                                }
                                // if open set doesn't contains similar node, check on the closed set. if closed set contains,
                                // we should check whether closed set's g values is bigger
                            } else if (already_in_closed_set != null) {
                                if (already_in_closed_set.p > n.p) {
                                    open_set.add(n);
                                }
                                // If neither of them contains the node, then we should add it to the open set.
                            } else {
                                open_set.add(n);
                            }
                        }
                    }
                }
            }
        }

        if (!solution.isEmpty()) return solution;
        return null;
    }

    public LinkedList<Node> getRoomPath(Point from, Point to) {
        // if the same then return current room
        if (from.equals(to)) {
            Section s = null;
            for (Section s_c : passages.section_map[from.y][from.x]) {
                if (s_c != null) s = s_c;
            }
            Node current_room = new Node(null, passages.section_map[from.y][from.x], from, s,
                    0, 0, 0, new ArrayList<>());
            LinkedList<Node> n = new LinkedList<>();
            n.add(current_room);
            return n;
        }

        // checking whether it is even possible.
        LinkedList<Node> emptyRoomPath = getEmptyRoomPath(from, to);
        if (emptyRoomPath == null) return null;

        ArrayList<Node> closed_set = new ArrayList<>();
        PriorityQueue<Node> open_set = new PriorityQueue<>(10, Comparator.comparingInt((n) -> n.f));
        Node init_node = new Node(null, passages.section_map[from.y][from.x], from, null, 0,
                passages.getDistanceFrom(from, to), 0, new ArrayList<>());
        open_set.add(init_node);

        boolean explore;
        LinkedList<Node> solution = new LinkedList<>();
        // main loop
        while(!open_set.isEmpty()) {
            explore = true;

            Node current_node = open_set.poll();
            //.err.println(current_node);
            if (!solution.isEmpty() && current_node.p > solution.getLast().p) {
                return solution;
            }

            // checking for goal state
            if (current_node.position.equals(to)) {
                LinkedList<Node> path = extractRoomPathWithBoxesPlan(current_node);
                // if first time we find a solution
                if (solution.isEmpty()) {
                    solution = path;
                }
                // if we find a better solution than we already have...
                if (!solution.isEmpty() && !path.isEmpty() && path.getLast().p < solution.getLast().p) {
                    solution = path;
                }
                explore = false;
            }

            // putting current state to the closed set
            closed_set.add(current_node);

            // If already in goal room
            for (Section s : current_node.sections) {
                if (s != null) {
                    if (s.contains(to)) {
                        PathWithObstacles path = Estimator.estimatePath(current_node.position, to, s, current_node.g);
                        if (path != null) { // if path exists...
                            ArrayList<Obstacle> obstacles = new ArrayList<>(current_node.obstacles);
                            obstacles.addAll(path.obstacles);

                            Node n = new Node(current_node, passages.section_map[to.y][to.x], to, s,
                                    current_node.g + path.distance, 0,
                                    current_node.punishment + path.punishment, obstacles);

                            Node already_in_open_set = getAlreadyInList(open_set, n);
                            Node already_in_closed_set = getAlreadyInList(closed_set, n);

                            // Check if open set contains a node which equals with current one but it's g values is less
                            if (already_in_open_set != null) {
                                if (already_in_open_set.p > n.p) {
                                    open_set.remove(already_in_open_set);
                                    open_set.add(n);
                                }
                                // if open set doesn't contains similar node, check on the closed set. if closed set contains,
                                // we should check whether closed set's g values is bigger
                            } else if (already_in_closed_set != null) {
                                if (already_in_closed_set.p > n.p) {
                                    open_set.add(n);
                                }
                                // If neither of them contains the node, then we should add it to the open set.
                            } else {
                                open_set.add(n);
                            }
                        }
                    }
                }
            }

            if (explore) {
                // generating successor states
                ArrayList<Path> neighbour_sections = this.passages.getAllNeighbours(current_node.position);
                if (neighbour_sections != null) {
                    for(Path s : neighbour_sections) {
                        PathWithObstacles path = Estimator.estimatePath(current_node.position, s.to, s.through, current_node.g);
                        if (path != null) { // if path exists...
                            Point p = path.arrivingPosition;
                            //System.err.println(distance);
                            ArrayList<Obstacle> obstacles = new ArrayList<>(current_node.obstacles);

                            obstacles.addAll(path.obstacles);
                            Node n = new Node(current_node, passages.section_map[p.y][p.x], p, s.through,
                                    current_node.g + path.distance, passages.getDistanceFrom(p, to),
                                    current_node.punishment + path.punishment, obstacles);

                            Node already_in_open_set = getAlreadyInList(open_set, n);
                            Node already_in_closed_set = getAlreadyInList(closed_set, n);

                            // Check if open set contains a node which equals with current one but it's g values is less
                            if (already_in_open_set != null) {
                                if (already_in_open_set.p > n.p) {
                                    open_set.remove(already_in_open_set);
                                    open_set.add(n);
                                }
                                // if open set doesn't contains similar node, check on the closed set. if closed set contains,
                                // we should check whether closed set's g values is bigger
                            } else if (already_in_closed_set != null) {
                                if (already_in_closed_set.p > n.p) {
                                    open_set.add(n);
                                }
                                // If neither of them contains the node, then we should add it to the open set.
                            } else {
                                open_set.add(n);
                            }
                        }
                    }
                }
            }
        }

        if (!solution.isEmpty()) return solution;
        return null;
    }

    public LinkedList<Node> getEmptyRoomPath(Point from, Point to) {
        // closed set - already discovered nodes
        ArrayList<Node> closed_set = new ArrayList<>();

        // open set - not yet discovered nodes
        PriorityQueue<Node> open_set = new PriorityQueue<>(10, Comparator.comparingInt((n) -> n.f));

        Node init_node = new Node(null, passages.section_map[from.y][from.x], from, null, 0,
                passages.getDistanceFrom(from, to), 0, null);
        open_set.add(init_node);

        boolean explore;
        LinkedList<Node> solution = new LinkedList<>();
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
                if (neighbour_sections != null) {
                    for(Path s : neighbour_sections) {
                        int travel_distance = s.to.getDistanceFromPoint(current_node.position);
                        Point p = s.to.getClosestPoint(current_node.position);
                        Node n = new Node(current_node, passages.section_map[p.y][p.x], p, s.through,
                                current_node.g + travel_distance, passages.getDistanceFrom(p, to), 0,
                                null);

                        Node already_in_open_set = getAlreadyInList(open_set, n);
                        Node already_in_closed_set = getAlreadyInList(closed_set, n);

                        // Check if open set contains a node which equals with current one but it's g values is less
                        if (already_in_open_set != null) {
                            if (already_in_open_set.g > n.g) {
                                open_set.remove(already_in_open_set);
                                open_set.add(n);
                            }
                            // if open set doesn't contains similar node, check on the closed set. if closed set contains,
                            // we should check whether closed set's g values is bigger
                        } else if (already_in_closed_set != null) {
                            if (already_in_closed_set.g > n.g) {
                                open_set.add(n);
                            }
                            // If neither of them contains the node, then we should add it to the open set.
                        } else {
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
                goal_node.g + this.getDistance(goal_node.position, to), 0, 0, null);
        plan.addFirst(goal);
        Node n = goal_node;
        while (n.parent != null) {
            plan.addFirst(n);
            n = n.parent;
        }
        return plan;
    }

    private LinkedList<Node> extractRoomPathWithBoxesPlan(Node goal_node) {
        LinkedList<Node> plan = new LinkedList<>();
        Node n = goal_node;
        while (n.parent != null) {
            plan.addFirst(n);
            n = n.parent;
        }
        return plan;
    }

    public Node getAlreadyInList(AbstractCollection<Node> list, Node n) {
        for (Node n_inside: list) {
            if (n_inside.equals(n)) return n_inside;
        }
        return null;
    }

    public int getEmptyPathEstimate(Point from, Point to) {
        LinkedList<sampleclients.room_heuristics.Node> result = getEmptyRoomPath(from, to);
        if(result == null) return Integer.MAX_VALUE;
        else return result.	pollLast().g;
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
