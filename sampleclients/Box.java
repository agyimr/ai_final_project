package sampleclients;
import java.util.*;

public class Box extends MovingObject {
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
        List <Goal>goals = MainBoard.goalsByID.get(Character.toLowerCase(getID()));
        if(goals != null) {
            for(Goal current : goals) {
                int currentDistance = RandomWalkClient.roomMaster.getPathEstimate(getCoordinates(), current.getCoordinates());
                if(current.assignedBox == null && current.canBeSolved() && currentDistance < bestDistance) {
                    bestDistance = currentDistance;
                    bestGoal = current;
                }
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
    public boolean atGoalPosition() {
        if (assignedGoal == null) return false;
        if( getX() - assignedGoal.getX() == 0
                && getY() - assignedGoal.getY() == 0 ) {
            return true;
        }
        else {
            return false;
        }
    }
    public void resetDependencies() {

        for(Box theCurrentID : MainBoard.allBoxes) {
            theCurrentID.noGoalOnTheMap = false;
        }
        for(Agent sameColor : MainBoard.agents) {
            if(sameColor.jobless()) sameColor.moveYourAss();
        }


    }
}