package sampleclients;
import java.util.*;
import java.awt.Point;

public class FindSafeSpot {

    private static MainBoard map;
    private static AnticipationPlanning anticiObj;

    public static Point safeSpotBFS(Point startPos) {

        map = RandomWalkClient.gameBoard;
        anticiObj = RandomWalkClient.anticipationPlanning;

        List<ConflictNode> frontier = new ArrayList<ConflictNode>();
        List<ConflictNode> explored = new ArrayList<ConflictNode>();

        //Add the current agent position to explored
        frontier.add(new ConflictNode(startPos, anticiObj.getClock()));

        double bestEstimation = 0;
        Point bestSpot = null;

        //continue search as long as there are points in the firstfrontier
        while (!frontier.isEmpty()) {
            //pop the first element
            ConflictNode cur = frontier.get(0);
            frontier.remove(0);
            //goal check - Is this an empty spot? with perhabs area around it? or perhabs the highest anticipation clock relative to position,
            if(isMySpot(cur.getAgent())){
                System.err.println(cur.getAgent());
                return cur.getAgent();
//                throw new NullPointerException();
            }

            if(bestSpot == null) {
                bestSpot = cur.getAgent();
            } else {
                double estimation = estimateSpot(cur.getAgent(), cur.getClock());

                if(estimation > bestEstimation) {
                    bestEstimation = estimation;
                    bestSpot = cur.getAgent();
                }
            }

            //Get neighbour states of cur
            List<ConflictNode> neighbours = getNeighbours(cur);
            System.err.println("NEIGHBOURS: ");
            System.err.println(neighbours);
            //add the current ConflictNode to explored
            explored.add(cur);


            for (ConflictNode n : neighbours) {
                //if point is not visited
                if (!frontier.contains(n) && !explored.contains(n)) {
                    frontier.add(n);
                }
            }

        }
        /*
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
        */

        return bestSpot;
        //return null;
    }
//Expansion increase clock with new depth
        private static List<ConflictNode> getNeighbours (ConflictNode cur){

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
                if (isAllowed(posCand)) {
                    ConflictNode nodeCand = new ConflictNode(posCand, cur.getClock()+1);
                    nodeCand.setParent(cur);
                    n.add(nodeCand);
                }
            }
            return n;
        }

        private static boolean isMySpot(Point spot){
        //Maybe add area clear around spot, or maybe consider
            int earliestOcc = anticiObj.getEarliestOccupation(spot);
            if(earliestOcc == -1 && isSpaceAround(spot) && map.isFree(spot.x, spot.y) && !map.isGoal(spot.x, spot.y)){
                return true;
            }
            return false;
        }


        private static double estimateSpot(Point spot, int localClock) {


            if(RandomWalkClient.gameBoard.isGoal((int) spot.getX(), (int) spot.getY())) {
                return 0;
            }

            int geoDistance = localClock - anticiObj.getClock();

            if(geoDistance == 0) {
                return 0;
            }

            int space = getSpacenessAround(spot);

            int nextBooking = anticiObj.getEarliestOccupation(spot);

            if(nextBooking == -1) {
                nextBooking = anticiObj.getClock() + RandomWalkClient.gameBoard.getHeight() + RandomWalkClient.gameBoard.getWidth();
            }

            int bookingDistance = nextBooking - anticiObj.getClock() - geoDistance;

            if(bookingDistance - geoDistance < 0) {
                return 0;
            }

           // int score = (bookingDistance - geoDistance) * Math.min((space-2)/2,1) + 3 * -geoDistance;

            double score = (1-geoDistance) + (1+geoDistance) * Math.max(space-2, 0);
            score =  Math.max(0, space-2)^3 - (1-geoDistance)^2;

            if(geoDistance * 2 < nextBooking) {
                score *= 1.5;
            }

            //score = bookingDistance * -geoDistance * space;

            //System.err.println(spot.getX() + " " + spot.getY() + " " + score);
//            minimize geoDistance = maximize -geoDistance
//            maximize (bookingDistance - geoDistance)
//            maximize space
            return score;
        }

        private static int getSpacenessAround(Point spot) {
            if(spot.x == 0 || spot.x == map.getWidth() - 1 || spot.y == 0 || spot.y == map.getHeight() - 1) {
                return 0;
            }

            int space = 0;

            if(!map.isWall(spot.x+1, spot.y)) { //right
                space++;
            }
            if(!map.isWall(spot.x-1, spot.y)) { //left
                space++;
            }
            if(!map.isWall(spot.x, spot.y-1)) {//top
                space++;
            }
            if(!map.isWall(spot.x, spot.y+1)) {  //bottom
                space++;
            }

            return space;
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
            if (x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight() || map.isWall(x, y) || map.isBox(x, y)) {
                return false;
            }

            return true;
        }

}

class ConflictNode {
    private final Point pos;
    private ConflictNode parent = null;
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

