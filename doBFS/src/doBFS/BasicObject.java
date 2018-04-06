package doBFS;
import java.io.*;
import java.util.*;


public class BasicObject {
    private String ObjectType;
    private int y = 0, x = 0;
    public BasicObject(int y, int x, String... ObjectType ) {
        this.y = y;
        this.x = x;
        this.ObjectType = ObjectType.length == 1 ? ObjectType[0] : "BasicObject";
//            System.err.println("Found " + color + " agent " + id + "at pos:" + x + ", " + y);
    }
    public int getY() { return y;}
    public  void setY(int nextY) { y = nextY;}

    public int getX() { return x;}
    public void setX(int nextX) {
        x = nextX;}

    public String getObjectType() {return ObjectType;}
}