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
            if(isMySpot(cur.getAgent())){
                return cur.getAgent();
            }

            //Get neighbour states of cur
            List<ConflictNode> neighbours = getNeighbours(cur);

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
            return maxNode.getAgent();
        }
        return null;
    }
//Expansion increase clock with new depth
        private static List<ConflictNode> getNeighbours (ConflictNode cur){

            List<ConflictNode> n = new ArrayList<ConflictNode>();
            int x1=0;
            int y1=0;
            clockIncrement = clockIncrement+1;
            for (int i = 0; i < 4; i++) {
                Point posCand = null;

                if(i==0){x1 = cur.getX();y1 = cur.getY()-1;}//UP
                if(i==1){x1 = cur.getX();y1 = cur.getY()+1;}//Down
                if(i==2){x1 = cur.getX()-1;y1 = cur.getY();}//Left
                if(i==3){x1 = cur.getX()+1;y1 = cur.getY();}//Right

                posCand = new Point(x1,y1);

                //if the command is applicable, and allowed in the enviroment
                if (isAllowed(posCand)) {
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
            if(earliestOcc==-1 && isSpaceAround(spot)){
                return true;
            }
            return false;
        }
        private static boolean isSpaceAround(Point spot) {
            if(spot.x == 0 || spot.x == map.getWidth() - 1 || spot.y == 0 || spot.y == map.getHeight() - 1) return false;
            if((map.isFree(spot.x+1, spot.y) && map.isFree(spot.x+1, spot.y - 1) && map.isFree(spot.x+1, spot.y + 1)) //right
                || (map.isFree(spot.x-1, spot.y) && map.isFree(spot.x-1, spot.y - 1) && map.isFree(spot.x-1, spot.y + 1)) //left
                || (map.isFree(spot.x-1, spot.y - 1) && map.isFree(spot.x, spot.y - 1) && map.isFree(spot.x+1, spot.y -1)) //top
                || (map.isFree(spot.x-1, spot.y + 1) && map.isFree(spot.x, spot.y + 1) && map.isFree(spot.x+1, spot.y +1))) {  //bottom
                return true;
            }
            return false;
        }



        private static boolean isAllowed (Point cand){
            int x = cand.x;
            int y = cand.y;
            if (x >= 0 && x < map.getWidth() && y >= 0 && y< map.getHeight() &&( map.isFree(x, y) || (nextMap.isAgent(x, y) && considerAgents))) {
                return false;
            }

            return true;
        }

}

class ConflictNode {
    private final Point pos;
    private ConflictNode parent = null;
    private Command c = null;
    private int Clock;

    public ConflictNode(Point pos, int Clock) {
        this.Clock = Clock;
        this.pos = pos;

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

    public Point getAgent() {return pos;}

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
        else return true;
    }

    @Override
    public String toString() {
        return "(" + pos.x + ", " + pos.y + ")";
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

