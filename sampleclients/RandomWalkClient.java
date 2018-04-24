package sampleclients;


import sampleclients.room_heuristics.RoomAStar;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;


public class RandomWalkClient {

	private static Random rand = new Random();
	private BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
    public static MainBoard gameBoard;
    public static MainBoard nextStepGameBoard;
    public static RoomAStar roomMaster;
	public RandomWalkClient() {
        gameBoard = new MainBoard(in); //map read in the constructor
        nextStepGameBoard = new MainBoard(gameBoard);
        MainBoard.Dep = GoalDependency.getGoalDependency();
        GoalDependency.print();
        System.err.println("Goaldep ended");

        roomMaster = new RoomAStar(gameBoard);
/*		Agent someAgent = agents.get(2);
        LinkedList<Node> path = someAgent.findPathToBox(BoxColorGroups.get(someAgent.getColor()).get(2));
        System.err.println(path + " for Agent: " + someAgent);
        */
	}



	public boolean update() throws IOException {
	    //reset hasMoved for all agents
	    for (int i = 0; i < MainBoard.agents.size(); i++ ){
	        MainBoard.agents.get(i).hasMoved = false;
        }
        boolean allHasMoved = false;
	    String[] actions = new String[MainBoard.agents.size()];

	    while(!allHasMoved) {
            for (int i = 0; i < MainBoard.agents.size(); i++) {
                try {
                    if(!MainBoard.agents.get(i).hasMoved) {
                        System.err.println("Update agent: " + MainBoard.agents.get(i).getID());
                        actions[i] = MainBoard.agents.get(i).act();
                        MainBoard.agents.get(i).hasMoved = true;
                        System.err.println("Agent " + MainBoard.agents.get(i).getID() + " has moved with action: "+actions[i]);
                        System.err.println("Agent has path:");
                        if (MainBoard.agents.get(i).path != null) {
                            for (Node c : MainBoard.agents.get(i).path) {
                                System.err.println(c.action.toString());
                            }
                        }
                        System.err.println();
                    }
                } catch (UnsupportedOperationException exc) {
                    //printBoard(NextMainBoard);
                    System.err.println();
                    System.err.println("Conflict for agent: " + MainBoard.agents.get(i).getID() + " and action " + MainBoard.agents.get(i).path.get(0).action.toString());
                    System.err.println("path:");
                    System.err.println(MainBoard.agents.get(i).path);
                    System.err.println(MainBoard.agents.get(i).getAttachedBox());
                    System.err.println(nextStepGameBoard);
                    System.err.println(gameBoard);
                    Conflicts.delegateConflict(MainBoard.agents.get(i));
                    System.err.println();
                    //--i;

                }
            }

            allHasMoved=true;
            for (int j = 0; j < MainBoard.agents.size(); j++){
                System.err.println("Agent: "+ MainBoard.agents.get(j).getID()+" hasMoved: "+MainBoard.agents.get(j).hasMoved);
                allHasMoved = allHasMoved && MainBoard.agents.get(j).hasMoved;
            }
            System.err.println("allhasmoved: "+allHasMoved);
        }

        String jointAction = "[";
        // create joint actions
        for(int i = 0; i < actions.length-1; i++){
            jointAction+=actions[i]+",";
        }
        jointAction+=actions[actions.length-1]+"]";
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
            System.err.println(MainBoard.agents.get(i));
            System.err.println(MainBoard.agents.get(i).path);
            System.err.println("------------ Update board failed -------");
            throw exc;
        }
        return true;
    }

	public static void main( String[] args ) {

        new Debugger("levels/MAthomasAppartment.lvl");

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
