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
        if(assignedGoal != null) return  true;
        int bestDistance = Integer.MAX_VALUE;
        Goal bestGoal = null;
        List <Goal>goals = MainBoard.goalsByID.get(Character.toLowerCase(getID()));
        if(goals != null) {
            for(Goal current : goals) {
                if(current.canBeSolved() && !current.solved()) {
                    int currentDistance = RandomWalkClient.roomMaster.getEmptyPathEstimate(getCoordinates(), current.getCoordinates());
                    if( currentDistance < bestDistance && goalCloserToMe(current, currentDistance) ) { // remember, deleting this estimation crashes everything.
                        bestDistance = currentDistance;
                        bestGoal = current;
                    }
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
        return RandomWalkClient.gameBoard.isGoal(getX(), getY())
                && (RandomWalkClient.gameBoard.getGoal(getX(), getY()).getID() == Character.toLowerCase(getID()));
    }
    public boolean reachedAssignedGoal() {
        return assignedGoal != null && assignedGoal.getCoordinates().equals(getCoordinates());
    }
    public void resetDependencies() {
        System.err.println("Resetting box: " + this + " and goal: " + assignedGoal);
        assignedGoal.assignedBox = null;
        assignedGoal = null;
        for(Box current: MainBoard.allBoxes) {
            current.noGoalOnTheMap = false;
        }
//        for(Goal obstruction : assignedGoal.obs) {
//            for(Box issue : MainBoard.boxesByID.get(Character.toUpperCase(obstruction.getID()))) {
//                issue.noGoalOnTheMap = false;
//                System.err.println("Issue: " + issue);
//            }
//        }
        for(Agent every : MainBoard.agents) {
            if(every.isJobless()) every.moveYourAss();
        }
//        if(!assignedGoal.obs.isEmpty()) {
//            throw new NullPointerException();
//        }
    }
    public boolean isBeingMoved() {
        if(assignedAgent == null) return false;
        return assignedAgent.isMovingBox();
    }
    private boolean goalCloserToMe(Goal current, int distance) {
        if(current.assignedBox == null) return true;
        else if(current.assignedBox.isBeingMoved()) return false;
        else {
            int currentDistance = RandomWalkClient.roomMaster.getEmptyPathEstimate(current.getCoordinates(), current.assignedBox.getCoordinates());
            if(currentDistance > distance) {
                return true;
            }
        }
        return false;
    }
}