package sampleclients;

import java.util.LinkedList;
import java.util.Arrays;
import java.util.*;
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
	public static enum dir {
		N, W, E, S
	};
	
	public static enum type {
		Move, Push, Pull
	};

	public final type actType;
	public final dir dir1;
	public final dir dir2;

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

		return actType.toString() + "(" + dir1 + "," + dir2 + ")";
	}
	

	public String toActionString() {
		return "[" + this.toString() + "]";
	}
	
	public List<Node> getReservedNodes(int x ,int y){
		List<Node> n = new ArrayList<Node>();
		try{
            switch (dir1) {
                case N:
                    n.add(new Node(y - 1, x));
                    break;

                case S:
                    n.add(new Node(y + 1, x));
                    break;

                case E:
                	n.add(new Node(y, x + 1));
                    break;

                case W:
                	n.add(new Node(y, x - 1));
                    break;

            }
            return n;
        }
        catch(UnsupportedOperationException exc) {

            return n;
        }
	}
}
