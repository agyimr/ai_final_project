package sampleclients;
import java.util.*;
import java.awt.Point;

public class FindSafeSpot {

    private static MainBoard map;
    private static AnticipationPlanning anticiObj;
    private static boolean IamBox;

    private static final int MAX_DEPTH = 10;

    public static Point safeSpotBFS(Point startPos) {
        System.err.println("ENTERING SAFESPOT \n");
        System.err.println("START POS: " + startPos + "\n");

        map = RandomWalkClient.gameBoard;
        anticiObj = RandomWalkClient.anticipationPlanning;
        IamBox = map.isBox(startPos.x, startPos.y);
        List<ConflictNode> frontier = new ArrayList<ConflictNode>();
        List<ConflictNode> explored = new ArrayList<ConflictNode>();

        int startClock = anticiObj.getClock();

        //Add the current agent position to explored
        frontier.add(new ConflictNode(startPos, anticiObj.getClock()));

        double bestEstimation = -99999999;
        Point bestSpot = null;

        //continue search as long as there are points in the firstfrontier
        while (!frontier.isEmpty()) {
            //pop the first element
            ConflictNode cur = frontier.get(0);
            frontier.remove(0);
            //goal check - Is this an empty spot? with perhabs area around it? or perhabs the highest anticipation clock relative to position,
//            if(isMySpot(cur.getAgent())){
//                System.err.println("SAFESPOT: " + cur.getAgent());
//                return cur.getAgent();
////                throw new NullPointerException();
//            }
            if(cur.getClock() - startClock >= MAX_DEPTH) {
                break;
            }

            if(bestSpot == null) {
                bestSpot = cur.getAgent();
                bestEstimation =  estimateSpot(cur.getAgent(), cur.getClock());
            } else {
                double estimation = estimateSpot(cur.getAgent(), cur.getClock());

                if(estimation > bestEstimation) {
                    bestEstimation = estimation;
                    bestSpot = cur.getAgent();
                }
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
        System.err.println("Hello ?");
        System.err.println("safeSpot " + startPos + " -> " + bestSpot);
        if(bestSpot.x == startPos.x && bestSpot.y == startPos.y){
            System.err.println("SAFESPOT NOT FOUND");
            return null;
        }
        System.err.println("SAFESPOT: " + bestSpot);
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

            // We try to maximize the score we return (0 = Simba, you must never there)

            if(RandomWalkClient.gameBoard.isGoal((int) spot.getX(), (int) spot.getY())) {
                return -99999999;
            }

            // Geographical distance until the spot
            int geoDistance = localClock - anticiObj.getClock();

            if(geoDistance == 0) {
                return -99999998;
            }

            // Number of free case around the spot
            int space = getSpacenessAround(spot);

            // Get the next instant where spot will be booked
            int nextBooking = anticiObj.getEarliestOccupation(spot);

            // If no booking then next booking will be in a far futur
            if(nextBooking == -1) {
                nextBooking = anticiObj.getClock() + 10000;
            }

            // Distance to next booking WHEN I will reach the spot
            int bookingDistance = nextBooking - anticiObj.getClock();

            // If cell was booked between the instant I start to my position and I arrive on the cell
            if(bookingDistance - geoDistance < 0) {
                return -99999997;
            }

            // If I am a box and I want to reach a box
            if( IamBox && map.isBox(spot.x, spot.y)) {
                return -99999996;
            }

            if(space <= 2) {
                return -99999995;
            }

            // More the score is high, more the spot is attractive
            // maximize nextBooking
            // maximize space
            // minimize geoDistance
            double score = nextBooking * Math.pow(2, space) / geoDistance;

            return score;
        }

        private static boolean isStaticCell(int x, int y) {
            return map.isWall(x, y) || (map.getElement(x, y) instanceof Box && ((Box) map.getElement(x, y)).assignedAgent == null);
        }

        private static int getSpacenessAround(Point spot) {
            if(spot.x == 0 || spot.x == map.getWidth() - 1 || spot.y == 0 || spot.y == map.getHeight() - 1) {
                return 0;
            }

            int space = 0;

            for(int dy=-1;dy<=1;dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if(dx != 0 && dy != 0 && !isStaticCell(spot.x + dx, spot.y + dy)) {
                        space++;
                    }
                }
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
            if (notWithinMapConstraints(x, y) || map.isWall(x, y) || (!IamBox && (map.isBox(x, y) || map.isAgent(x, y)))) {
                return false;
            }

            return true;
        }

    private static boolean notWithinMapConstraints(int x, int y) {
        return x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight();
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

