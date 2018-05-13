package sampleclients;

import java.io.*;
import java.util.*;


public class Goal extends BasicObject {
    public Box assignedBox = null;
    public List<Goal> deps = new ArrayList<Goal>();
    public List<Goal> obs = new ArrayList<>();
    public Goal( char id, int y, int x ) {
        super( y, x,id, "Goal");
    }
    public boolean solved(){
        BasicObject el = RandomWalkClient.gameBoard.getElement(this.getX(),this.getY());
        if(el instanceof Box) {
            return (Character.toLowerCase(el.getID())==this.getID());
        }
        else {
            return false;
        }
    }
    public boolean canBeSolved(){
        for (Goal g : deps){
            if(!g.solved()){
                return false;
            }
        }
        return true;

    }
    public void findClosestBox() {
        int bestDistance = Integer.MAX_VALUE;
        Box bestBox = null;
        List <Box>boxes = MainBoard.boxesByID.get(Character.toUpperCase(getID()));
        if(boxes != null) {
            for(Box current : boxes) {
                int currentDistance = RandomWalkClient.roomMaster.getEmptyPathEstimate(getCoordinates(), current.getCoordinates());
                if(currentDistance < bestDistance && boxCloserToMe(current, currentDistance)) {
                    bestDistance = currentDistance;
                    bestBox = current;
                }
            }
        }
        if (bestBox == null) {
            assignedBox = null;
        }
        else {
            if(bestBox.assignedGoal != null) {
                Goal imSorry = bestBox.assignedGoal;
                imSorry.assignedBox = null;

                assignedBox = bestBox;
                assignedBox.assignedGoal = this;

                imSorry.findClosestBox();
            }
            else {
                assignedBox = bestBox;
                assignedBox.assignedGoal = this;
            }
        }
        System.err.println("Goal: "+ this + ", Assigned Box: " + assignedBox);
    }
    private boolean boxCloserToMe(Box current, int distance) {
        if(current.assignedGoal == null) return true;
        else {
            int currentDistance = RandomWalkClient.roomMaster.getEmptyPathEstimate(current.getCoordinates(), current.assignedGoal.getCoordinates());
            if(currentDistance > distance) {
                return true;
            }
        }
        return false;
    }
}
