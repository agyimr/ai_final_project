package sampleclients;

import sampleclients.ConflictBFS;
import java.awt.Point;
import java.io.*;
import java.util.*;

public class Conflicts {

	private static MainBoard mainBoard;

	public static void delegateConflict(Agent agent1){
		//Easy nop case  TODO
		mainBoard = RandomWalkClient.gameBoard;
		List<Agent> agents = mainBoard.agents;
		Agent agent2;
		int priority1 =	calculatePriority(agent1);
		int priority2;// = calculatePriority(agent2);
		Box box2;
		BasicObject bob = getConflictPartners(agent1);

		if (bob instanceof Agent){
			agent2 = (Agent) bob;
			priority2 = calculatePriority(agent2);
			Agent pawnAgent = (priority1 > priority2) ? agent2 : agent1;
			Agent kingAgent = (priority1 < priority2) ? agent2 : agent1;
			planMerge(kingAgent,pawnAgent);

		} else if (bob instanceof Box){
			box2 = (Box) bob;
			if(box2.assignedAgent==null){
				bid(box2);
			} else{
				agent2 = box2.assignedAgent;
				priority2 = calculatePriority(agent2);
				Agent pawnAgent = (priority1 > priority2) ? agent2 : agent1;
				Agent kingAgent = (priority1 < priority2) ? agent2 : agent1;
				//If an agent is assigned to the box
				planMerge(kingAgent,pawnAgent);
			}
		}
	}

	private static BasicObject getConflictPartners(Agent agent1) {
		Command c = agent1.getCommand(0); //Find command for agent in path
		List<Point> pos = new ArrayList<Point>(); //Pos array handles the positions of agent and maybe box
		pos.add(agent1.getAgentPoint()); //add agent to pos
		if (agent1.isBoxAttached()) {
			pos.add(agent1.getAttachedBoxPoint());
		} //Add box if exists
		List<Point> nextPos = c.getNext(pos); //nextPos dependant on if box or not
		Point nextPosition = null;
		for (int i = 0; i < nextPos.size(); i++) {
			if (!pos.contains(nextPos.get(i))) {
				nextPosition = nextPos.get(i);
			}
		}
		return mainBoard.getElement((int) nextPosition.getX(), (int) nextPosition.getY());

	}

	//This method is for detecting and delegating the type of conflict to the correct methods
	private static boolean noopFix(Agent pawnAgent, Agent kingAgent){
		//Find next two points for king, if intersects with pawnAgent pos, return false, else true.
		List<Point> pawnArea = new ArrayList<Point>();		
		pawnArea.add(new Point(pawnAgent.getX(),pawnAgent.getY()));
		
		if(pawnAgent.isBoxAttached()){
			pawnArea.add(pawnAgent.getAttachedBoxPoint()); //FIX
		}
		
		List<Point> kingArea = new ArrayList<Point>();
		Command kingCommand = kingAgent.getCommand(0);
		
		
		kingArea.add(new Point(kingAgent.getX(),kingAgent.getY()));
		int ij =1;
		if(kingAgent.isBoxAttached()){
			ij--;
			kingArea.add(kingAgent.getAttachedBoxPoint());
		}
		kingArea.addAll(kingCommand.getNext(kingArea));
		
		
		
		for (int i = ij; i < 3; i++) {
			kingCommand = kingAgent.getCommand(0);
			kingArea = kingCommand.getNext(kingArea);
			if(kingArea.contains(pawnArea)){
				return false;
			}
		}
		/*if(kingAgent.isBoxAttached()){
			pawnAgent.path.add(new Command());
		}
		pawnAgent.path.add(new Command());
		pawnAgent.path.add(new Command());*/
		return true;
	}

	
	//For use in deciding who goes first in a simple conflict
	private static int calculatePriority(Agent agent1){
		int ID = agent1.getID();
		int heuristicsToGoal = 0;
		int prio = ID + heuristicsToGoal;
		return prio;


	}
	//More difficult conflict where one needs to backtrack or go around with/without box
	
	private static boolean planMerge(Agent kingAgent, Agent pawnAgent){
		char[][] board = RandomWalkClient.MainBoard;
		int index = 0;
		Point posKing = new Point(kingAgent.getX(),kingAgent.getY()); //Node 0 for the king
		List<Point> pos = new ArrayList<Point>();
		pos.add(posKing);
		if(kingAgent.isBoxAttached()){
			Point posBox = kingAgent.getAttachedBoxPoint();
			pos.add(posBox);
		}


		//Run though path 7 times, and return a list of those points?
		int numLocked = kingAgent.path.size();
		List<Point> locked = new ArrayList<Point>();
		Command tmpC;
		for (int i = 0; i < numLocked; i++) {
			tmpC = kingAgent.getCommand(i);

			pos = tmpC.getNext(pos);
			for (Point p:pos) {
				if(!locked.contains(p)){
					locked.add(p);
				}
			}
		}
		List<Command> solution = ConflictBFS.doBFS(locked, pos);
		if(solution.size() == 0){
			return false;
			//bid()
		}
		if(solution.size() == 0){
			solution.add(new Command());
		}
		pawnAgent.replacePath(solution);
		return true;
	}
	private static boolean bid(Box box){
		System.err.println("NotDoneYet");
		List<Agent> agents = mainBoard.agents;
		//getRelevantAgents(box,agents);
		//attached?
		return true;
	}
	
	
	
}







//		int i;
//		if(kingAgent.isBoxAttached()){
//			i=2;
//		} else{
	
//			i=1;
//		}
//
//		Node temporaryPosition = null;
//		List<Node> posKing = new ArrayList<Node>();
//		List<Node> posPawn = new ArrayList<Node>();
//		for (int j = 0; j < i; j++) {
//			Command ck = kingAgent.path.get(j);
//			Command cp = pawnAgent.path.get(j);
//
//			if(temporaryPosition != null){
//				posKing = ck.getNext(new Point(temporaryPosition.getX(),temporaryPosition.getY()));
//				posPawn = cp.getNext(new Point(pawnAgent.getX(),pawnAgent.getY()));
//			} else{
//				posKing = ck.getNext(new Point(kingAgent.getX(),kingAgent.getY()));
//				posPawn = cp.getNext(new Point(pawnAgent.getX(),pawnAgent.getY()));
//			}
//
//			if(!posPawn.contains(posKing)){
//
//				break;
//			}else{
//				pawnAgent.path.add(new Command());
//				pawnAgent.path.add(new Command());
//				if(posKing.contains(pawnPoint)){ //Check if resolved?
//					planMerge(kingAgent,pawnAgent,board);
//					kingAgent.path.add(new Command());
//					kingAgent.path.add(new Command());
//				}
//			}
//			temporaryPosition = posKing.get(0);
//		}


//Loop
// reverse af sidste commando til BFS
// Sig til BFS at der ikke skal s�ges i 2 af retningerne
// list<commando> returneret, nop king 1 gang, og till�g dette til planen
// Ellers hvis NULL returneret, g� 1 bagud, og BFS igen.
//Der skal hele tiden holdes �je med positionen i planen.