package sampleclients;

import sampleclients.ConflictBFS;
import java.awt.Point;
import java.io.*;
import java.util.*;

public class Conflicts {

	private static MainBoard mainBoard;

	public static void delegateConflict(Agent agent1){
		System.err.println();
		System.err.println( "Conflict Started for agent:"+agent1.getID() );
		mainBoard = RandomWalkClient.gameBoard;
		List<Agent> agents = mainBoard.agents;
		Agent agent2;
		int priority1 =	calculatePriority(agent1);
		int priority2;// = calculatePriority(agent2);
		Box box2;
		BasicObject bob = getConflictPartners(agent1);
		if(bob == null){
			System.err.println("conflict caught but not detected");
			if(agent1.path != null){
				agent1.path.clear();
			}
			return;

		}
		System.err.println("Conflict partner:" + bob.toString() );
		if (bob instanceof Agent){
			agent2 = (Agent) bob;
			priority2 = calculatePriority(agent2);
			Agent pawnAgent = (priority1 < priority2) ? agent2 : agent1;
			Agent kingAgent = (priority1 > priority2) ? agent2 : agent1;
			System.err.println("pawn is:"+pawnAgent.getID());
			System.err.println("king is:"+kingAgent.getID());
			if(!noopFix(pawnAgent,kingAgent)){
				if(!planMerge(kingAgent,pawnAgent)){
					System.err.println("clear path for pawn");
					if(pawnAgent.path != null){
						pawnAgent.path.clear();
					}

				}
			}


		} else if (bob instanceof Box){
			box2 = (Box) bob;
			if(box2.assignedAgent==null){
				bid(box2,agent1);
			} else{
				if(box2.assignedAgent.isMovingBox){
					agent2 = box2.assignedAgent;
					priority2 = calculatePriority(agent2);
					Agent pawnAgent = (priority1 < priority2) ? agent2 : agent1;
					Agent kingAgent = (priority1 > priority2) ? agent2 : agent1;
					//If an agent is assigned to the box

					System.err.println("pawn is:"+pawnAgent.getID());
					System.err.println("king is:"+kingAgent.getID());
					if(!noopFix(pawnAgent,kingAgent)){
						if(!planMerge(kingAgent,pawnAgent)){
							System.err.println("clear path for pawn");
							if(pawnAgent.path != null){
								pawnAgent.path.clear();
							}
						}
					}
				}
				else{
					bid(box2,agent1);
				}

			}
		}
		System.err.println("Conflict done");
	}

	private static BasicObject getConflictPartners(Agent agent1) {
		System.err.println( "getConflictPartners" );
		Command c = agent1.getCommand(0); //Find command for agent in path
		List<Point> oldPos = new ArrayList<Point>(); //Pos array handles the positions of agent and maybe box
		oldPos.add(agent1.getAgentPoint()); //add agent to pos
		if (agent1.isBoxAttached()) {
			oldPos.add(agent1.getAttachedBoxPoint());
			System.err.println( "Agent has box attached" );
		} //Add box if exists
		List<Point> newPos = c.getNext(oldPos); //nextPos dependant on if box or not
		Point conflictPos = null;

		for (int i = 0; i < newPos.size(); i++) {

			if (!oldPos.contains(newPos.get(i))) {
				conflictPos = newPos.get(i);
			}
		}

		BasicObject b = RandomWalkClient.nextStepGameBoard.getElement((int) conflictPos.getX(), (int) conflictPos.getY());
		if(b == null){
			b = RandomWalkClient.gameBoard.getElement((int) conflictPos.getX(), (int) conflictPos.getY());
		}
		return b;

	}

	//This method is for detecting and delegating the type of conflict to the correct methods
	private static boolean noopFix(Agent pawnAgent, Agent kingAgent){
		System.err.println( "NoopFix" );
		//Find next two points for king, if intersects with pawnAgent pos, return false, else true.
		List<Point> pawnArea = new ArrayList<Point>();		
		pawnArea.add(new Point(pawnAgent.getX(),pawnAgent.getY()));
		
		if(pawnAgent.isBoxAttached()){
			pawnArea.add(pawnAgent.getAttachedBoxPoint()); //FIX
		}
		
		List<Point> kingArea = new ArrayList<Point>();
		Command kingCommand = kingAgent.getCommand(0);
		
		
		kingArea.add(new Point(kingAgent.getX(),kingAgent.getY()));
		if(kingAgent.isBoxAttached()){
			kingArea.add(kingAgent.getAttachedBoxPoint());
		}
		kingArea.addAll(kingCommand.getNext(kingArea));
		
		
		if(kingAgent.path != null) {
			for (int i = 0; i < kingAgent.path.size(); i++) {
				kingCommand = kingAgent.getCommand(i);
				kingArea = kingCommand.getNext(kingArea);
				for (Point p : pawnArea) {
					if (kingArea.contains(p)) {
						return false;
					}
				}

			}
		}

		LinkedList<Command> newC = new LinkedList<Command>();

		if(kingAgent.isBoxAttached()){
			newC.add(new Command());
		}
		newC.add(new Command());
		newC.add(new Command());
		newC.add(new Command());

		pawnAgent.replacePath(newC);

		return true;
	}

	
	//For use in deciding who goes first in a simple conflict
	private static int calculatePriority(Agent agent1){
		int ID = agent1.getID();
		int heuristicsToGoal = 0;
		int prio = ID + heuristicsToGoal;
		if(agent1.conflictSteps > 0){
			prio = -1;
		}
		return prio;


	}
	//More difficult conflict where one needs to backtrack or go around with/without box
	
	private static boolean planMerge(Agent kingAgent, Agent pawnAgent){
		System.err.println( "planMerge Started" );
		int index = 0;
		Point posKing = new Point(kingAgent.getX(),kingAgent.getY()); //Node 0 for the king
		List<Point> pos = new ArrayList<Point>();
		pos.add(posKing);
		if(kingAgent.isBoxAttached()){
			Point posBox = kingAgent.getAttachedBoxPoint();
			pos.add(posBox);
		}

		List<Point> pawnAgentPos = new LinkedList<Point>();
		pawnAgentPos.add(new Point(pawnAgent.getX(),pawnAgent.getY()));
		if (pawnAgent.isBoxAttached()){
			pawnAgentPos.add(pawnAgent.getAttachedBoxPoint());
		}

		System.err.println("planmerge pos: "+pos.toString()) ;
		List<Point> locked = new ArrayList<Point>();
		Command tmpC;

		System.err.println("ka at: "+kingAgent.toString());
		boolean kingNoop = false;
		for (int i = 0; i < kingAgent.path.size(); i++) {
			tmpC = kingAgent.getCommand(i);

			pos = tmpC.getNext(pos);
			for (Point p:pos) {
				if(!locked.contains(p)){
					locked.add(p);
				}
				if(i == 0 && pawnAgentPos.contains(p)){
					kingNoop = true;
				}
			}

		}

		System.err.println("locked for bfs");
		for (Point p : locked){
			System.err.println(p.toString());
		}



		List<Command> solution = ConflictBFS.doBFS(locked, pawnAgentPos);
		if(solution.size() == 0){
			System.err.println();
			System.err.println("PLANMERGE FOUND NO SOLUTION");
			System.err.println();
			return false;
		}

		System.err.println("PlanMerge found solution with agent "+pawnAgent.getID()+":");
		for(Command c: solution){
			System.err.println(c.toString());
		}

		pos.clear();
		pos.add(posKing);
		if(kingAgent.isMovingBox){
			pos.add(kingAgent.getAttachedBoxPoint());
		}
		List<Command> kp = new LinkedList<Command>();
		if(kingNoop){

			kp.add(new Command());
			kingAgent.replacePath(kp);
		}

		solution.add(new Command());


		pawnAgent.replacePath(solution);
		return true;
	}
	private static boolean bid(Box box,Agent a){
		System.err.println(" bid NotDoneYet, just clearing path for replanning");
		List<Agent> agents = mainBoard.agents;
		a.path.clear();
		//getRelevantAgents(box,agents);
		//attached?
		return true;
	}
	
	
	
}
