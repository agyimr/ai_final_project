package sampleclients;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GoalDependency {
    public static Map<Goal,Set<Goal>> getGoalDependency(Map<Character,Set<Goal>> goalSet, char[][] board){
        System.err.println( "GoalDep started" );
        Map<Goal,Set<Goal>> obstructions = new HashMap<Goal,Set<Goal>>();
        Map<Goal,Set<Goal>> dependencies = new HashMap<Goal,Set<Goal>>();

        for (char key : goalSet.keySet()) {
            for (Goal g : goalSet.get(key)) {
                PriorityQueue<GDNode> explored = new PriorityQueue<GDNode>();
                PriorityQueue<GDNode> frontier = new PriorityQueue<GDNode>();

                //add start to frontier
                frontier.add(new GDNode(new Point(g.getX(), g.getY()), null, new HashSet<Goal>()));

                while (!frontier.isEmpty()) {
                    GDNode cur = frontier.poll();
                    explored.add(cur);

                    if (cur.isGoal(g, board)) {
                        obstructions.put(g, cur.getGoalSet());
                    }

                    LinkedList<GDNode> neighbours = getNeighbours(cur, board, goalSet);

                    for (GDNode n : neighbours) {
                        if (!explored.contains(n) && !frontier.contains(n)) {
                            frontier.add(n);
                        }
                    }
                }
            }


        }



        return generateDependencies(obstructions,dependencies);
    }


    public <E> boolean containsList(List<E> l1, List<E> l2){
        //goes through two list and returns false if one element is in the other list and vice versa
        for(E n : l1){
            if(l2.contains(n)){
                return true;
            }
        }
        return false;

    }

    public static LinkedList<GDNode> getNeighbours(GDNode cur,char[][] map, Map<Character,Set<Goal>> goalset){
        LinkedList<GDNode> neighbours = new LinkedList<GDNode>();

        //North
        Point pos = new Point(cur.getPos().x,cur.getPos().y-1);
        char mapEntry = map[pos.y][pos.x];
        if(!isWall(mapEntry)){
            Set<Goal> s = cur.getGoalSet();
            if(isGoal(mapEntry)){
                for(Goal g : goalset.get(mapEntry)){
                    if (g.getX() == pos.x && g.getY() == pos.y){
                        s.add(g);
                    }
                }
            }
            neighbours.add(new GDNode(pos, cur, s));
        }
        //South
        pos = new Point(cur.getPos().x,cur.getPos().y+1);
        mapEntry = map[pos.y][pos.x];
        if(!isWall(mapEntry)){
            Set<Goal> s = cur.getGoalSet();
            if(isGoal(mapEntry)){
                for(Goal g : goalset.get(mapEntry)){
                    if (g.getX() == pos.x && g.getY() == pos.y){
                        s.add(g);
                    }
                }
            }
            neighbours.add(new GDNode(pos, cur, s));
        }
        //West
        pos = new Point(cur.getPos().x-1,cur.getPos().y);
        mapEntry = map[pos.y][pos.x];
        if(!isWall(mapEntry)){
            Set<Goal> s = cur.getGoalSet();
            if(isGoal(mapEntry)){
                for(Goal g : goalset.get(mapEntry)){
                    if (g.getX() == pos.x && g.getY() == pos.y){
                        s.add(g);
                    }
                }
            }
            neighbours.add(new GDNode(pos, cur, s));
        }
        //East
        pos = new Point(cur.getPos().x+1,cur.getPos().y);
        mapEntry = map[pos.y][pos.x];
        if(!isWall(mapEntry)){
            Set<Goal> s = cur.getGoalSet();
            if(isGoal(mapEntry)){
                for(Goal g : goalset.get(mapEntry)){
                    if (g.getX() == pos.x && g.getY() == pos.y){
                        s.add(g);
                    }
                }
            }
            neighbours.add(new GDNode(pos, cur, s));
        }

        return neighbours;
    }

    public static Map<Goal,Set<Goal>> generateDependencies(Map<Goal,Set<Goal>> obs, Map<Goal,Set<Goal>> dep){
        for (Goal key : obs.keySet()){
            for (Goal g : obs.get(key)){
                Set<Goal> s = dep.get(g);
                if (s == null){
                    s = new HashSet<Goal>();
                }
                s.add(key);
                dep.put(g,s);
            }
        }
        return dep;
    }


    public static boolean isAgent (char id) { return ( '0' <= id && id <= '9' );}
    public static boolean isBox (char id) { return ( 'A' <= id && id <= 'Z' );}
    public static boolean isGoal (char id) { return ( 'a' <= id && id <= 'z' ); }
    public static boolean isWall (char id) {return (id == '+');}
}


 class GDNode implements Comparator<GDNode>, Comparable<GDNode>{
    Point pos;
    GDNode parent;
    Set<Goal> g;

    public GDNode(Point pos, GDNode parent, Set<Goal> g){
        this.pos = pos;
        this.parent = parent;
        this.g = g;
    }

     public Point getPos() {
         return pos;
     }

     public GDNode getParent() {
         return parent;
     }

     public Set<Goal> getGoalSet() {
         return new HashSet<Goal>(g);
     }
     public boolean isGoal(Goal g, char[][] map){
        boolean character = Character.toLowerCase(map[pos.y][pos.x]) == g.getID();
        return character;
     }

     @Override
     public boolean equals(Object obj) {
         GDNode other = (GDNode) obj;
         return pos.x == other.getPos().x && pos.y == other.getPos().y;
     }
     public int compare(GDNode self, GDNode other) {
         return self.getGoalSet().size() - other.getGoalSet().size();
     }
     public int compareTo(GDNode other) {
         return g.size() - other.getGoalSet().size();
     }
 }