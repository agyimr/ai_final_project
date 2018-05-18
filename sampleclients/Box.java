package sampleclients;

import java.util.*;



public class Box extends MovingObject {
    public Goal assignedGoal = null;
    public Agent assignedAgent = null;
    boolean noGoalOnTheMap = false;
    int boxRemovalTime = 0;
    public Box( char id, String color, int currentRow, int currentColumn, int objID) {
        super(id, color, currentRow, currentColumn, objID,  "Box");
//            System.err.println("Found " + color + " box " + id + " at pos: " + currentColumn + ", " + currentRow );
    }
    public void clearOwnerReferences() {
        assignedAgent = null;
    }
    boolean unassignedGoal() {
        return assignedGoal == null;
    }
    boolean canBeSolved() {
        if(assignedGoal != null) {
            if(assignedGoal.canBeSolved() && !assignedGoal.solved()) return true;
            else {
                assignedGoal.assignedBox = null;
                assignedGoal = null;
            }
        }
        List <Goal>goals = MainBoard.goalsByID.get(Character.toLowerCase(getID()));
        if(goals != null) {
            for(Goal current : goals) {
                if(current.canBeSolved() && !current.solved()) {
                    int currentDistance = RandomWalkClient.roomMaster.getEmptyPathEstimate(getCoordinates(), current.getCoordinates());
                    if( currentDistance < Integer.MAX_VALUE) { // remember, deleting this estimation crashes everything.
                        return true;
                    }
                }
            }
        }
        return false;
    }
    boolean tryToFindAGoal() {
        if(assignedGoal != null) {
            if(assignedGoal.canBeSolved() && !assignedGoal.solved()) return true;
            else {
                assignedGoal.assignedBox = null;
                assignedGoal = null;
            }
        }
        int bestDistance = Integer.MAX_VALUE;
        Goal bestGoal = null;
        List <Goal>goals = MainBoard.goalsByID.get(Character.toLowerCase(getID()));
        if(goals != null) {
            for(Goal current : goals) {
                if(current.canBeSolved() && !current.solved()) {
                    int currentDistance = RandomWalkClient.roomMaster.getPathEstimate(getCoordinates(), current.getCoordinates(), getColor());
                    if( currentDistance < bestDistance && goalCloserToMe(current, currentDistance) ) { // remember, deleting this estimation crashes everything.
                        bestDistance = currentDistance;
                        bestGoal = current;
                    }
                }
            }
        }
        if (bestGoal == null) {
            noGoalOnTheMap = true;
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
        return assignedAgent.isWithBox();
    }
    private boolean goalCloserToMe(Goal current, int distance) {

        if(current.assignedBox == null) return true;
        else if(current.assignedBox.isBeingMoved()) return false;
        else {
            int currentDistance = RandomWalkClient.roomMaster.getPathEstimate(current.getCoordinates(), current.assignedBox.getCoordinates(), getColor());
            if(currentDistance > distance) {
                return true;
            }
        }
        return false;
    }
}