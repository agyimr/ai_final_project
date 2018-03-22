package sampleclients;

import java.io.*;
import java.util.*;

public class Agent extends MovingObject {
    public Agent( char id, String color, int y, int x ) {
        super(id, color, y, x, "Agent");
			System.err.println("Found " + getColor() + " agent " + getID() + " at pos: " + getX() + ", " + getY());
    }
    public String act() {
//            String move = Command.every[rand.nextInt( Command.every.length )].toString();
//            System.err.println(move);
        if (path != null) {
            Node nextStep = path.pollFirst();
            if (nextStep != null)
                return move(nextStep.getX(), nextStep.getY());
        }
        return "NoOp";
    }
    @Override
    public String toString() {
        return "Agent "+ getID() + " color: " + getColor() + " at position: (" + getX() + ", " + getY() + ")";
    }
}