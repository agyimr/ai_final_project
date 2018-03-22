package sampleclients;

import java.io.*;
import java.util.*;



public class RandomWalkClient {
	private static Random rand = new Random();
	private BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
	private List< Agent > agents = new ArrayList< Agent >();
    private List< Box > boxes = new ArrayList< Box>();

    //GLOBAL HERE
    public static int MainBoardYDomain = 0, MainBoardXDomain = 0;
    public static char[][] MainBoard; //every state change is seen on the main board
    public static boolean isAgent (char id) { return ( '0' <= id && id <= '9' );}
    public static boolean isBox (char id) { return ( 'A' <= id && id <= 'Z' );}
    public static boolean isGoal (char id) { return ( 'a' <= id && id <= 'z' ); }
    public static boolean isWall (char id) {return (id == '+');}
	public RandomWalkClient() throws IOException {
		readMap();
		Agent someAgent = agents.get(2);
        LinkedList<Node> path = someAgent.findPath(5, 5);
        System.err.println(path + " for Agent: " + someAgent);
	}

	private void readMap() throws IOException {
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
        ArrayList<String> table = new ArrayList<>();
		int currentColumn = 0;
        while ( !line.equals( "" ) ) {
            for ( int i = 0; i < line.length(); i++ ) {
                char id = line.charAt( i );
                if (isAgent(id)) {
                    agents.add( new Agent( id, colors.get( id ), MainBoardYDomain, currentColumn) );
                }
                else if (isBox(id)) {
                    boxes.add( new Box( id, colors.get( id ), MainBoardYDomain, currentColumn ) );
                }

                ++currentColumn;
            }
//            System.err.println(line); //prints a map
            if(MainBoardXDomain < currentColumn) {
                MainBoardXDomain = currentColumn; }
            table.add(line);
            line = in.readLine();
            currentColumn = 0;
            ++MainBoardYDomain;
        }


        Collections.sort(agents, (left, right) -> left.getID() - right.getID());


        MainBoard = new char[MainBoardYDomain][MainBoardXDomain];
        for(int row = 0; row < MainBoardYDomain; ++row) {
            MainBoard[row] = table.get(row).toCharArray();
        }
        printBoard(MainBoard);
        System.err.println(agents);
	}

    void printBoard(char board[][]) {
        System.err.println();
        for(int row = 0; row < MainBoardYDomain; ++row) {
            System.err.println( new String(board[row]));
        }
        System.err.println();
    }

	public boolean update() throws IOException {
		String jointAction = "[";

		for ( int i = 0; i < agents.size() - 1; i++ )
			jointAction += agents.get( i ).act() + ",";
		
		jointAction += agents.get( agents.size() - 1 ).act() + "]";

		// Place message in buffer
		System.out.println( jointAction );
		System.err.println(jointAction);
//		printBoard(MainBoard);
		// Flush buffer
		System.out.flush();

		// Disregard these for now, but read or the server stalls when its output buffer gets filled!
		String percepts = in.readLine();
		if ( percepts == null )
			return false;

		return true;
	}

	public static void main( String[] args ) {

		// Use stderr to print to console
		System.err.println( "Hello from RandomWalkClient. I am sending this using the error outputstream" );
		try {
			RandomWalkClient client = new RandomWalkClient();
			while ( client.update() )
				;

		} catch ( IOException e ) {
			// Got nowhere to write to probably
		}
	}
}
