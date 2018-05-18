package sampleclients;

import java.io.*;
import java.util.*;
import static sampleclients.Command.dir;
import static sampleclients.Command.type;

public class MovingObject extends BasicObject {
    private String color;
    public MovingObject ( char id, String color, int y, int x , int objID, String ObjectType) {
        super(y, x, id, objID,  ObjectType);
        this.color = color;

    }

    public String getColor(){ return color;}

    dir getDirection(int x,int y) {
        dir Direction = null;
        if(x!=getX()) {
            if(x>getX()) {
                Direction = dir.E;
            } else {
                Direction = dir.W;
            }
        }
        else if(y != getY()) {
            if(y>getY()) {
                Direction = dir.S;
            } else {
                Direction = dir.N;
            }
        }
        return Direction;
    }

    @Override
    public String toString() {
        return getObjectType() + " id:" + getID() + " color: " + getColor() + " at position: (" + getX() + ", " + getY() + ")";
    }


}