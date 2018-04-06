package doBFS;

public class Agent extends MovingObject{
	public Agent( char id, String color, int y, int x ) {
        super(id, color, y, x, "Agent");
			System.err.println("Found " + getColor() + " agent " + getID() + " at pos: " + getX() + ", " + getY());
    }
    @Override
    public String toString() {
        return "Agent "+ getID() + " color: " + getColor() + " at position: (" + getX() + ", " + getY() + ")";
    }
}
