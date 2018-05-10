package sampleclients;

import java.awt.Point;
import java.util.*;
import sampleclients.Agent.possibleStates;
public class Conflicts {

	private static MainBoard mainBoard;

	public static void Conflict(Agent agent1){
        int maxPathSize = -1;
        LinkedList<Agent> involved = new LinkedList<>();
        involved.add(agent1);

        if(!Conflicts.delegateConflict(agent1,involved,maxPathSize)){
            maxPathSize = 3;
            if(!Conflicts.delegateConflict(agent1,involved,maxPathSize)){
                maxPathSize = 1;
                if(Conflicts.delegateConflict(agent1,involved,maxPathSize)){
                    System.err.println("Conflict resolved");
                }else{
                    System.err.println("Conflict NOT resolved");
                }
            }
        }
    }

	public static boolean delegateConflict(Agent agent1, List<Agent> involved,int mps){
        boolean solved = false;
		System.err.println();
		System.err.println( "Conflict Started for agent:"+agent1.getID() );
		mainBoard = RandomWalkClient.gameBoard;
		Agent conflictPartner = getConflictPartners(agent1);

        if(conflictPartner == null){
            System.err.println("conflict detected but no conflict partner found");
            System.err.println("Replanning and waiting");
            agent1.replan();
            agent1.handleConflict(1, true);
            return true;
        }

		if(conflictPartner.getID() == agent1.getID()){
			System.err.println("Conflict detected with itself.");
            System.err.println("Replanning and waiting");
            agent1.replan();
            agent1.handleConflict(1, true);
            return true;
		}


		System.err.println("Conflict partner:" + conflictPartner.toString() );
		if(conflictPartner.isMovingBox()){
            System.err.println("With box "+conflictPartner.getAttachedBox().toString());
        }

        Agent kingAgent = getKing(agent1,conflictPartner);
        Agent pawnAgent = null;
		if(kingAgent.getID() == agent1.getID()){
		    pawnAgent = conflictPartner;
        }else{
		    pawnAgent = agent1;
        }

        System.err.println("pawn is:"+pawnAgent.getID());
        System.err.println("king is:"+kingAgent.getID());

        if(!involved.contains(conflictPartner)){
            involved.add(conflictPartner);
        }else{
            return false;
        }


        System.err.println("Trying to resolve conflict by adding NoOp to pawn agent");
        solved = noopFix(pawnAgent,kingAgent,agent1.getID());
        System.err.println("Conflict resolved: "+solved);

        if(!solved){
            System.err.println("Trying to resolve conflict by PlanMerging");
            solved = planMerge(kingAgent,pawnAgent,mps,false,involved,agent1.getID());
            System.err.println("Conflict resolved: "+solved);
        }

        return solved;

	}

	private static Agent getConflictPartners(Agent agent1) {
		List<Point> agentPos = new ArrayList<Point>();
		agentPos.add(agent1.getCoordinates());
		if (agent1.isMovingBox()) {
			agentPos.add(agent1.getAttachedBox().getCoordinates());
		}
		List<Point> nextAgentPos = agent1.path.peek().action.getNext(agentPos);
		Point conflictPos = null;
		for (int i = 0; i < nextAgentPos.size(); i++) {

			if (!agentPos.contains(nextAgentPos.get(i))) {
				conflictPos = nextAgentPos.get(i);
			}
		}


		BasicObject b = RandomWalkClient.nextStepGameBoard.getElement((int) conflictPos.getX(), (int) conflictPos.getY());
		if(b == null){
			b = RandomWalkClient.gameBoard.getElement((int) conflictPos.getX(), (int) conflictPos.getY());
		}
		Agent conflictPartner = null;
		if(b instanceof Agent){
			conflictPartner = (Agent)b;
		}else if(b instanceof Box){
			conflictPartner = ((Box) b).assignedAgent;
		}
		return conflictPartner;

	}

	//This method is for detecting and delegating the type of conflict to the correct methods
	private static boolean noopFix(Agent pawnAgent, Agent kingAgent,char original){
		//Find next two points for king, if intersects with pawnAgent pos, return false, else true.
        if(kingAgent.isWaiting() || pawnAgent.isWaiting() || pawnAgent.path.isEmpty() || kingAgent.path.isEmpty()){
            return false;
        }


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

		pawnAgent.handleConflict(3, pawnAgent.getID() == original);
		if(kingAgent.getID() == original){
            System.err.println("acted");
		    kingAgent.act();
        }
		return true;
	}

	
	//For use in deciding who goes first in a simple conflict
	private static Agent getKing(Agent cand1, Agent cand2){
        int cand1Prio = cand1.getPriority();
        int cand2Prio = cand2.getPriority();

        if(cand1Prio == cand2Prio){
            cand1Prio += cand1.getID();
            cand2Prio += cand2.getID();
        }

        if(cand1Prio > cand2Prio){
            return cand1;
        }else{
            return cand2;
        }
	}
	//More difficult conflict where one needs to backtrack or go around with/without box
	
	private static boolean planMerge(Agent kingAgent, Agent pawnAgent, int mps,boolean reversed,List<Agent> involved,char original) {
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
        if (pawnAgent.isMovingBox()) {
            pawnAgentPos.add(pawnAgent.getAttachedBox().getCoordinates());
        }

        List<Point> locked = new ArrayList<Point>();
        Command tmpC;
        locked.addAll(pos);
        if(mps == -1){
            mps = kingAgent.path.size();
        }else {
            if(kingAgent.path.size() < mps){
                mps = kingAgent.path.size();
            }
        }
        for (int i = 0; i < mps; i++) {
            tmpC = kingAgent.getCommand(i);
            pos = tmpC.getNext(pos);
            for (Point p : pos) {
                if (!locked.contains(p)) {
                    locked.add(p);
                }
            }

        }


        List<Command> solution = ConflictBFS.doBFS(locked, pawnAgentPos, true,true,reversed);
        if (solution.size() == 0) {
            System.err.println();
            System.err.println("PLANMERGE FOUND NO SOLUTION while considering other agents");
            System.err.println();

            System.err.println();
            System.err.println("trying to find solution while not considering other agents");
            solution = ConflictBFS.doBFS(locked, pawnAgentPos, false,true,reversed);
            System.err.println();

            if (solution.size() == 0) {
                System.err.println();
                System.err.println("PLANMERGE FOUND NO SOLUTION while not considering other agents");
                System.err.println();

                System.err.println();
                System.err.println("trying to find solution while not considering other agents or boxes");
                solution = ConflictBFS.doBFS(locked, pawnAgentPos, false,false,reversed);
                System.err.println();

                if (solution.size() == 0) {
                    System.err.println();
                    System.err.println("PLANMERGE FOUND NO SOLUTION while not considering other agents and boxes");
                    System.err.println();
                    return false;
                }else{
                    System.err.println("Planmerge found solution. Reversing roles to get out");
                    solution.add(0,new Command());
                    pawnAgent.handleConflict(solution,pawnAgent.getID() == original);
                    return planMerge(pawnAgent,kingAgent,mps,true,involved,pawnAgent.getID());
                }
            }

        }
        System.err.println("PlanMerge found solution with pawn agent " + pawnAgent.getID() + ":");
        for (Command c : solution) {
            System.err.println(c.toString());
        }
        System.err.println("and king agent " + kingAgent.getID() + ":");
        for (Node c : kingAgent.path) {
            System.err.println(c.action.toString());
        }

        if(!kingAgent.hasMoved()) {
            kingAgent.handleConflict(1, kingAgent.getID() == original);
        }

        try{
            pawnAgent.handleConflict(solution, pawnAgent.getID() == original);
        }catch (UnsupportedOperationException exc){
            System.err.println("move Cant be applied after conflict");
            delegateConflict(pawnAgent,involved,mps);
        }

        return true;
    }

}


