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
		int prio1 =	calculatePriority(agent1);
		int prio2 = calculatePriority(agent2);
		Agent pawn = (prio1 > prio2) ? agent2 : agent1;
		Agent king = (prio1 < prio2) ? agent2 : agent1;
		int i;
		if(king.isBoxAttached()){
			//twice
			i=2;
		} else{
			i=1;
		}
		//TODO (J=1/0, første action poppet?)

		Node tmpPosA = null;
		List<Node> posPrio;
		List<Node> posPawn;
		for (int j = 1; j < i; j++) {
			Command ck = king.path.get(j);
			Command cp = pawn.path.get(j);

			if(tmpPosA != null){
				posPrio = ck.getNext(tmpPosA.getX(),tmpPosA.getY());
				posPawn = cp.getNext(pawn.getX(),pawn.getY());
			} else{
				posPrio = ck.getNext(king.getX(),king.getY());
				posPawn = cp.getNext(pawn.getX(),pawn.getY());
			}

			//TODO Node == Pos CHANGE?

			//
			if(!posPawn.contains(posPrio)){

				break;
			}else{
				pawn.path.add(new Command());
				if(posPrio.contains(pawnPoint)){
					planMerge(king,pawn,board);
				}
			}
			tmpPosA = posPrio.get(0);
		}

	}
	//For use in deciding who goes first in a simple conflict
	private int calculatePriority(Agent agent1){
		int ID = agent1.getID();
		int heuristicsToGoal = 0;
		int prio = ID + heuristicsToGoal;
		return prio;


	}
	//More difficult conflict where one needs to backtrack or go around with/without box
	
	private void planMerge(Agent kingAgent, Agent pawnAgent, char[][] board){
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
	}
}









//Loop
// reverse af sidste commando til BFS
// Sig til BFS at der ikke skal søges i 2 af retningerne
// list<commando> returneret, nop king 1 gang, og tillæg dette til planen
// Ellers hvis NULL returneret, gå 1 bagud, og BFS igen.
//Der skal hele tiden holdes øje med positionen i planen.