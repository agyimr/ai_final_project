package doBFS;

public class Box extends MovingObject{
	 public Box( char id, String color, int currentRow, int currentColumn ) {
	        super(id, color, currentRow, currentColumn, "Box");
//	            System.err.println("Found " + color + " box " + id + " at pos: " + currentColumn + ", " + currentRow );
	    }
}
