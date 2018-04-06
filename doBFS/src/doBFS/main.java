package doBFS;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.io.*;

import doBFS.Command.dir;
import doBFS.Command.type;

public class main {
	private static Random rand = new Random();
	private static BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
	private static List< Agent > agents = new ArrayList< Agent >();
    private static List< Box > boxes = new ArrayList< Box >();
    
	public static int MainBoardYDomain = 0, MainBoardXDomain = 0;
    public static char[][] MainBoard; //every state change is seen on the main board
    public static boolean isAgent (char id) { return ( '0' <= id && id <= '9' );}
    public static boolean isBox (char id) { return ( 'A' <= id && id <= 'Z' );}
    public static boolean isGoal (char id) { return ( 'a' <= id && id <= 'z' ); }
    public static boolean isWall (char id) {return (id == '+');}
	
	public static void main(String[] args) throws IOException {
		
		readMap();
		
		System.out.println();
		Point p0 = null;
		Point p1 = null;
		List<Point> posPawn = new ArrayList<Point>();
		List<Point> locked = new ArrayList<Point>();
		//assign two two agents to two points
		for(Agent a : agents){
			if(a.getID() == '0'){
				p0 = new Point(a.getX(),a.getY());
				locked.add(p0);
			}
			if(a.getID() == '1'){
				p1 = new Point(a.getX(),a.getY());
				posPawn.add(p1);
			}
		}
		for(Box a : boxes){
			if(a.getID() == 'B'){
				p0 = new Point(a.getX(),a.getY());
				posPawn.add(p0);
			}
		}
		
		
		//Plan for agent 0
		List<Command> plan = new ArrayList<Command>();
		plan.add(new Command(dir.E));
		plan.add(new Command(dir.E));
		plan.add(new Command(dir.E));
		plan.add(new Command(dir.E));
		plan.add(new Command(dir.E));
		
		//generate locked points from plan
		Point cur = locked.get(0);
		while(!plan.isEmpty()){
			cur = plan.get(0).getNext(cur);
			plan.remove(0);
			locked.add(cur);
		}
		
		System.out.println("Locked Points:");
		for(Point p : locked){
			System.out.println(p.toString());
		}
		System.out.println();
		
		
		//calculate solution
		List<Command> solution = Conflict.doBFS(locked, posPawn, MainBoard);
		
		System.out.println("Solution:");
		for(Command c : solution){
			System.out.println(c.toActionString());
		}
		
	}
	
	private static void readMap() throws IOException {
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

        MainBoard = new char[MainBoardYDomain][MainBoardXDomain];
        for(int row = 0; row < MainBoardYDomain; ++row) {
            MainBoard[row] = table.get(row).toCharArray();
        }
        printBoard(MainBoard);
	}

    static void printBoard(char board[][]) {
        System.err.println();
        for(int row = 0; row < MainBoardYDomain; ++row) {
            System.err.println( new String(board[row]));
        }
        System.err.println();
    }
}
