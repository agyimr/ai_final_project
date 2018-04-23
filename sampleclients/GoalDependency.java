package sampleclients;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GoalDependency {
    public static MainBoard mainBoard = RandomWalkClient.gameBoard;
    public static Map<Goal,Set<Goal>> getGoalDependency(Map<Character,Set<Goal>> goalSet){
        System.err.println( "GoalDep started" );
        Map<Goal,Set<Goal>> obstructions = new HashMap<Goal,Set<Goal>>();

        for (char key : goalSet.keySet()) {//Loop through goalSet
            for (Goal g : goalSet.get(key)) {
                PriorityQueue<GDNode> explored = new PriorityQueue<GDNode>();
                PriorityQueue<GDNode> frontier = new PriorityQueue<GDNode>();

                //add start to frontier
                frontier.add(new GDNode(new Point(g.getX(), g.getY()), null, new HashSet<Goal>()));

                while (!frontier.isEmpty()) {
                    GDNode cur = frontier.poll();
                    explored.add(cur);

                    if (mainBoard.isGoal((int)cur.getPos().getX(),(int)cur.getPos().getY())) { //TODO: Change isGoal check (cur.isGoal(g, board))
                        obstructions.put(g, cur.getGoalSet());
                    }

                    LinkedList<GDNode> neighbours = getNeighbours(cur, goalSet); //TODO:board change again

                    for (GDNode n : neighbours) {
                        if (!explored.contains(n) && !frontier.contains(n)) {
                            frontier.add(n);
                        }
                    }
                }
            }
        }
        return generateDependencies(obstructions);
    }


    private <E> boolean containsList(List<E> l1, List<E> l2){
        //goes through two list and returns false if one element is in the other list and vice versa
        for(E n : l1){
            if(l2.contains(n)){
                return true;
            }
        }
        return false;
    }

    private static LinkedList<GDNode> getNeighbours(GDNode cur, Map<Character,Set<Goal>> goalset){
        LinkedList<GDNode> neighbours = new LinkedList<GDNode>();

        for (int i=0; i<4;i++){
            Point pos = null;
            if(i==0){
                pos = new Point(cur.getPos().x,cur.getPos().y-1);
            }else if(i==1){
                pos = new Point(cur.getPos().x,cur.getPos().y+1);
            }else if(i==2){
                pos = new Point(cur.getPos().x-1,cur.getPos().y);
            }else if(i==3){
                pos = new Point(cur.getPos().x+1,cur.getPos().y);
            }
            BasicObject mapEntry = mainBoard.getElement(pos.x,pos.y);
            int mapEntryX = mapEntry.getX();
            int mapEntryY = mapEntry.getY();
            if(!mainBoard.isWall(mapEntryX,mapEntryY)){
                Set<Goal> s = cur.getGoalSet();
                if(mainBoard.isGoal(mapEntryX,mapEntryY)){
                    for(Goal g : goalset.get(mapEntry.getID())){
                        if (g.getX() == pos.x && g.getY() == pos.y){
                            s.add(g);
                        }
                    }
                }
                neighbours.add(new GDNode(pos, cur, s));
            }
        }
        return neighbours;
    }

    private static Map<Goal,Set<Goal>> generateDependencies(Map<Goal,Set<Goal>> obs){
        Map<Goal,Set<Goal>> dep = new HashMap<Goal,Set<Goal>>();
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

     public Set<Goal> getGoalSet() {
         return new HashSet<Goal>(g);
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