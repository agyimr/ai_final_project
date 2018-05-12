package sampleclients;
import java.util.*;

import static sampleclients.RandomWalkClient.assignGoals;

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
                if(current.canBeSolved() && currentDistance < bestDistance && goalCloserToMe(current, currentDistance)) {
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
        else if(bestGoal.assignedBox != null){
            if(bestGoal.assignedBox.assignedAgent != null) {
                bestGoal.assignedBox.assignedAgent.finishTheJob();
            }
            bestGoal.assignedBox.assignedGoal = null;
        }
        bestGoal.assignedBox = this;
        assignedGoal = bestGoal;
        return true;
    }
    public boolean atGoalPosition() {
        if(assignedGoal == null) return false;
        else if( getX() - assignedGoal.getX() == 0
                && getY() - assignedGoal.getY() == 0 ) {
            return true;
        }
        else {
            return false;
        }
    }
    public void resetDependencies() {
        for(Box theCurrentBox : MainBoard.allBoxes) {
            theCurrentBox.noGoalOnTheMap = false;
        }
        for(Agent sameColor : MainBoard.agents) {
            if(sameColor.isJobless()) sameColor.moveYourAss();
        }


    }
    public boolean isBeingMoved() {
        if(assignedAgent == null) return false;
        return assignedAgent.isMovingBox();
    }
    private boolean goalCloserToMe(Goal current, int distance) {
        if(current.assignedBox == null) return true;
        else if(current.assignedBox.isBeingMoved()) return false;
        else {
            int currentDistance = RandomWalkClient.roomMaster.getPathEstimate(current.getCoordinates(), current.assignedBox.getCoordinates());
            if(currentDistance > distance) {
                return true;
            }
        }
        return false;
    }
}