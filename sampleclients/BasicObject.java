package sampleclients;

import java.awt.*;
import java.io.*;
import java.util.*;


public class BasicObject {
    private String ObjectType;
    private int y , x;
    private char id;
    public BasicObject(int y, int x, char id, String... ObjectType ) {
        this.y = y;
        this.x = x;
        this.id = id;
        this.ObjectType = ObjectType.length == 1 ? ObjectType[0] : "BasicObject";
    }
    public int getY() { return y;}
    public  void setY(int nextY) { y = nextY;}

    public int getX() { return x;}
    public void setX(int nextX) {
        x = nextX;}
    public void setCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public Point getCoordinates() {
        return new Point(x,y);
    }
    public String getObjectType() {return ObjectType;}
    public char getID() { return id;}
    @Override
    public String toString() {
        return getObjectType() + " id:" + getID() + " at position: (" + getX() + ", " + getY() + ")";
    }

    //only equals if at the same coordinates or the same object
    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }
        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof BasicObject)) {
            return false;
        }
        // typecast o to Complex so that we can compare data members
        BasicObject c = (BasicObject) o;
        // Compare the data members and return accordingly
        return Integer.compare(getX(), c.getX()) == 0
                && Integer.compare(getY(), c.getY()) == 0;
    }
}