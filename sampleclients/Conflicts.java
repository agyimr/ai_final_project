package sampleclients;

import java.awt.Point;
import java.io.*;
import java.util.*;

public class Conflicts {
	public static char[][] MainBoard;
	private List< Agent > agents;
	private List< Box > boxes;
	
	//This method is for detecting and delegating the type of conflict to the correct methods
	private void delegateConflictType(Agent agent1, Agent agent2, char[][] board){
		//Easy nop case  TODO
		int priority1 =	calculatePriority(agent1);
		int priority2 = calculatePriority(agent2);
		Agent pawnAgent = (priority1 > priority2) ? agent2 : agent1;
		Agent kingAgent = (priority1 < priority2) ? agent2 : agent1;
		
		if(!noopFix(pawnAgent, kingAgent)){
			if(!planMerge(kingAgent,pawnAgent,board)){
				if(!Bid()){
					system.err.println("Conflict can not be resolved on kingAgent"+kingAgent.getID+"and pawnAgent"+pawnAgent.getID);
				}
			}
		}			
	}
	
	private boolean noopFix(Agent pawnAgent, Agent kingAgent){
		//Find next two points for king, if intersects with pawnAgent pos, return false, else true.
		List<Point> pawnArea = new ArrayList<Point>();		
		pawnArea.add(new Point(pawnAgent.getX(),pawnAgent.getY()));
		
		if (pawnAgent.isBoxAttached){
			pawnArea.add(new Point(pawnAgent.getBoxX,pawnAgent.getBoxY)); //FIX 
		}
		
		List<Point> kingArea = new ArrayList<Point>();
		Command kingCommand = kingAgent.path.get(0);
		
		
		kingArea.add(new Point(kingAgent.getX(),kingAgent.getY()));
		int i =1;
		if(kingAgent.isBoxAttached){
			i--;
			kingArea.add(new Point(kingAgent.getBoxX,kingAgent.getBoxY));
		}
		kingArea.add(kingCommand.getNext(kingArea));
		
		
		
		for (i; i < 3; i++) {
			kingCommand = kingAgent.path.get(i);
			kingArea = kingCommand.getNext(kingArea);
			if(kingArea.contains(pawnArea){
				return false;
			}
		}
		if(kingAgent.isBoxAttached){
			pawnAgent.path.add(new Command());
		}
		pawnAgent.path.add(new Command());
		pawnAgent.path.add(new Command());
		return true;
	}

	
	//For use in deciding who goes first in a simple conflict
	private int calculatePriority(Agent agent1){
		int ID = agent1.getID();
		int heuristicsToGoal = 0;
		int prio = ID + heuristicsToGoal;
		return prio;


	}
	//More difficult conflict where one needs to backtrack or go around with/without box
	
	private boolean planMerge(Agent kingAgent, Agent pawnAgent, char[][] board){
		int index = 0;
		Point posKing = new Point(kingAgent.getX(),kingAgent.getY()); //Node 0 for the king
		//How do i get box position?
		Point posBox = new Point(posKing.getBoxX(),posKing.getBoxY());
		
		List<Point> pos = new List<Point>();
		pos.add(posKing);
		pos.add(posBox);
		//Run though path 7 times, and return a list of those points?
		int numLocked = 7;
		List<Point> locked = new List<Point>();
		
		for (int i = 0; i < numLocked; i++) {
			Node tmpPos = kingAgent.path.get(i);
			Point tmpPoint = new Point(tmpPos.getX(),tmpPos.getY());
			locked.add(tmpPoint);
		}
		List<Command> solution = doBFS(locked, pos, board);
		if(solution.length == 0){
			return false;
		}
		return true
	}
	
	
	//private boolean bid(){
		
		
	//}
	
	
	
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
// Sig til BFS at der ikke skal søges i 2 af retningerne
// list<commando> returneret, nop king 1 gang, og tillæg dette til planen
// Ellers hvis NULL returneret, gå 1 bagud, og BFS igen.
//Der skal hele tiden holdes øje med positionen i planen.