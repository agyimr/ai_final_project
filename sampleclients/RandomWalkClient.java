package sampleclients;


import sampleclients.room_heuristics.RoomAStar;

import java.io.*;
import java.util.*;


public class RandomWalkClient {

	private static Random rand = new Random();
	private BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
    public static MainBoard gameBoard;
    public static MainBoard nextStepGameBoard;
    public static RoomAStar roomMaster;
    public static AnticipationPlanning anticipationPlanning;

	public RandomWalkClient() {
        gameBoard = new MainBoard(in); //map read in the constructor
        nextStepGameBoard = new MainBoard(gameBoard);
        GoalDependency.getGoalDependency();
        roomMaster = new RoomAStar(gameBoard);
        anticipationPlanning = new AnticipationPlanning(gameBoard);
        assignGoals();
	}
    public void assignGoals() {
        for(Goal current : MainBoard.allGoals) {
            if (current.canBeSolved() && current.assignedBox != null) {
                current.findClosestBox();
            }
        }
    }


	public boolean update() throws IOException {
	    //reset hasMoved for all agents
        for (int i = 0; i < MainBoard.agents.size(); i++) {
            try {
                System.err.println("Update agent: " + MainBoard.agents.get(i).getID());
                MainBoard.agents.get(i).act();
                System.err.println();
            } catch (UnsupportedOperationException exc) {
                System.err.println();
                System.err.println("Conflict for agent: " + MainBoard.agents.get(i).getID() + " and action " + MainBoard.agents.get(i).path.get(0).action.toString());
                System.err.println(MainBoard.agents.get(i).getAttachedBox());
                System.err.println(nextStepGameBoard);
                System.err.println(gameBoard);
                Conflicts.delegateConflict(MainBoard.agents.get(i));
                System.err.println();
            }
        }

        String jointAction = "[";
        // create joint actions
        for(int i = 0; i < MainBoard.agents.size()-1; i++){
            jointAction+=MainBoard.agents.get(i).collectServerOutput()+",";
        }
        jointAction+=MainBoard.agents.get(MainBoard.agents.size()-1).collectServerOutput()+"]";
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
                    System.err.println( MainBoard.agents.get(i));
                    throw new NumberFormatException();                }
            }
        }
        catch (UnsupportedOperationException exc) {
            System.err.println("------------ Update board failed -------");
            System.err.println(MainBoard.agents.get(i));
            System.err.println(MainBoard.agents.get(i).path);
            System.err.println("------------ Update board failed -------");
            throw exc;
        }

        System.err.println("Clock " + anticipationPlanning.getClock());
        anticipationPlanning.incrementClock();
        anticipationPlanning.displayBoard();
        return true;
    }

	public static void main( String[] args ) {

        new Debugger("levels/MAthomasAppartment.lvl", 150);

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
