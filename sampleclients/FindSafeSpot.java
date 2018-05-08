package sampleclients;
import java.io.*;
import java.util.*;
import java.awt.Point;
import sampleclients.Command.dir;
import sampleclients.Command.type;

public class FindSafeSpot {

    private static MainBoard map;
    private static MainBoard nextMap;
    private static AnticipationPlanning anticiObj;
    private static int startClock;
    private static boolean considerAgents = true;
    private static int clockIncrement=0;
    public static Point safeSpotBFS(Point startPos) {
        map = RandomWalkClient.gameBoard;
        nextMap = RandomWalkClient.nextStepGameBoard;
        anticiObj = RandomWalkClient.anticipationPlanning;
        startClock = anticiObj.getClock();
        List<ConflictNode> frontier = new ArrayList<ConflictNode>();
        List<ConflictNode> explored = new ArrayList<ConflictNode>();

        //Add the current agent position to explored
        frontier.add(new ConflictNode(startPos,startClock));

        //continue search as long as there are points in the firstfrontier
        while (!frontier.isEmpty()) {
            //pop the first element
            ConflictNode cur = frontier.get(0);
            frontier.remove(0);

            //goal check - Is this an empty spot? with perhabs area around it? or perhabs the highest anticipation clock relative to position,
            if(isMySpot(cur.getPoints().get(0))){
                return cur.getPoints().get(0);
            }

            //Get neighbour states of cur
            List<ConflictNode> neighbours = getNeighbours(cur, startPos);

            //add the current ConflictNode to explored
            explored.add(cur);


            for (ConflictNode n : neighbours) {
                //if point is not visited
                if (!frontier.contains(n) && !explored.contains(n)) {
                    frontier.add(n);
                }
            }

        }
        if(frontier.isEmpty()){
            int max = 0;
            ConflictNode maxNode = explored.get(0);
            for (ConflictNode n : explored){
                if(n.getClock()> max){
                    max = n.getClock();
                    maxNode = n;
                }
            }
            return maxNode.getPoints().get(0);
        }
        return new Point();
    }
//Expansion increase clock with new depth
        private static List<ConflictNode> getNeighbours (ConflictNode cur, Point startPos){

            List<ConflictNode> n = new ArrayList<ConflictNode>();
            int x1=0;
            int y1=0;
            for (int i = 0; i < 4; i++) {
                Point posCand = null;

                if(i==0){x1 = cur.getX();y1 = cur.getY()-1;}//UP
                if(i==1){x1 = cur.getX();y1 = cur.getY()+1;}//Down
                if(i==2){x1 = cur.getX()-1;y1 = cur.getY();}//Left
                if(i==3){x1 = cur.getX()+1;y1 = cur.getY();}//Right

                posCand = new Point(x1,y1);

                //if the command is applicable, and allowed in the enviroment
                if (isAllowed(posCand, startPos)) {
                    clockIncrement = clockIncrement+1;
                    ConflictNode nodeCand = new ConflictNode(posCand, startClock+clockIncrement);
                    nodeCand.setParent(cur);
                    n.add(nodeCand);
                }
            }
            return n;
        }

        private static boolean isMySpot(Point spot){
        //Maybe add area clear around spot, or maybe consider
            int earliestOcc = anticiObj.getEarliestOccupation(spot);
            if(earliestOcc==-1){
                return true;
            }
            return false;
        }




        private static boolean isAllowed (Point cand, Point Pos){
            if (!(Pos == cand)) {
                int x = cand.x;
                int y = cand.y;
                if (map.isWall(x, y) || map.isBox(x, y) || map.isAgent(x, y) || nextMap.isWall(x, y) || nextMap.isBox(x, y) || (nextMap.isAgent(x, y) && considerAgents)) {
                    return false;
                }
            }
            return true;
        }

}

class ConflictNode {
    private final Point pos;
    private final Point boxPos;
    private final List<Point> posLst;
    private ConflictNode parent = null;
    private Command c = null;
    private int Clock;

    public ConflictNode(Point pos, int Clock) {
        this.Clock = Clock;
        this.pos = pos;
        boxPos = null;
        posLst = new ArrayList<Point>();
        posLst.add(pos);

    }

    public void setParent(ConflictNode p) {
        parent = p;
    }
    public ConflictNode getParent() {
        return parent;
    }

    public void setCommand(Command c) {
        this.c = c;
    }
    public Command getCommand() {
        return c;
    }

    public int getClock(){
        return this.Clock;
    }

    public int getX() { return pos.x;}
    public int getY() {return pos.y;}
    public int getBoxX() { return boxPos.x;}
    public int getBoxY() {return boxPos.y;}
    public List<Point> getPoints() {return posLst;}
    public boolean hasBox(){return posLst.size() == 2;}

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        ConflictNode other = (ConflictNode) obj;
        if (pos.x != other.pos.x)
            return false;
        if (pos.y != other.pos.y)
            return false;
        return posLst.equals(other.posLst);
    }

    @Override
    public String toString() {
        if(boxPos == null){
            return "(" + pos.x + ", " + pos.y + ")";
        }else{
            return "(" + pos.x + ", " + pos.y + ")" + "(" + boxPos.x + ", " + boxPos.y + ")";
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17;
        result = prime * result;
        result = prime * result + pos.x;
        result = prime * result + pos.y;
        return result;
    }
}

