package sampleclients;

import java.io.*;
import java.util.*;



public class RandomWalkClient {
	private static Random rand = new Random();
	private BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
	public static List< Agent > agents = new ArrayList< Agent >();
    public static List< Box > boxes = new ArrayList< Box>();
    //GLOBAL HERE
    public static Map<String, List<MovingObject>> ColorGroups;
    public static Map<Character, Goal> goals = new HashMap<Character, Goal>();
    public static int MainBoardYDomain = 0, MainBoardXDomain = 0;
    public static char[][] MainBoard; //every state change is seen on the main board TODO embed in a class
    public static char[][] NextMainBoard;
    public static GlobalPlanningBoard globalPlanningBoard;
    public static boolean isAgent (char id) { return ( '0' <= id && id <= '9' );}
    public static boolean isBox (char id) { return ( 'A' <= id && id <= 'Z' );}
    public static boolean isGoal (char id) { return ( 'a' <= id && id <= 'z' ); }
    public static boolean isWall (char id) {return (id == '+');}
	public RandomWalkClient() throws IOException {
		readMap(); // do not do anything before!
/*		Agent someAgent = agents.get(2);
        LinkedList<Node> path = someAgent.findPathToBox(ColorGroups.get(someAgent.getColor()).get(2));
        System.err.println(path + " for Agent: " + someAgent);*/
	}

	private void readMap() throws IOException  {
		Map< Character, String > colors = new HashMap< Character, String >();
		String line, color;

		// Read lines specifying colors
		while ( ( line = in.readLine() ).matches( "^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$" ) ) {
			line = line.replaceAll( "\\s", "" );
			color = line.split( ":" )[0];

			for ( String id : line.split( ":" )[1].split( "," ) )
				colors.put( id.charAt( 0 ), color );
		}
        // Read lines specifying level layout
        ColorGroups = new HashMap<String, List<MovingObject>>(colors.size());
        ArrayList<String> table = new ArrayList<>();
		int currentX = 0;
        while ( !line.equals( "" ) ) {
            for ( int i = 0; i < line.length(); i++ ) {
                char id = line.charAt( i );
                if (isAgent(id)) {
                    String currentColor = colors.get( id );
                    if(currentColor == null) currentColor = "blue";
                    Agent newAgent = new Agent( id,currentColor, MainBoardYDomain, currentX);
                    agents.add( newAgent );

                }
                else if (isBox(id)) {
                    String currentColor = colors.get( id );
                    if(currentColor == null) currentColor = "blue";
                    Box newBox = new Box( id, currentColor, MainBoardYDomain, currentX);
                    boxes.add( newBox );
                    List<MovingObject> result = ColorGroups.get(currentColor);
                    if (ColorGroups.get(currentColor) == null) {
                        result = new ArrayList<MovingObject>();
                        result.add(newBox);
                        ColorGroups.put(currentColor, result);
                    }
                    else {
                        result.add(newBox);
                    }
                }
                else if(isGoal(id)) {
                    goals.put(id, new Goal(id, MainBoardYDomain, currentX));
                }

                ++currentX;
            }
            if(MainBoardXDomain < currentX) {
                MainBoardXDomain = currentX; }
            table.add(line);
            line = in.readLine();
            currentX = 0;
            ++MainBoardYDomain;
        }
        Collections.sort(agents, (left, right) -> left.getID() - right.getID());

        globalPlanningBoard = new GlobalPlanningBoard(MainBoardXDomain, MainBoardYDomain);

        MainBoard = new char[MainBoardYDomain][MainBoardXDomain];
        for(int row = 0; row < MainBoardYDomain; ++row) {
            MainBoard[row] = table.get(row).toCharArray();
        }
        NextMainBoard = deepCopy(MainBoard);
        printBoard(MainBoard);
        printBoard(NextMainBoard);
//        System.err.println(ColorGroups);
	}
    static void printBoard(char board[][]) {
        System.err.println();
        for(int row = 0; row < MainBoardYDomain; ++row) {
            System.err.println( new String(board[row]));
        }
        System.err.println();
    }
    public static char[][] deepCopy(char[][] original) {
        if (original == null) {
            return null;
        }
        final char[][] result = new char[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }
	public boolean update() throws IOException {

        System.err.println("Clock " + globalPlanningBoard.getClock());

        String jointAction = "[";

		for ( int i = 0; i < agents.size() - 1; i++ ) {
            try {
                jointAction += agents.get( i ).act() + ",";
            }
            catch(UnsupportedOperationException exc) {
                printBoard(NextMainBoard);
                Conflicts.delegateConflict(agents.get(i));
                --i;
            }
        }
		jointAction += agents.get( agents.size() - 1 ).act() + "]";

		// Place message in buffer
		System.out.println( jointAction );
		System.err.println(jointAction);
		// Flush buffer
        System.out.flush();
        //server's output
        String percepts = in.readLine();
        if ( percepts == null )
            return false;
//        System.err.println(percepts);
        String[] results = percepts.replace("[","")
                            .replace("]","")
                            .replace(",", "")
                            .split(" "); //this is dumb but I don't know the simpler way to just read it into Array String in Java
        System.err.println(Arrays.toString(results));
        printBoard(MainBoard);
        for(int i= 0;i<results.length; ++i) {
            if(results[i].equals("true")) {
                agents.get(i).updatePosition();
            }
            else {
                agents.get(i).path = null;
            }
        }

        globalPlanningBoard.incrementClock();

		return true;
	}

	public static void main( String[] args ) {
		// Use stderr to print to console
		System.err.println( "Hello from NotSoRandomWalkClient. I am sending this using the error outputstream" );
		try {
			RandomWalkClient client = new RandomWalkClient();
            System.out.flush();
            System.err.println(client.in.readLine());
			while ( client.update() ) {
			}
		} catch ( IOException e ) {	
			// Got nowhere to write to probably
		}
	}
}
