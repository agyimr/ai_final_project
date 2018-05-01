package sampleclients;

import java.awt.Point;
import java.util.*;
import sampleclients.Agent.possibleStates;
public class Conflicts {

	private static MainBoard mainBoard;

	public static void delegateConflict(Agent agent1){
		System.err.println();
		System.err.println( "Conflict Started for agent:"+agent1.getID() );
		mainBoard = RandomWalkClient.gameBoard;
		Agent conflictPartner = getConflictPartners(agent1);

        if(conflictPartner == null){
            System.err.println("conflict detected but no conflict partner found");
            System.err.println("reverting, Replanning and waiting");
            agent1.revertMoveIntention(RandomWalkClient.nextStepGameBoard);
            agent1.replan();
            agent1.handleConflict(2);
            agent1.act();
            return;
        }

		if(conflictPartner.getID() == agent1.getID()){
			System.err.println("Conflict detected with itself.");
            System.err.println("reverting, Replanning and waiting");
            agent1.revertMoveIntention(RandomWalkClient.nextStepGameBoard);
            agent1.replan();
            agent1.handleConflict(2);
            agent1.act();
            return;
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

        boolean pawnMove = false;
        if(pawnAgent.getID() < kingAgent.getID()){
            pawnMove = true;
            System.err.println("trying to revert Agent "+pawnAgent.getID());
            pawnAgent.revertMoveIntention(RandomWalkClient.nextStepGameBoard);
        }

        System.err.println("Trying to resolve conflict by adding NoOp to pawn agent");
        boolean noopFix = noopFix(pawnAgent,kingAgent);
        System.err.println("Conflict resolved: "+noopFix);

        boolean planMerge = false;
        if(!noopFix){
            System.err.println("Trying to resolve conflict by PlanMerging");
            planMerge = planMerge(kingAgent,pawnAgent);
            System.err.println("Conflict resolved: "+planMerge);
        }

        if(noopFix || planMerge){
            System.err.println("Agent act "+agent1.getID());
            try {
                agent1.act();
            }catch (UnsupportedOperationException exc){
                System.err.println("Move cant be applied after conflict");
                System.err.println("waiting and Replanning instead");
                agent1.replan();
                agent1.handleConflict(2);
                agent1.act();
            }
            if(pawnMove && conflictPartner.getID() == pawnAgent.getID()) {
                try {
                    conflictPartner.act();
                } catch (UnsupportedOperationException exc) {
                    System.err.println("Move cant be applied after conflict");
                    System.err.println("waiting and Replanning instead");
                    conflictPartner.replan();
                    conflictPartner.handleConflict(2);
                    conflictPartner.act();
                }
            }
            return;
        }else{
            System.err.println("Conflict resolution have not been able to resovle the conflict -> throwing exception");
            throw new UnsupportedOperationException();
        }

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
	private static boolean noopFix(Agent pawnAgent, Agent kingAgent){
		//Find next two points for king, if intersects with pawnAgent pos, return false, else true.
        if(kingAgent.isWaiting() || pawnAgent.isWaiting()){
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


		pawnAgent.handleConflict(3);
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
	
	private static boolean planMerge(Agent kingAgent, Agent pawnAgent) {
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
        boolean kingNoop = false;
        for (int i = 0; i < kingAgent.path.size(); i++) {
            tmpC = kingAgent.getCommand(i);

            pos = tmpC.getNext(pos);
            for (Point p : pos) {
                if (!locked.contains(p)) {
                    locked.add(p);
                }
                if (i == 0 && pawnAgentPos.contains(p)) {
                    kingNoop = true;
                }
            }

        }


        List<Command> solution = ConflictBFS.doBFS(locked, pawnAgentPos, true);
        if (solution.size() == 0) {
            System.err.println();
            System.err.println("PLANMERGE FOUND NO SOLUTION while considering other agents");
            System.err.println();

            System.err.println();
            System.err.println("trying to find solution while not considering other agents");
            solution = ConflictBFS.doBFS(locked, pawnAgentPos, false);
            System.err.println();

            if (solution.size() == 0) {
                System.err.println();
                System.err.println("PLANMERGE FOUND NO SOLUTION while not considering other agents");
                System.err.println();
                return false;
            }


        }

        kingAgent.handleConflict(1);
        pawnAgent.handleConflict(solution);
        pawnAgent.handleConflict(1);
        System.err.println("PlanMerge found solution with pawn agent " + pawnAgent.getID() + ":");
        for (Command c : solution) {
            System.err.println(c.toString());
        }
        System.err.println("and king agent " + kingAgent.getID() + ":");
        for (Node c : kingAgent.path) {
            System.err.println(c.action.toString());
        }
        System.err.println("CurrentKingAgentNextState = " + kingAgent.getCurrentState());
        System.err.println("CurrentPawnAgentNextState = " + pawnAgent.getCurrentState());

        return true;
    }
}
