package sampleclients;

import java.io.*;
import java.util.*;


public class Box extends MovingObject {
    public boolean atGoalPosition = false;
    public Goal assignedGoal = null;
    public Agent assignedAgent = null;
    boolean noGoalOntheMap = false;
    public Box( char id, String color, int currentRow, int currentColumn ) {
        super(id, color, currentRow, currentColumn, "Box");
//            System.err.println("Found " + color + " box " + id + " at pos: " + currentColumn + ", " + currentRow );
    }
    public void clearOwnerReferences() {
        assignedGoal = null;
        assignedAgent = null;
    }
}