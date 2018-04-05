package sampleclients;

import java.io.*;
import java.util.*;

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
}
