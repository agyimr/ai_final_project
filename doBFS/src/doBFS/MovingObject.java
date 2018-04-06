package doBFS;

import java.io.*;
import java.util.*;
import static doBFS.Command.dir;
import static doBFS.Command.type;

public class MovingObject extends BasicObject{
	 private String color;
	    private char id;
	    public LinkedList<Node> path;
	    public MovingObject ( char id, String color, int currentRow, int currentColumn , String ObjectType) {
	        super(currentRow, currentColumn, ObjectType);
	        this.color = color;
	        this.id = id;
	    }
	    public char getID() { return id;}
	    public String getColor(){ return color;}

	   
}
