package sampleclients;

import java.awt.Point;
import java.io.*;
import java.util.*;

import javax.xml.bind.helpers.NotIdentifiableEventImpl;

public class Conflicts {
	public static char[][] MainBoard;
	private List< Agent > agents;
	private List< Box > boxes;

	public void handleConflict(String response, List<Command> jact) {
		String tmp = response.substring(1, response.length());
		String[] parts = tmp.split(", ");
		Boolean boolean1;
		for (int i = 0; i < parts.length; i++) {
			boolean1 = Boolean.valueOf(parts[i]);
			if(boolean1 == false){
				System.err.println("conflict on agent: "+i);
				getConflictPartner(i, jact);
			}
		}
	}
	//Check in small area on map for partners in conflict and type of conflict
	private int getConflictPartner(int id1,List<Command> jact) {

		List<Node> reservedNodes = jact.get(id1).getReservedNodes(agents.get(id1).getX(),agents.get(id1).getY());


		//go through each agent except itself
		for(int i = 0; i < agents.size();i++) {
			if(i != id1) {

				//get reservedNodes of next agent
				List<Node> rn = jact.get(i).getReservedNodes(agents.get(i).getX(),agents.get(i).getY());
				for(int j = 0; j<reservedNodes.size();j++) {

					//if they share a reserved Node they are in conflict with each other
					if(rn.contains(reservedNodes.get(j))) {
						return i;
					}
				}
			}
		}
		return id1;
	}
	//This method is for detecting and delegating the type of conflict to the correct methods
	private void delegateConflictType(Agent agent1, Agent agent2, char[][] board){
		//Easy nop case  TODO
		int prio1 =	calculatePriority(agent1);
		int prio2 = calculatePriority(agent2);
		Agent pawn = (prio1 > prio2) ? agent2 : agent1;
		Agent king = (prio1 < prio2) ? agent2 : agent1;

		if(king.boxAttached){
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
				pawn.path.add(NOOP)
				if(posPrio.contains(pawnPoint)){
					planmerge();
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
	private void planMerge(){
		//not impl
	}
