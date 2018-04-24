package sampleclients;

import java.awt.Point;
import java.util.*;

public class Conflicts {

	private static MainBoard mainBoard;

	public static void delegateConflict(Agent agent1){
		System.err.println();
		System.err.println( "Conflict Started for agent:"+agent1.getID() );
		mainBoard = RandomWalkClient.gameBoard;
		List<Agent> agents = MainBoard.agents;
		Agent agent2;
		int priority1 =	calculatePriority(agent1);
		int priority2;// = calculatePriority(agent2);
		Box box2;
		BasicObject bob = getConflictPartners(agent1);
		if(bob.getID() == agent1.getID()){
			System.err.println("Conflict detected with itself. ----- BUG ----");
			System.err.println("replanning instead");
			if(agent1.path != null){
				agent1.path.clear();
			}
		}

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
			if(pawnAgent.hasMoved){
				pawnAgent.revertMoveIntention(RandomWalkClient.nextStepGameBoard);
			}
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
				if(box2.assignedAgent.currentState == Agent.possibleStates.movingBox){
					agent2 = box2.assignedAgent;
					priority2 = calculatePriority(agent2);
					Agent pawnAgent = (priority1 < priority2) ? agent2 : agent1;
					Agent kingAgent = (priority1 > priority2) ? agent2 : agent1;
					//If an agent is assigned to the box

					System.err.println("pawn is:"+pawnAgent.getID());
					System.err.println("king is:"+kingAgent.getID());
					if(pawnAgent.hasMoved){
						pawnAgent.revertMoveIntention(RandomWalkClient.nextStepGameBoard);
					}
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
		oldPos.add(agent1.getCoordinates()); //add agent to pos
		if (agent1.isBoxAttached()) {
			oldPos.add(agent1.getAttachedBox().getCoordinates());
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
			pawnArea.add(pawnAgent.getAttachedBox().getCoordinates()); //FIX
		}
		
		List<Point> kingArea = new ArrayList<Point>();
		Command kingCommand = kingAgent.getCommand(0);
		
		
		kingArea.add(new Point(kingAgent.getX(),kingAgent.getY()));
		if(kingAgent.isBoxAttached()){
			kingArea.add(kingAgent.getAttachedBox().getCoordinates());
		}
		if(kingCommand != null){
			kingArea.addAll(kingCommand.getNext(kingArea));
		}

		
		
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

		pawnAgent.replacePath(newC);
		return true;
	}

	
	//For use in deciding who goes first in a simple conflict
	private static int calculatePriority(Agent agent1){
		int ID = agent1.getID();
		int heuristicsToGoal = 0;
		int prio = ID + heuristicsToGoal;
		if(agent1.currentState == Agent.possibleStates.inConflict){
			prio = -1;
		}
		if(agent1.currentState == Agent.possibleStates.movingBox){
			prio -= 100;
		}
		if(agent1.path == null || agent1.path.size() == 0 || agent1.path.peek().action.actType == Command.type.Noop){
			prio = 1000;
		}
		return prio;


	}
	//More difficult conflict where one needs to backtrack or go around with/without box
	
	private static boolean planMerge(Agent kingAgent, Agent pawnAgent) {
		System.err.println("planMerge Started");
		int index = 0;
		Point posKing = new Point(kingAgent.getX(), kingAgent.getY()); //Node 0 for the king
		List<Point> pos = new ArrayList<Point>();
		pos.add(posKing);
		if (kingAgent.isBoxAttached()) {
			Point posBox = kingAgent.getAttachedBox().getCoordinates();
			pos.add(posBox);
		}

		List<Point> pawnAgentPos = new LinkedList<Point>();
		pawnAgentPos.add(new Point(pawnAgent.getX(), pawnAgent.getY()));
		if (pawnAgent.currentState == Agent.possibleStates.movingBox) {
			pawnAgentPos.add(pawnAgent.getAttachedBox().getCoordinates());
		}

		System.err.println("planmerge pos: " + pos.toString());
		List<Point> locked = new ArrayList<Point>();
		Command tmpC;
		locked.addAll(pos);
		System.err.println("ka at: " + kingAgent.toString());
		boolean kingNoop = false;
		for (int i = 0; i < kingAgent.path.size(); i++) {
			tmpC = kingAgent.getCommand(i);

			pos = tmpC.getNext(pos);
			for (Point p : pos) {
				if (!locked.contains(p)) {
					locked.add(p);
				}
				if (i == 0 && pawnAgentPos.contains(p)) {
					kingNoop = true;
				}
			}

		}

		System.err.println("locked for bfs");
		for (Point p : locked) {
			System.err.println(p.toString());
		}


		List<Command> solution = ConflictBFS.doBFS(locked, pawnAgentPos, true);
		if (solution.size() == 0) {
			System.err.println();
			System.err.println("PLANMERGE FOUND NO SOLUTION while considering other agents");
			System.err.println();

			System.err.println();
			System.err.println("trying to find solution while not considering other agents");
			solution = ConflictBFS.doBFS(locked, pawnAgentPos, false);
			System.err.println();

			if (solution.size() == 0) {
				System.err.println();
				System.err.println("PLANMERGE FOUND NO SOLUTION while not considering other agents");
				System.err.println();
				return false;
			}


		}

		List<Command> kp = new LinkedList<Command>();
		Node oldn = kingAgent.path.peek();
		Node n = new Node(null, new Command(), kingAgent.getX(), kingAgent.getY());

		kingAgent.path.add(0,n);
		kingAgent.wake();
		pawnAgent.wake();
		solution.add(new Command());
		pawnAgent.replacePath(solution);

		System.err.println("PlanMerge found solution with pawn agent "+pawnAgent.getID()+":");
		for(Command c: solution){
			System.err.println(c.toString());
		}
		System.err.println("and king agent "+kingAgent.getID()+":");
		for(Node c: kingAgent.path){
			System.err.println(c.action.toString());
		}
		kingAgent.nextState = kingAgent.currentState;
		System.err.println("KingAgentNextState = "+kingAgent.nextState);
		pawnAgent.nextState = Agent.possibleStates.inConflict;
		System.err.println("PawnAgentNextState = "+pawnAgent.nextState);

		return true;
	}
	private static boolean bid(Box box,Agent a){
		System.err.println(" bid NotDoneYet, just clearing path for replanning");
		List<Agent> agents = MainBoard.agents;
		a.path.clear();
		//getRelevantAgents(box,agents);
		//attached?
		return true;
	}
	
}
