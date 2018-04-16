package sampleclients;

import java.io.*;
import java.util.*;


public class RandomWalkClient {
	private static Random rand = new Random();
	private BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );

    public static MainBoard gameBoard;
    public static MainBoard nextStepGameBoard;
	public RandomWalkClient() throws IOException {
        gameBoard = new MainBoard(in); //map read in the constructor
        nextStepGameBoard = new MainBoard(gameBoard);
/*		Agent someAgent = agents.get(2);
        LinkedList<Node> path = someAgent.findPathToBox(BoxColorGroups.get(someAgent.getColor()).get(2));
        System.err.println(path + " for Agent: " + someAgent);*/
	}



	public boolean update() throws IOException {

		String jointAction = "[";
		for ( int i = 0; i < MainBoard.agents.size() - 1; i++ ) {
            try {
                jointAction += MainBoard.agents.get( i ).act() + ",";
            }
            catch(UnsupportedOperationException exc) {
                //printBoard(NextMainBoard);
                System.err.println("Conflict");
                Conflicts.delegateConflict(MainBoard.agents.get(i));
                //--i;
                //throw exc;

            }
        }
		jointAction += MainBoard.agents.get( MainBoard.agents.size() - 1 ).act() + "]";

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
        System.err.println("Results: "+ Arrays.toString(results));
        int i=0;
        try {
            for(i= 0;i<results.length; ++i) {
                if(results[i].equals("true")) {
                    MainBoard.agents.get(i).updatePosition();
                }
                else {
                    MainBoard.agents.get(i).path = null;
                }
            }
        }
        catch (UnsupportedOperationException exc) {
            System.err.println(gameBoard);
            System.err.println(nextStepGameBoard);
            System.err.println(MainBoard.agents.get(i));
            System.err.println(MainBoard.agents.get(i).path);
            throw exc;
        }
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
