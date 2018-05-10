package sampleclients;

import java.awt.Point;
import java.util.*;
import sampleclients.Agent.possibleStates;
public class Conflicts {

	private static MainBoard mainBoard;

	public static void Conflict(Agent agent1){
        System.err.println("\n -----------------------start--------------------------------");

        List<Agent> allInvolved = new ArrayList<>();
        allInvolved.add(agent1);
        int maxPathSize = -1;
        LinkedList<Agent> involved = new LinkedList<>();
        involved.add(agent1);
        System.err.println("\n ----------------first-------------------------");
        if(Conflicts.delegateConflict(agent1,involved,maxPathSize,1) == -1){
            maxPathSize = 3;
            allInvolved.addAll(involved);
            involved.clear();
            involved.add(agent1);
            System.err.println("\n ----------------second-------------------------");
            if(Conflicts.delegateConflict(agent1,involved,maxPathSize,1) == -1){
                maxPathSize = 1;
                allInvolved.addAll(involved);
                involved.clear();
                involved.add(agent1);
                System.err.println("\n ----------------third-------------------------");
                if(Conflicts.delegateConflict(agent1,involved,maxPathSize,1) != -1){
                    System.err.println("Conflict resolved");
                }else{
                    System.err.println("Conflict NOT resolved");
                    System.err.println("Replanning and waiting");
                    agent1.handleConflict(1,true,true);
                }
            }
        }

        for(Agent a : allInvolved){
            if(!a.hasMoved()){
                System.err.println("----FAIL SAFE Start----");
                System.err.println("Agent "+a.getID()+" emergency acts");
                a.handleConflict(1,true,false);
                System.err.println("----FAIL SAFE end----");
            }
        }
        System.err.println("\n -------------------------end--------------------------------");
    }

	public static int delegateConflict(Agent agent1, List<Agent> involved,int mps,int rec){
        System.err.println("---- Delegate conflict start ----");
        System.err.println( rec+" Conflict Started for agent:"+agent1.getID() );
        boolean solved = false;
		mainBoard = RandomWalkClient.gameBoard;
		Agent conflictPartner = getConflictPartners(agent1);

        if(conflictPartner == null){
            System.err.println("\n"+rec+" conflict detected but no conflict partner found");
            System.err.println("Replanning and waiting");
            agent1.handleConflict(1,true,true);
            System.err.println("---- Delegate conflict end ----");
            return 1;
        }

		if(conflictPartner.getID() == agent1.getID()){
			System.err.println(rec+" Conflict detected with itself.");
            System.err.println("Replanning and waiting");
            agent1.handleConflict(1,true,true);
            System.err.println("---- Delegate conflict end ----");
            return -1;
		}


		System.err.println(rec+" Conflict partner:" + conflictPartner.toString() );
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
            System.err.println("Conflict resolution have recurred into the same agent");
            System.err.println("---- Delegate conflict end ----");
            return -1;
        }

        solved = noopFix(pawnAgent,kingAgent,agent1.getID(),involved,mps,rec);
        System.err.println(rec+" Conflict resolved by NoOp's: "+solved);

        if(!solved){
            solved = planMerge(kingAgent,pawnAgent,mps,false,involved,agent1.getID(),rec);
            System.err.println(rec+" Conflict resolved with planmerge: "+solved);
        }

        System.err.println("---- Delegate conflict end ----");
        if(solved){
            return rec+1;
        }else {
            return -1;
        }

	}

	private static Agent getConflictPartners(Agent agent1) {
        if(agent1.isWaiting() || agent1.path.isEmpty()){
            return null;
        }
		List<Point> agentPos = new ArrayList<Point>();
		agentPos.add(agent1.getCoordinates());
		if (agent1.isMovingBox()) {
            System.err.println("isMovingBox");
			agentPos.add(agent1.getAttachedBox().getCoordinates());
		}
		List<Point> nextAgentPos = agent1.path.peek().action.getNext(agentPos);
		Point conflictPos = null;
		for (int i = 0; i < nextAgentPos.size(); i++) {

			if (!agentPos.contains(nextAgentPos.get(i))) {
				conflictPos = nextAgentPos.get(i);
			}
		}


		BasicObject b = RandomWalkClient.gameBoard.getElement((int) conflictPos.getX(), (int) conflictPos.getY());
		if(b == null){
			b = RandomWalkClient.nextStepGameBoard.getElement((int) conflictPos.getX(), (int) conflictPos.getY());
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
	private static boolean noopFix(Agent pawnAgent, Agent kingAgent,char original,List<Agent> involved,int mps, int rec){
        System.err.println("----- NoopFix start -------");
		//Find next two points for king, if intersects with pawnAgent pos, return false, else true.
        if(kingAgent.isWaiting() || pawnAgent.isWaiting() || pawnAgent.path.isEmpty() || kingAgent.path.isEmpty()){
            System.err.println("----- NoopFix end -------");
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
                        System.err.println("----- NoopFix end -------");
						return false;
					}
				}

			}
		}

		pawnAgent.handleConflict(3, pawnAgent.getID() == original,false);
		if(kingAgent.getID() == original){
		    try{
                kingAgent.act();
            }catch (UnsupportedOperationException exc){
                System.err.println("\n move Cant be applied after conflict!");
                rec = delegateConflict(kingAgent,involved,mps,rec);
                System.err.println("in planmerge out of delegate");
            }

        }
        System.err.println("----- NoopFix end -------");
		return rec != -1;
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
	
	private static boolean planMerge(Agent kingAgent, Agent pawnAgent, int mps,boolean reversed,List<Agent> involved,char original,int rec) {
        System.err.println("---- PLANMERGE start ----");
	    int index = 0;
        List<Point> startLocked = new ArrayList<>();
        Point posKing = new Point(kingAgent.getX(), kingAgent.getY()); //Node 0 for the king
        List<Point> pos = new ArrayList<Point>();
        startLocked.add(posKing);
        pos.add(posKing);
        if (kingAgent.isBoxAttached()) {
            Point posBox = kingAgent.getAttachedBox().getCoordinates();
            pos.add(posBox);
            startLocked.add(posBox);
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


        List<Command> solution = ConflictBFS.doBFS(locked, pawnAgentPos, startLocked,true,true,reversed);
        if (solution.size() == 0) {
            System.err.println("\nPLANMERGE FOUND NO SOLUTION while considering other agents");
            System.err.println("trying to find solution while not considering other agents\n");
            solution = ConflictBFS.doBFS(locked, pawnAgentPos, startLocked,false,true,reversed);

            if (solution.size() == 0) {
                System.err.println("\nPLANMERGE FOUND NO SOLUTION while not considering other agents");
                System.err.println("trying to find solution while not considering other agents or boxes\n");
                solution = ConflictBFS.doBFS(locked, pawnAgentPos, startLocked,false,false,reversed);

                if (solution.size() == 0) {
                    System.err.println("\nPLANMERGE FOUND NO SOLUTION while not considering other agents and boxes\n");
                    System.err.println("---- PLANMERGE end ----");
                    return false;
                }else{
                    System.err.println("\nPlanmerge found solution. Reversing roles to get out\n");
                    solution.add(0,new Command());
                    if(pawnAgent.getID() == original){
                        System.err.println("PROBLEM ! UNSAFE HANDLE CONFLICT");
                    }
                    pawnAgent.replacePath(solution);
                    System.err.println("---- PLANMERGE end ----");
                    return planMerge(pawnAgent,kingAgent,mps,true,involved,original,rec);
                }
            }

        }
        System.err.println("\n"+rec+" PlanMerge found solution with pawn agent " + pawnAgent.getID() + ":");
        for (Command c : solution) {
            System.err.println(c.toString());
        }
        System.err.println("and king agent " + kingAgent.getID() + ":");
        for (Node c : kingAgent.path) {
            System.err.println(c.action.toString());
        }

        try{
            pawnAgent.handleConflict(solution, pawnAgent.getID() == original);
        }catch (UnsupportedOperationException exc){
            System.err.println("\n"+rec+" move Cant be applied after conflict!");
            rec = delegateConflict(pawnAgent,involved,mps,rec);
            System.err.println("in planmerge out of delegate");
        }


        if(rec == -1){
            System.err.println("---- PLANMERGE end ----");
            return false;
        }
        if(!kingAgent.hasMoved()) {
            kingAgent.handleConflict(rec, kingAgent.getID() == original,false);
        }

        System.err.println("---- PLANMERGE end ----");
        return rec != -1;
    }

}


