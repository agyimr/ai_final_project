package sampleclients;

import java.util.LinkedList;
import java.util.Arrays;
import java.util.*;
import java.awt.Point;
public class Command {
	static {
		LinkedList< Command > cmds = new LinkedList< Command >();
		for ( dir d : dir.values() ) {
			cmds.add( new Command( d ) );
		}

		for ( dir d1 : dir.values() ) {
			for ( dir d2 : dir.values() ) {
				if ( !Command.isOpposite( d1, d2 ) ) {
					cmds.add( new Command( type.Push, d1, d2 ) );
				}
			}
		}
		for ( dir d1 : dir.values() ) {
			for ( dir d2 : dir.values() ) {
				if ( d1 != d2 ) {
					cmds.add( new Command( type.Pull, d1, d2 ) );
				}
			}
		}
		every = cmds.toArray( new Command[0] );
//		System.err.println(every.toString());
	}

	public final static Command[] every;

	private static boolean isOpposite( dir d1, dir d2 ) {
		return d1.ordinal() + d2.ordinal() == 3;
	}

	// Order of enum important for determining opposites
	public enum dir {
		N, W, E, S
	}

    public enum type {
		Move, Push, Pull, Noop
	}

    public final type actType;
	public final dir dir1;
	public final dir dir2;
	public Command() {
		actType = type.Noop;
		dir1 = null;
		dir2 = null;
	}
	public Command( dir d ) {
		actType = type.Move;
		dir1 = d;
		dir2 = null;
	}
	public Command( type t, dir d1, dir d2 ) {
		actType = t;
		dir1 = d1;
		dir2 = d2;
	}

	public String toString() {
		if ( actType == type.Move )
			return actType.toString() + "(" + dir1 + ")";

		if (actType == type.Noop){
			return "NoOp";
		}
		return actType.toString() + "(" + dir1 + "," + dir2 + ")";
	}
	

	public String toActionString() {
		return "[" + this.toString() + "]";
	}
	
	public Point getNext(Point pos){
		//if noop return same point
		if(actType.equals(type.Noop)) {
			return pos;
		}
		Point next = null;
		Point agent = pos;
		
		//North
		if(dir1.equals(dir.N)){
			next = new Point( agent.x, agent.y-1 );
		}
		//West
		if(dir1.equals(dir.W)){
			next = new Point( agent.x-1, agent.y );
		}
		//East
		if(dir1.equals(dir.E)){
			next = new Point( agent.x+1, agent.y );
		}
		//South
		if(dir1.equals(dir.S)){
			next = new Point( agent.x, agent.y+1 );
		}
		
		return next;
	}
	public List<Point> getNext(List<Point> pos){
//		System.out.println(actType);
//		System.out.println(pos.toString());
//		System.out.println(toString());
		List<Point> next = new ArrayList<Point>();
		Point agent = pos.get(0);
		
		//If noop return same point
		if(actType.equals(type.Noop)) {
			return pos;
		}
		//North
		if(dir1.equals(dir.N)){
			next.add(new Point( agent.x, agent.y-1 ));
		}
		//West
		if(dir1.equals(dir.W)){
			next.add(new Point( agent.x-1, agent.y ));
		}
		//East
		if(dir1.equals(dir.E)){
			next.add(new Point( agent.x+1, agent.y ));
		}
		//South
		if(dir1.equals(dir.S)){
			next.add(new Point( agent.x, agent.y+1 ));
		}
		if(pos.size() == 2 && actType.equals(type.Pull)){
			Point box = new Point(pos.get(0).x,pos.get(0).y);
			next.add(box);
		}
		if(pos.size() == 2 && actType.equals(type.Push)){
			Point box = pos.get(1);
			//North
			if(dir2.equals(dir.N)){
				next.add(new Point( box.x, box.y-1 ));
			}
			//West
			if(dir2.equals(dir.W)){
				next.add(new Point( box.x-1, box.y ));
			}
			//East
			if(dir2.equals(dir.E)){
				next.add(new Point( box.x+1, box.y ));
			}
			//South
			if(dir2.equals(dir.S)){
				next.add(new Point( box.x, box.y+1 ));
			}
			
		}
		return next;
	}
	public static int dirToYChange(dir d) {
		// South is down one row (1), north is up one row (-1).
		if(d == null){
			return 0;
		}
		switch (d) {
			case S:
				return 1;
			case N:
				return -1;
			default:
				return 0;
		}
	}
	public static int dirToXChange(dir d) {
		// East is right one column (1), west is left one column (-1).
		if(d == null){
			return 0;
		}
		switch (d) {
			case E:
				return 1;
			case W:
				return -1;
			default:
				return 0;
		}
	}
    public dir invertDirection(dir Direction) {
        switch (Direction) {
            case N:
                return dir.S;
            case S:
                return dir.N;
            case E:
                return dir.W;
            case W:
                return dir.E;
        }
        return null;
    }
}
