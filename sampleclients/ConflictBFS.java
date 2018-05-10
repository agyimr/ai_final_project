package sampleclients;

import java.io.*;
import java.util.*;
import java.awt.Point;
import sampleclients.Command.dir;
import sampleclients.Command.type;

public class ConflictBFS {
	private static MainBoard map;
	private static MainBoard nextMap;
	private static boolean considerAgents = true;
	private static boolean considerBoxes = true;
	private static boolean reversed = false;
	private static List<Point> sl;
	public static List<Command> doBFS(List<Point> locked, List<Point> pos,List<Point> startLocked, boolean ca, boolean cb, boolean r){
		sl = startLocked;
		considerAgents = ca;
		considerBoxes = cb;
		reversed = r;
		map = RandomWalkClient.gameBoard;
		nextMap = RandomWalkClient.nextStepGameBoard;
		PriorityQueue<Cnode> explored = new PriorityQueue<Cnode>();
		PriorityQueue<Cnode> frontier = new PriorityQueue<Cnode>();
		List<Command> path = new ArrayList<Command>();
		
		//Add the current agent position to explored
		frontier.add(new Cnode(pos,0));

		//continue search as long as there are points in the firstfrontier
		while(!frontier.isEmpty()) {
			//pop the first element
			Cnode cur = frontier.poll();
			frontier.remove(0);
			
			//goal check - not in any locked points
			if(!containsList(locked,cur.getPoints()) && isGoal(cur)){
				path = generateGoalPath(cur);
				break;
			}
			
			
			//Get neighbour states of cur
			List<Cnode> neighbours = getNeighbours(cur, pos);
			
			//add the current Cnode to explored
			explored.add(cur);
			
			for(Cnode n : neighbours){
				//if point is not visited
				if(!frontier.contains(n) && !explored.contains(n)) {
					frontier.add(n);
				}
			}

		}

		return path;
	}



	private static List<Cnode> getNeighbours(Cnode cur,List<Point> startPos){
		List<Cnode> n = new ArrayList<Cnode>();
		dir boxdir = null;
		Command[] allCommands = Command.every;

		//If box attached - get its direction from the agent
		if(startPos.size() == 2){
			boxdir = getBoxDir(cur);
		}else{
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
				Cnode nodeCand = new Cnode(posCand,cur.getG()+1);
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
				int x = cand.get(i).x;
				int y = cand.get(i).y;
				boolean mapBoxHasAgent = false;
				boolean nextMapBoxHasAgent = false;
				if(map.getElement(x,y) instanceof Box){
					mapBoxHasAgent = ((Box) map.getElement(x,y)).assignedAgent == null;
				}
				if(nextMap.getElement(x,y) instanceof Box){
					nextMapBoxHasAgent = ((Box) nextMap.getElement(x,y)).assignedAgent == null;
				}
				if(map.isWall(x,y) ||
						(map.isBox(x,y) && (considerBoxes || mapBoxHasAgent)) ||
						(map.isAgent(x,y) && considerAgents) ||
						nextMap.isWall(x,y) ||
						(nextMap.isBox(x,y) && considerBoxes) ||
						(nextMap.isAgent(x,y) && (considerAgents || nextMapBoxHasAgent)) ||
						(containsList(sl,cand) && (considerAgents || considerBoxes))
						){
						return false;
				}
			}
		}
		return true;
	}
	private static List<Command> generateGoalPath(Cnode goal){
		List<Command> solution = new ArrayList<Command>();
		Cnode cur = goal;
		if (needNoop(cur)){
			solution.add(0,new Command());
		}
		while(cur.getParent() != null){
			solution.add(0,cur.getCommand());
			cur = cur.getParent();
			
		}
		
		return solution;
	}
	private static boolean needNoop(Cnode cur){
		Point agent =  cur.getPoints().get(0);
		Point box = agent;
		if(cur.getPoints().size() == 2){
			box = cur.getPoints().get(1);
		}
		int freeSpaces = 0;

		Point curP = new Command(dir.N).getNext(agent);
		if(!isBlocked(curP) || curP.equals(box)){
			freeSpaces+=1;
		}
		curP = new Command(dir.S).getNext(agent);
		if(!isBlocked(curP) || curP.equals(box)){
			freeSpaces+=1;
		}
		curP = new Command(dir.E).getNext(agent);
		if(!isBlocked(curP) || curP.equals(box)){
			freeSpaces+=1;
		}
		curP = new Command(dir.W).getNext(agent);
		if(!isBlocked(curP) || curP.equals(box)){
			freeSpaces+=1;
		}

		return freeSpaces < 3;
	}
	private static boolean isBlocked(Point p){
		int x = p.x;
		int y = p.y;
		return map.isWall(x,y) ||
				map.isBox(x,y)||
				map.isAgent(x,y)||
				nextMap.isWall(x,y) ||
				nextMap.isBox(x,y) ||
				nextMap.isAgent(x,y);
	}

	private static boolean isGoal(Cnode cur) {
		int x = cur.getX();
		int y = cur.getY();
		boolean mapBoxHasAgent = false;
		boolean nextMapBoxHasAgent = false;
		if(map.getElement(x,y) instanceof Box){
			mapBoxHasAgent = ((Box) map.getElement(x,y)).assignedAgent == null;
		}
		if(nextMap.getElement(x,y) instanceof Box){
			nextMapBoxHasAgent = ((Box) nextMap.getElement(x,y)).assignedAgent == null;
		}
		boolean boxCase = 		!(map.isBox(x,y) && considerBoxes && mapBoxHasAgent) &&
								!(nextMap.isBox(x,y) && considerBoxes && nextMapBoxHasAgent);
		boolean agentCase = 	!(map.isAgent(x,y) && considerAgents) &&
								!(nextMap.isAgent(x,y) && considerAgents);
		boolean alleyCase = 	!(reversed && isAlley(cur));

		return  !map.isWall(x,y) &&
				boxCase &&
				agentCase &&
				alleyCase;
	}
	private static boolean isAlley(Cnode cur) {
		Point agent =  cur.getPoints().get(0);
		int freeSpaces = 0;

		Point curP = new Command(dir.N).getNext(agent);
		if(!map.isWall(curP.x,curP.y)){
			freeSpaces+=1;
		}
		curP = new Command(dir.S).getNext(agent);
		if(!map.isWall(curP.x,curP.y)){
			freeSpaces+=1;
		}
		curP = new Command(dir.E).getNext(agent);
		if(!map.isWall(curP.x,curP.y)){
			freeSpaces+=1;
		}
		curP = new Command(dir.W).getNext(agent);
		if(!map.isWall(curP.x,curP.y)){
			freeSpaces+=1;
		}

		return freeSpaces <= 2;
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
	private static dir getBoxDir(Cnode cur){
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
		return boxdir;
	}
}

class Cnode implements Comparator<Cnode>, Comparable<Cnode>{
	private final Point pos;
	private final Point boxPos;
	private final List<Point> posLst;
	private Cnode parent = null;
	private Command c = null;
	private int g;
	public Cnode(int x, int y, int g) {
		this.pos = new Point(x,y);
		boxPos = null;
		posLst = new ArrayList<Point>();
		posLst.add(pos);
		this.g = g;
	}
	public Cnode(Point pos, int g) {
		this.pos = pos;
		boxPos = null;
		posLst = new ArrayList<Point>();
		posLst.add(pos);
		this.g = g;
	}
	public Cnode(Point pos, Point box, int g) {
		this.pos = pos;
		this.boxPos = box;
		posLst = new ArrayList<Point>();
		posLst.add(pos);
		posLst.add(boxPos);
		this.g = g;
	}
	public Cnode(List<Point> pos, int g) {
		this.pos = pos.get(0);
		if(pos.size() == 2){
			this.boxPos = pos.get(1);
		}else{
			this.boxPos = null;
		}

		posLst = new ArrayList<Point>(pos);
		this.g = g;
	}

	public void setParent(Cnode p) {
		parent = p;
	}
	public Cnode getParent() {
		return parent;
	}

	public void setCommand(Command c) {
		this.c = c;
	}
	public Command getCommand() {
		return c;
	}

	public int getX() { return pos.x;}
	public int getY() {return pos.y;}
	public int getBoxX() { return boxPos.x;}
	public int getBoxY() {return boxPos.y;}
	public List<Point> getPoints() {return posLst;}
	public boolean hasBox(){return posLst.size() == 2;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		Cnode other = (Cnode) obj;
		if (pos.x != other.pos.x)
			return false;
		if (pos.y != other.pos.y)
			return false;
		return posLst.equals(other.posLst);
	}

	@Override
	public String toString() {
		if(boxPos == null){
			return "(" + pos.x + ", " + pos.y + ")";
		}else{
			return "(" + pos.x + ", " + pos.y + ")" + "(" + boxPos.x + ", " + boxPos.y + ")";
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 17;
		result = prime * result;
		result = prime * result + pos.x;
		result = prime * result + pos.y;
		return result;
	}
	public int getG(){
		return g;
	}
	public int getH(){
		int prio = g;
		if(RandomWalkClient.gameBoard.isGoal(pos.x,pos.y)){
			prio += 1;
		}
		if(RandomWalkClient.gameBoard.isAgent(pos.x,pos.y)){
			Agent a = (Agent) RandomWalkClient.gameBoard.getElement(pos.x,pos.y);
			prio += a.getPriority();
		}
		if(RandomWalkClient.gameBoard.getElement(pos.x,pos.y) instanceof Box){
			if(((Box) RandomWalkClient.gameBoard.getElement(pos.x,pos.y)).assignedAgent == null){
				prio += 10;
			}else{
				prio = 5;
			}
		}

		return g + prio;
	}
	public int compare(Cnode self, Cnode other) {
		return self.getH() - other.getH();
	}
	public int compareTo(Cnode other) {
		return getH() - other.getH();
	}
}
