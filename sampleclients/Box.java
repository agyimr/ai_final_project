package sampleclients;

import java.io.*;
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
        assignedGoal = null;
        assignedAgent = null;
    }
    boolean unassignedGoal() {
        return assignedGoal == null;
    }
    boolean tryToFindAGoal() {
        assignedGoal = MainBoard.goals.get(Character.toLowerCase(getID()));
        if (assignedGoal == null) {
            noGoalOnTheMap = true;
            assignedAgent = null;
            return false;
        }
        return true;
    }
    public boolean SetGoalPosition() {
        if (assignedGoal == null) return false;
        if( Integer.compare(getX(), assignedGoal.getX()) == 0
                && Integer.compare(getY(), assignedGoal.getY()) == 0 ) {
            assignedGoal.boxAtGoalPosition = this;
            atGoalPosition = true;
        }
        return atGoalPosition;
    }
}