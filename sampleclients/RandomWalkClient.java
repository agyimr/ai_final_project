package sampleclients;



import java.io.*;
import java.util.*;


public class RandomWalkClient {

    public static Debugger debugger = null;

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
		for ( int i = 0; i < MainBoard.agents.size(); i++ ) {
            try {
                System.err.println("Update agent: "+MainBoard.agents.get(i).getID());
                jointAction += MainBoard.agents.get( i ).act() + ',';
                System.err.println("Agent has path:");
                if(MainBoard.agents.get(i).path != null){
                    for (Node c : MainBoard.agents.get(i).path){
                        System.err.println(c.action.toString());
                    }
                }
                System.err.println();
            }
            catch(UnsupportedOperationException exc) {
                //printBoard(NextMainBoard);
                System.err.println();
                System.err.println("Conflict for agent: "+MainBoard.agents.get(i).getID()+" and action "+MainBoard.agents.get(i).path.get(0).action.toString());
                System.err.println("path:");
                System.err.println(MainBoard.agents.get(i).path.toString());
                System.err.println(MainBoard.agents.get(i).getAttachedBox());
                Conflicts.delegateConflict(MainBoard.agents.get(i));
                System.err.println("\nAgent acts after conflict:"+MainBoard.agents.get(i).getID());
                jointAction += MainBoard.agents.get( i ).act() + ',';
                System.err.println();
                //--i;
                //throw exc;

            }
        }
		jointAction =  jointAction.substring(0,jointAction.length() - 1) +  "]";

		// Place message in buffer
        System.err.println(jointAction);
		System.out.println( jointAction );

		// Flush buffer
        System.out.flush();
        //server's output
        String percepts = in.readLine();
        if ( percepts == null )
            return false;
        String[] results = percepts.replace("[","")
                            .replace("]","")
                            .replace(",", "")
                            .split(" ");
        System.err.println("Results: "+ Arrays.toString(results));
        int i=0;
        System.err.println(nextStepGameBoard);
        System.err.println(gameBoard);
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
            System.err.println("------------ Update board failed -------");
            System.err.println(gameBoard);
            System.err.println(nextStepGameBoard);
            System.err.println(MainBoard.agents.get(i));
            System.err.println(MainBoard.agents.get(i).path);
            System.err.println("------------ Update board failed -------");
            throw exc;
        }
        return true;
    }

	public static void main( String[] args ) {
		// Use stderr to print to console

        //debugger = new Debugger(args[3]);
        debugger = new Debugger("levels/MAthomasAppartment.lvl");

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
