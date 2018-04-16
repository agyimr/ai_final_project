package sampleclients;

import java.io.*;
import java.util.*;
import java.awt.Point;
import sampleclients.Command.dir;
import sampleclients.Command.type;

public class ConflictBFS {
	private static MainBoard map;
	public static List<Command> doBFS(List<Point> locked, List<Point> pos){
		map = RandomWalkClient.gameBoard;
		List<ConflictNode> frontier = new ArrayList<ConflictNode>();
		List<ConflictNode> explored = new ArrayList<ConflictNode>();
		List<Command> path = new ArrayList<Command>();
		
		//Add the current agent position to explored
		frontier.add(new ConflictNode(pos));
		
		//continue search as long as there are points in the firstfrontier
		while(!frontier.isEmpty()) {
			//pop the first element
			ConflictNode cur = frontier.get(0);
			frontier.remove(0);
			
			
//			System.out.println("Current ConflictNode: "+cur.toString());
//			System.out.println();
			
			//goal check - not in any locked points
			if(!containsList(locked,cur.getPoints())){
				path = generateGoalPath(cur);
				break;
			}
			
			
			//Get neighbour states of cur
			List<ConflictNode> neighbours = getNeighbours(cur, pos);
			
			//add the current ConflictNode to explored
			explored.add(cur);
			
			
//			System.out.println();
//			System.out.println("Explroed:");
//			for(ConflictNode n : explored){
//				System.out.println(n.toString());
//			}
//			System.out.println();
			
			
			
			for(ConflictNode n : neighbours){
				//if point is not visited
				if(!frontier.contains(n) && !explored.contains(n)) {
					frontier.add(n);
				}
			}
			
//			System.out.println("Frontier");
//			for(ConflictNode n : frontier){
//				System.out.println(n.toString());
//			}
//			System.out.println();
			
		}
	
		return path;
	}
	
	private static List<ConflictNode> getNeighbours(ConflictNode cur,List<Point> startPos){
		List<ConflictNode> n = new ArrayList<ConflictNode>();
		dir boxdir = null;
		Command[] allCommands = Command.every;
		
		//If box attached - get its direction from the agent
		if(startPos.size() == 2){
			boxdir = getBoxDir(cur);
		}
		
		// for all commands
		for(int i = 0; i<allCommands.length;i++) {
			Command c = allCommands[i];
			List<Point> posCand = null;
			
			switch(c.actType){
				case Move:
					//only consider this if there is no box attached
					if(startPos.size()==1){
						posCand = c.getNext(cur.getPoints());
					}
					break;
					
				case Pull:
					//only consider if if the box is in dir2 - pull(x, dir2)
					if(startPos.size() == 2 && c.dir2 == boxdir){
						posCand = c.getNext(cur.getPoints());
					}
					break;
					
				case Push:
					//only consider if the box is in the push direction - push(dir1 ,x) 
					if(startPos.size() == 2 && c.dir1 == boxdir ){
						posCand = c.getNext(cur.getPoints());
					}
					break;
			}
			
			//if the command is applicable, and allowed in the enviroment
			if(posCand != null && isAllowed(posCand,startPos)){
				ConflictNode nodeCand = new ConflictNode(posCand);
				nodeCand.setParent(cur);
				nodeCand.setCommand(allCommands[i]);
				n.add(nodeCand);
			}
		}
		return n;
	}
	private static boolean isAllowed(List<Point> cand,List<Point> startPos){
		//go through box and agent position. Check if they are free in the map
		for(int i = 0; i < cand.size(); i++){
			//disregard the starting position in the map
			if(!startPos.contains(cand.get(i))){
				if(map.isFree(cand.get(i).y,cand.get(i).x)){
//					System.out.println("isAllowed: false");
					return false;
				}
			}
			
		}
//		System.out.println("isAllowed: true");
		return true;
	}
	private static List<Command> generateGoalPath(ConflictNode goal){
		List<Command> solution = new ArrayList<Command>();
		
		ConflictNode cur = goal;
		while(cur.getParent() != null){
			solution.add(0,cur.getCommand());
			cur = cur.getParent();
			
		}
		
		return solution;
	}
	public static <E> boolean containsList(List<E> l1, List<E> l2){
		//goes through two list and returns false if one element is in the other list and vice versa
		for(E n : l1){
			if(l2.contains(n)){
				return true;
			}
		}
		return false;
		
	}
	private static dir getBoxDir(ConflictNode cur){
		dir boxdir = null;
		Point agent =  cur.getPoints().get(0);
		Point box = cur.getPoints().get(1);
		
		if(new Command(dir.N).getNext(agent).equals(box)){
			boxdir = dir.N;
		}
		if(new Command(dir.S).getNext(agent).equals(box)){
			boxdir = dir.S;			
		}
		if(new Command(dir.W).getNext(agent).equals(box)){
			boxdir = dir.W;
		}
		if(new Command(dir.E).getNext(agent).equals(box)){
			boxdir = dir.E;
		}
//		System.out.println("Boxdir: "+boxdir);
		return boxdir;
	}
}