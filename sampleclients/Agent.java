package sampleclients;


import java.util.*;
import java.awt.Point;

import static sampleclients.Command.type;

public class Agent extends MovingObject {
    private static final int WAITING_MAX = 3;
    private boolean waiting = false;
    private Box attachedBox = null;
    boolean isMovingBox = false;
    SearchClient pathFindingEngine;
    int waitingCounter = 0;
    public int conflictSteps = 0;

    public Agent( char id, String color, int y, int x ) {
        super(id, color, y, x, "Agent");
        pathFindingEngine = new SearchClient(this);
    }
    public String act() {


        if(attachedBox == null) {
            if(!findABox()) {
                if(conflictSteps > 0){
                    conflictSteps--;
                }
                System.err.println("Cant find box: ");
                return waitingProcedure();
            }
        }
        if(!isMovingBox) {//then move towards box
            System.err.println("Execute path");
            if(conflictSteps > 0){
                conflictSteps--;
            }
            String result = executePath();
            if(result != null) return result;
            else if(nextToBox(attachedBox)) {
                System.err.println("isNext to box: ");
                isMovingBox = true;
            }
            else if(findPathToBox(attachedBox) == null) {
                return waitingProcedure();
            }

            else {
                System.err.println("Moving towards box: ");
                return executePath();
            }
        }
        //no assigned goal
        if (attachedBox.assignedGoal == null) {
            System.err.println("No assigned goal: ");
            //try finding a goal
            attachedBox.assignedGoal = MainBoard.goals.get(Character.toLowerCase(attachedBox.getID()));
            if (attachedBox.assignedGoal == null) {
                //no goal that satisfies the box on the map
                attachedBox.noGoalOnTheMap = true;
                attachedBox.clearOwnerReferences();
                attachedBox = null;
                //try finding a box again
                return act();
            }
        }
        //box at the goal position!
        if (attachedBox.assignedGoal.atGoalPosition(attachedBox)) {
            System.err.println("box at goal postion: ");
            attachedBox.clearOwnerReferences();
            attachedBox.atGoalPosition = true;
            attachedBox = null;
            isMovingBox = false;
            //try finding a box again
            return act();
        }
        //box attached and not at the goal position
        else {
            if(conflictSteps > 0){
                conflictSteps--;
            }
            System.err.println("Moving box towards goal: ");
            //now you must make a move
            if (path == null) {
                path = findPathWithBox();
            }
            String result = executePath();
            if (result != null) return result;
            else {
                //path blocked?
                path = null;
                return waitingProcedure();

            }
        }
    }
    private boolean findABox() {
        Box newBox = null;
        Box bestBox = null;
        LinkedList<Node> bestPath = null;
        for(MovingObject currentBox : MainBoard.BoxColorGroups.get(getColor()).values()) {
            if(currentBox instanceof Box) {
                newBox = (Box) currentBox;
                if (!newBox.atGoalPosition && (newBox.assignedAgent == null) && !newBox.noGoalOnTheMap) {
                    findPathToBox(newBox);
                    if(bestPath == null) {
                        bestPath = path;
                        bestBox = newBox;
                        continue;
                    }
                    else if(nextToBox(newBox)) { // can find a path to box, or is next to!
                        attachedBox = newBox;
                        attachedBox.assignedAgent = this;
                        isMovingBox = true;
                        return true;
                    }
                    else if(path != null && path.size() < bestPath.size()) {
                        bestPath = path;
                        bestBox = newBox;
                    }

                }
            }
        }
        if(bestBox != null) {
            attachedBox = bestBox;
            path = bestPath;
            attachedBox.assignedAgent = this;
            return true;
        }
        return false;
    }
    private boolean nextToBox(Box current) {
        return (Math.abs(getX() - current.getX()) == 1) && (Math.abs(getY() - current.getY()) == 0)
                || (Math.abs(getX() - current.getX()) == 0) && (Math.abs(getY() - current.getY()) == 1);
    }
    private String executePath( ) {
        if (path != null) {
            Node nextStep = path.peek();
            if (nextStep != null) {
                System.err.println("try to move");
                tryToMove(nextStep);
                return nextStep.action.toString();

            }
        }
        path = null;
        return null;
    }
    public void tryToMove(Node nextStep)  throws UnsupportedOperationException {
        //return getMoveDirection(x, y);
        System.err.println("action: "+nextStep.action.actType);
        if(nextStep.action.actType == type.Noop) {
            System.err.println("Noop command");
            return;
        }
        else if(nextStep.action.actType == type.Move) {
            updateMap(nextStep, RandomWalkClient.nextStepGameBoard);
        }
        else {
                updateMapWithBox(nextStep, RandomWalkClient.nextStepGameBoard);
        }
    }
    private void updateMap(Node nextStep, MainBoard board)  throws UnsupportedOperationException {
        board.changePositionOnMap(this, nextStep.agentX, nextStep.agentY);
    }
    private void updateMapWithBox (Node nextStep, MainBoard board) throws UnsupportedOperationException {
        boolean pushing = false;
        Box movedObject = null;
        try {
            if(nextStep.action.actType ==  Command.type.Push) {
                pushing = true;
                System.err.println("Agent trying to move to "+nextStep.agentX+","+nextStep.agentY);
                int bx = nextStep.boxX + Command.dirToXChange(nextStep.action.dir2);
                int by = nextStep.boxY + Command.dirToYChange(nextStep.action.dir2);
                System.err.println("Agent trying to move box from "+bx+","+by);
                movedObject = (Box) board.getElement(nextStep.agentX, nextStep.agentY);
                board.changePositionOnMap(movedObject, nextStep.boxX, nextStep.boxY);
                board.changePositionOnMap(this, nextStep.agentX, nextStep.agentY);

            }
            else if(nextStep.action.actType ==  Command.type.Pull){
                pushing = false;
                System.err.println("Agent trying to move to "+nextStep.agentX+","+nextStep.agentY);
                int bx = nextStep.boxX + Command.dirToXChange(nextStep.action.dir2);
                int by = nextStep.boxY + Command.dirToYChange(nextStep.action.dir2);
                System.err.println("Agent trying to move box from "+bx+","+by);
                movedObject = (Box)
                        board.getElement(
                        nextStep.boxX + Command.dirToXChange(nextStep.action.dir2),
                        nextStep.boxY + Command.dirToYChange(nextStep.action.dir2));

                board.changePositionOnMap(this, nextStep.agentX, nextStep.agentY);
                System.err.println("agent moved");
                board.changePositionOnMap(movedObject, nextStep.boxX, nextStep.boxY);
                System.err.println("box moved");
            }
        }
        catch(UnsupportedOperationException exc) {
            if(pushing) {
                System.err.println("Pushing");
                board.changePositionOnMap(movedObject, movedObject.getX(), movedObject.getY());
            }
            else {
                System.err.println("pulling");
                board.changePositionOnMap(this, getX(), getY());
            }
            throw exc;
        }
    }
    private LinkedList<Node> findPathToBox(Box BoxToMoveTo) {
        RandomWalkClient.gameBoard.setElement(  BoxToMoveTo.getX(), BoxToMoveTo.getY(), null);
        path = pathFindingEngine.FindPath(false, BoxToMoveTo.getX(), BoxToMoveTo.getY());
        //findPath(BoxToMoveTo.getX(), BoxToMoveTo.getY());
        if(path != null) path.pollLast();
        RandomWalkClient.gameBoard.setElement(  BoxToMoveTo.getX(), BoxToMoveTo.getY(), BoxToMoveTo);
        return path;
    }
    private LinkedList<Node> findPathWithBox() {
        path = pathFindingEngine.FindPath(true, attachedBox.assignedGoal.getX(), attachedBox.assignedGoal.getY());

        return path;
    }

        Command getCommand(int i) {
        Node somePosition= null;
        try{
            if(!isMovingBox) {
                if(path == null){
                    return new Command();
                }else{
                    return path.get(i).action;
                }
            }
            else {
                if(path == null){
                    return new Command();
                }else{
                    return path.get(i).action;
                }

            }
        }
        catch (IndexOutOfBoundsException exc) {
            return null;
        }
    }
    public boolean replacePath(List<Command> commands) {
        if(path != null){
            path.clear();
        }else{
            path = new LinkedList<Node>();
        }

        int agentY = getY();
        int agentX = getX();
        for(int i = 0; i< commands.size(); i++) {
            Command c = commands.get(i);
            int newAgentY = agentY + Command.dirToYChange(c.dir1);
            int newAgentX = agentX + Command.dirToXChange(c.dir1);

            System.err.println("Agent to "+newAgentX+","+newAgentY);

            if(c.actType == type.Move) {
                path.add(new Node(null, c, newAgentX, newAgentY));
            }
            else if(c.actType == type.Push) {
                int newBoxY = newAgentY + Command.dirToYChange(c.dir2);
                int newBoxX = newAgentX + Command.dirToXChange(c.dir2);
                path.add(new Node(null, c, newAgentX, newAgentY, newBoxX, newBoxY));
            }
            else if ( c.actType == type.Pull ) {
                path.add(new Node(null, c, newAgentX, newAgentY, agentX, agentY));
            }
            else {
                path.add(new Node(null, c, agentX, agentY));
            }
            agentX = newAgentX;
            agentY = newAgentY;
        }
        conflictSteps = commands.size();
        return true;
    }

    public boolean isBoxAttached() {
    	return isMovingBox;
    }
    void updatePosition() throws UnsupportedOperationException {
        //save'em so you can restore the state if sth goes wrong`
        int  AttachedBoxCoordX = 0,
                AttachedBoxCoordY = 0,
                currentX= getX(),
                currentY= getY();
        if(waiting) {
            waitingCounter++;
            waiting = false;
            return;
        }
        waitingCounter = 0;
        try {
            if(isMovingBox) {
                Node nextStep = path.pollFirst();
                if (nextStep.action.actType == type.Noop){
                    return;
                }
                updateMapWithBox(nextStep, RandomWalkClient.gameBoard);
                Box movedObject = (Box) RandomWalkClient.gameBoard.getElement(nextStep.boxX, nextStep.boxY);
                setCoordinates(nextStep.agentX, nextStep.agentY);
                movedObject.setCoordinates(nextStep.boxX, nextStep.boxY);
            }
            else {
                Node nextStep = path.pollFirst();
                if (nextStep.action.actType == type.Noop){
                    return;
                }
                updateMap(nextStep, RandomWalkClient.gameBoard);
                setCoordinates(nextStep.agentX, nextStep.agentY);
            }
        }
        catch(UnsupportedOperationException exc) {
            throw exc;
        }

    }
    private String waitingProcedure() {
        waiting = true;
        if(waitingCounter >= WAITING_MAX) {
            if(attachedBox != null) {
                attachedBox.noGoalOnTheMap = true; // this is only temporary to see it work without conflict resolutions!!
                attachedBox.clearOwnerReferences();
                attachedBox = null;
            }
        }
        return "NoOp";
    }

    public Box getAttachedBox() {
        return attachedBox;
    }
    public Point getAgentPoint(){
        return new Point(this.getX(),this.getY());
    }

    public Point getAttachedBoxPoint() {
        Point tmp = new Point(attachedBox.getX(),attachedBox.getY());
        return tmp;
    }

}
