package sampleclients;
import java.util.*;

public class Box extends MovingObject {
    public boolean atGoalPosition = false;
    public Goal assignedGoal = null;
    public Agent assignedAgent = null;
    boolean noGoalOnTheMap = false;
    public Box( char id, String color, int currentRow, int currentColumn ) {
        super(id, color, currentRow, currentColumn, "Box");
//            System.err.println("Found " + color + " box " + id + " at pos: " + currentColumn + ", " + currentRow );
    }
    public void clearOwnerReferences() {
        assignedAgent = null;
    }
    boolean unassignedGoal() {
        return assignedGoal == null;
    }
    boolean tryToFindAGoal() {
        int bestDistance = Integer.MAX_VALUE;
        Goal bestGoal = null;
        for(Goal current : MainBoard.goalsByID.get(Character.toLowerCase(getID()))) {
            int currentDistance = RandomWalkClient.roomMaster.getPathEstimate(getCoordinates(), current.getCoordinates());
            if(current.assignedBox == null && currentDistance < bestDistance) {
                bestDistance = currentDistance;
                bestGoal = current;
            }
        }
        if (bestGoal == null) {
            noGoalOnTheMap = true;
            assignedAgent = null;
            return false;
        }
        else {
            bestGoal.assignedBox = this;
            assignedGoal = bestGoal;
            return true;
        }
    }
    public boolean setGoalPosition() {
        if (assignedGoal == null) return false;
        if( Integer.compare(getX(), assignedGoal.getX()) == 0
                && Integer.compare(getY(), assignedGoal.getY()) == 0 ) {
            assignedGoal.boxAtGoalPosition = true;
            atGoalPosition = true;
        }
        else {
            atGoalPosition = false;
        }
        return atGoalPosition;
    }
}