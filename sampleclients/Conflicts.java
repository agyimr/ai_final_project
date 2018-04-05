package sampleclients;

import java.io.*;
import java.util.*;

import com.sun.javafx.scene.paint.GradientUtils.Point;

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
	
	public List<String> BackTrack(char[][] map, List<String> plan, Point p1, Point p2 ){
		Point agent1 = p1, agent2=p2;
		List<String> newP = new ArrayList<String>();
		
		int planPointer = 0;
		while(true) {
			List<Point> blocked = new ArrayList<Node>();
			Point blocked1 = agent1;
			Point blocked2 = getPointFrom(plan.get(planPointer),agent1);
			
			Point blocked3 = blocked2;
			if(planPointer < plan.size()-1) {
				blocked3 = getPointFrom(plan.get(planPointer+1),blocked2);
			}
			
			blocked.add(blocked1);
			blocked.add(blocked2);
			blocked.add(blocked3);
//			System.out.println();
//			System.out.println("iteration:"+planPointer);
//			System.out.println("agent1:"+agent1);
//			System.out.println("agent2:"+agent2);
//			System.out.println();
			
			//////////// Searh directions ///////////////
			Point candidat;
			//North
			candidat = getPointFrom("Move(N)",agent2);
			if(!blocked.contains(candidat) && map[candidat.y][candidat.x] == ' ') {
				newP.add("Move(N)");
				break;
			}
			
			//South
			candidat = getPointFrom("Move(S)",agent2);
			if(!blocked.contains(candidat) && map[candidat.y][candidat.x] == ' ') {
				newP.add("Move(S)");
				break;
			}
			//West
			candidat = getPointFrom("Move(W)",agent2);
			if(!blocked.contains(candidat) && map[candidat.y][candidat.x] == ' ') {
				newP.add("Move(W)");
				break;
			}
			//East
			candidat = getPointFrom("Move(E)",agent2);
			if(!blocked.contains(candidat) &&  map[candidat.y][candidat.x] == ' ') {
				newP.add("Move(E)");
				break;
			}
			////////////////////////////////////////////
			
			
			//if no solution
			planPointer++;
			newP.add(plan.get(planPointer));
			
			agent1 = blocked2;
			agent2 = blocked3;
			
			//System.out.println(newP.get(0));
		}
		
		
		return newP;
	}
	
	
	public static Point getPointFrom(String action,Point p) {
		Point newPoint = (Point) p.clone();
		if(action == "Move(N)") {
			newPoint.y--;
			return newPoint;
		}
		if(action == "Move(S)") {
			newPoint.y++;
			return newPoint;
		}
		if(action == "Move(E)") {
			newPoint.x++;
			return newPoint;
		}
		if(action == "Move(W)") {
			newPoint.x--;
			return newPoint;
		}
		return null;
		
	}
	public static void printMap(char[][] m) {
		for(int i = 0;i<m.length;i++) {
			for(int j = 0; j<m[0].length;j++) {
				System.out.print(m[i][j]);
			}
			System.out.println();
		}
	}
}
}
