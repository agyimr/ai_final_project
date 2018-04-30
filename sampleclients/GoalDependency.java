package sampleclients;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GoalDependency {
    public static MainBoard mainBoard = RandomWalkClient.gameBoard;
    public static void getGoalDependency(){
        System.err.println( "GoalDep started" );
        List<Goal> goals = MainBoard.allGoals;
        Map<Goal,List<Goal>> obstructions = new HashMap<Goal,List<Goal>>();

        for (Goal g : goals) {
            PriorityQueue<GDNode> explored = new PriorityQueue<GDNode>();
            PriorityQueue<GDNode> frontier = new PriorityQueue<GDNode>();

            //add start to frontier
            frontier.add(new GDNode(new Point(g.getX(), g.getY()), null, new ArrayList<Goal>()));
            while (!frontier.isEmpty()) {
                GDNode cur = frontier.poll();
                explored.add(cur);

                if(mainBoard.isBox((int)cur.getPos().getX(),(int)cur.getPos().getY())){
                    System.err.println("cur: "+mainBoard.getElement((int)cur.getPos().getX(),(int)cur.getPos().getY()).getID()+"  g: "+g.getID());
                    if(Character.toLowerCase(mainBoard.getElement((int)cur.getPos().getX(),(int)cur.getPos().getY()).getID()) == g.getID()){
                        obstructions.put(g, cur.getGoalSet());
                        continue;
                    }
                }



                //if (mainBoard.isGoal((int)cur.getPos().getX(),(int)cur.getPos().getY())) {
                 //   obstructions.put(g, cur.getGoalSet());
                //}

                LinkedList<GDNode> neighbours = getNeighbours(cur, goals); //TODO:board change again

                for (GDNode n : neighbours) {
                    if (!explored.contains(n) && !frontier.contains(n)) {
                        frontier.add(n);
                    }
                }
            }
        }

        Map<Goal,List<Goal>> tmp = generateDependencies(obstructions);
        for (Goal key : tmp.keySet()) {//Loop through goalSet
            key.deps = tmp.get(key);
        }
        System.err.println("-----------------------------------------------");
        for (Goal key : tmp.keySet()){//Loop through goalSet
            System.err.println("key: "+key.getID());
            for (Goal g : tmp.get(key)) {
                System.err.println("goalDep: " + g.getID());
            }
        }
        System.err.println("-----------------------------------------------");
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

    private static LinkedList<GDNode> getNeighbours(GDNode cur, List<Goal> goals){
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
            //if(mapEntry != null) {
               // int mapEntryX = mapEntry.getX();
                //int mapEntryY = mapEntry.getY();
            if (!mainBoard.isWall((int)pos.getX(), (int)pos.getY())) {
                List<Goal> s = cur.getGoalSet();
                if (mainBoard.isGoal((int)pos.getX(), (int)pos.getY())){
                    //for (Goal g : goals) {
                       // if (g.getX() == pos.x && g.getY() == pos.y) {
                    s.add((Goal)mapEntry);
                      //  }
                   // }
                }
                neighbours.add(new GDNode(pos, cur, s));
            }
           // }
        }
        return neighbours;
    }
    private static Map<Goal,List<Goal>> generateDependencies(Map<Goal,List<Goal>> obs){
        Map<Goal,List<Goal>> dep = new HashMap<Goal,List<Goal>>();
        for (Goal key : obs.keySet()){
            for (Goal g : obs.get(key)){
                List<Goal> s = dep.get(g);
                if (s == null){
                    s = new ArrayList<Goal>();
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
    List<Goal> g;

    public GDNode(Point pos, GDNode parent, List<Goal> g){
        this.pos = pos;
        this.parent = parent;
        this.g = g;
    }

     public Point getPos() {
         return pos;
     }

     public List<Goal> getGoalSet() {
         return new ArrayList<Goal>(g);
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