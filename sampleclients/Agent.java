package sampleclients;


import java.util.*;

import static sampleclients.Agent.possibleStates.*;
import static sampleclients.Command.type;

public class Agent extends MovingObject {
    private boolean alreadyWaited = false;
    private Box attachedBox = null;
    private SearchClient pathFindingEngine;
    private int waitingCounter = 0;
    public int conflictSteps = 0;
    public boolean hasMoved = false;
    public LinkedList<Node> path;
    private possibleStates currentState = unassigned;
    private possibleStates previousState = currentState;
    enum possibleStates {
        jobless,
        unassigned,
        waiting,
        movingTowardsBox,
        movingBox,
        inConflict,
        pathBlocked
    }
    String serverOutput;
    public Agent( char id, String color, int y, int x ) {
        super(id, color, y, x, "Agent");
        pathFindingEngine = new SearchClient(this);
    }
    public String act(){
        serverOutput = null;
        switch (currentState) {
            case waiting:
                waitingProcedure();
                break;
            case unassigned:
                searchForJob();
                break;
            case jobless:
                serverOutput = "NoOp";
                break;
            case inConflict:
                resolveConflict();
                break;
            case movingTowardsBox:
                moveToTheBox();
                break;
            case movingBox:
                moveWithTheBox();
                break;
            case pathBlocked:
                serverOutput = "NoOp";
                break;
        }
        if(serverOutput != null) return serverOutput;
        System.err.println(currentState);
         return act(); // Temporary, just to cause stackOverflow instead of infinite loop, for better debugging
    }
    private void searchForJob() {
        if(!findClosestBox()) {
            System.err.println("Cant find box: ");
            currentState = possibleStates.jobless;
        }
        //maybe some other job?
    }
    private void resolveConflict() {
        if(path!= null && !path.isEmpty()){
            System.err.println("executing path to resolve conflict");
            serverOutput = executePath();
        }
        else {
            currentState = previousState;
        }
    }
    private void moveToTheBox() {
        String result = executePath();
        if(result != null) serverOutput = result;
        else if(nextToBox(attachedBox)) {
            System.err.println("isNext to box: ");
            currentState = possibleStates.movingBox;
        }
        else if(findPathToBox(attachedBox) == null) {
            enterWaitingState();
        }
        else {
            System.err.println("Moving towards box: ");
            serverOutput = executePath();
        }
    }
    private void moveWithTheBox() {
        if ((attachedBox.unassignedGoal() && !attachedBox.tryToFindAGoal())
                || attachedBox.setGoalPosition()) {
            dropTheBox();
            currentState = possibleStates.unassigned;
        }
        else {
            System.err.println("Moving box towards goal: ");
            String result = executePath();
            if (result != null) serverOutput = result;
            else if (findPathWithBox() == null) {
                enterWaitingState();
            }
            else {
                serverOutput = executePath();
            }
        }
    }
    private void dropTheBox() {
        attachedBox.clearOwnerReferences();
        attachedBox = null;
    }
    private boolean findClosestBox() {
        Box newBox;
        Box bestBox = null;
        int bestPath = Integer.MAX_VALUE;
        if(MainBoard.BoxColorGroups.get(getColor()) == null) return false;
        for(MovingObject currentBox : MainBoard.BoxColorGroups.get(getColor()).values()) {
            if(currentBox instanceof Box) {
                newBox = (Box) currentBox;
                System.err.println(newBox);
                if (!newBox.atGoalPosition && (newBox.assignedAgent == null) && !newBox.noGoalOnTheMap) {
                    if(nextToBox(newBox)) { // can find a path to box, or is next to!
                        attachedBox = newBox;
                        attachedBox.assignedAgent = this;
                        currentState = possibleStates.movingBox;
                        return true;
                    }
                    int currentPath = RandomWalkClient.roomMaster.getPathEstimate(getCoordinates(), newBox.getCoordinates());
                    System.err.println(currentPath);
                    if(currentPath < bestPath) {
                        bestPath = currentPath;
                        bestBox = newBox;
                    }
                }
            }
        }
        if(bestBox != null) {
            if(findPathToBox(bestBox) != null) {
                attachedBox = bestBox;
                attachedBox.assignedAgent = this;
                currentState = possibleStates.movingTowardsBox;
                return true;
            }
        }
        return false;
    }
    private void enterWaitingState() {
        previousState = currentState;
        currentState = possibleStates.waiting;
    }
    private void waitingProcedure() {
        waitForSomeMiracle();
    }
    private void waitForSomeMiracle() {
        if(waitingCounter == 0) {
            currentState = previousState;
        }
        else {
            serverOutput = "NoOp";
        }
    }
    private boolean nextToBox(Box current) {
        return nextTo(getX(), getY(), current.getX(), current.getY());
    }
    private String executePath( ) {
        if(path == null || path.isEmpty()) path = pathFindingEngine.continuePath();
        if (path != null) {
            Node nextStep = path.peek();
            if (nextStep != null) {
                System.err.println("try to move");
                if(!tryToMove(nextStep)) {
                    path = null;
                    return "NoOp";
                }
                //serverOutput = nextStep.action.toString();
                return nextStep.action.toString();

            }
        }
        path = null;
        return null;
    }
    private boolean tryToMove(Node nextStep)  throws UnsupportedOperationException {
        //return getMoveDirection(x, y);
        System.err.println("action: "+nextStep.action.toString());
        if(nextStep.action.actType == type.Noop) {
            System.err.println("Noop command");
        }
        else if(nextStep.action.actType == type.Move) {
            if(!RandomWalkClient.gameBoard.isFree(nextStep.agentX, nextStep.agentY)) throw new UnsupportedOperationException();
            updateMap(nextStep, RandomWalkClient.nextStepGameBoard);
        }
        else if(nextStep.action.actType == type.Push){
            if(!RandomWalkClient.gameBoard.isFree(nextStep.boxX, nextStep.boxY)) throw new UnsupportedOperationException();
            return updateMapWithBox(nextStep, RandomWalkClient.nextStepGameBoard);
        }
        else if(nextStep.action.actType == type.Pull){
            if(!RandomWalkClient.gameBoard.isFree(nextStep.agentX, nextStep.agentY)) throw new UnsupportedOperationException();
            return updateMapWithBox(nextStep, RandomWalkClient.nextStepGameBoard);
        }
        return true;
    }
    private void updateMap(Node nextStep, MainBoard board)  throws UnsupportedOperationException {
        board.changePositionOnMap(this, nextStep.agentX, nextStep.agentY);
    }
    private boolean updateMapWithBox (Node nextStep, MainBoard board) throws UnsupportedOperationException {
        boolean pushing = false;
        BasicObject movedObject = null;
        try {
            if(nextStep.action.actType ==  Command.type.Push) {
                pushing = true;
                System.err.println("Agent trying to move to "+nextStep.agentX+","+nextStep.agentY);
                System.err.println("Agent trying to move box to "+nextStep.boxX+","+nextStep.boxY);
                movedObject = board.getElement(nextStep.agentX, nextStep.agentY);
                if(movedObject instanceof Box) {
                    ((Box)movedObject).setGoalPosition();
                    board.changePositionOnMap((Box) movedObject, nextStep.boxX, nextStep.boxY);
                    board.changePositionOnMap(this, nextStep.agentX, nextStep.agentY);
                }
                else return false;
            }
            else if(nextStep.action.actType ==  Command.type.Pull){
                pushing = false;
                System.err.println("Agent trying to move to "+nextStep.agentX+","+nextStep.agentY);
                int bx = nextStep.boxX + Command.dirToXChange(nextStep.action.dir2);
                int by = nextStep.boxY + Command.dirToYChange(nextStep.action.dir2);
                System.err.println("Agent trying to move box from "+bx+","+by);
                movedObject = board.getElement(bx, by);
                if(movedObject instanceof Box) {
                    ((Box)movedObject).setGoalPosition();
                    board.changePositionOnMap(this, nextStep.agentX, nextStep.agentY);
                    board.changePositionOnMap((Box) movedObject, nextStep.boxX, nextStep.boxY);
                }
                else return false;
            }
        }
        catch(UnsupportedOperationException exc) {
            if(pushing) {
                System.err.println("Pushing");
                board.revertPositionChange((Box) movedObject, nextStep.boxX, nextStep.boxY);
            }
            else {
                System.err.println("pulling");
                board.revertPositionChange(this, nextStep.agentX, nextStep.agentY);
            }
            throw exc;
        }
        return true;
    }
    private LinkedList<Node> findPathToBox(Box BoxToMoveTo) {
        path = pathFindingEngine.getPath(false, BoxToMoveTo.getX(), BoxToMoveTo.getY());
        return path;
    }
    private LinkedList<Node> findPathWithBox() {
        path = pathFindingEngine.getPath(true, attachedBox.assignedGoal.getX(), attachedBox.assignedGoal.getY());
        return path;
    }
    public Command getCommand(int i) {
        try{
            return path.get(i).action;
        }
        catch (IndexOutOfBoundsException exc) {
            return null;
        }
    }

    public static boolean nextTo(int firstX, int firstY, int secondX, int secondY) {
        return (Math.abs(firstX - secondX) == 1) && (Math.abs(firstY - secondY) == 0)
                || (Math.abs(firstX - secondX) == 0) && (Math.abs(firstY - secondY) == 1);
    }
    public void replacePath(List<Command> commands) {
        if(path != null){
            path.clear();
        }else{
            path = new LinkedList<>();
        }
        int agentY = getY();
        int agentX = getX();
        for(int i = 0; i< commands.size(); i++) {
            Command c = commands.get(i);
            int newAgentY = agentY + Command.dirToYChange(c.dir1);
            int newAgentX = agentX + Command.dirToXChange(c.dir1);

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
    }
    public void handleConflict(List<Command> commands) {
        replacePath(commands);
    }
    public void handleConflict(int waitingTime) {
        waitingCounter = waitingTime;
        previousState = currentState;
        currentState = waiting;
    }
    public boolean isMovingBox() { return currentState == movingBox;}
    public String getCurrentState() { return "" + currentState;}
    public boolean isBoxAttached() {
    	return !(attachedBox == null);
    }
    public int getPriority() {return currentState.ordinal();}
    public void updatePosition() throws UnsupportedOperationException {

        switch (currentState) {
            case waiting:
                waitingCounter--;
                waitingCounter = 0;
            case unassigned:
            case jobless:
                return;
            case inConflict:
            case movingTowardsBox:
            case movingBox:
                finalizeNextMove();
                return;
        }

    }
    public void finalizeNextMove() {
        if(path == null || path.isEmpty()) return;
        Node nextStep = path.pollFirst();
        switch(nextStep.action.actType) {
            case Noop:
                return;
            case Move:
                updateMap(nextStep, RandomWalkClient.gameBoard);
                setCoordinates(nextStep.agentX, nextStep.agentY);
                return;
            case Pull:
            case Push:
                if(!updateMapWithBox(nextStep, RandomWalkClient.gameBoard)) {
                    path = null;
                    return;
                }
                Box movedObject = (Box) RandomWalkClient.gameBoard.getElement(nextStep.boxX, nextStep.boxY);
                setCoordinates(nextStep.agentX, nextStep.agentY);
                movedObject.setCoordinates(nextStep.boxX, nextStep.boxY);
        }
    }
    public void waitForObstacleToBeRemoved() {
        previousState = currentState;
        currentState = pathBlocked;
    }
    public void revertMoveIntention(MainBoard board) {
        if (hasMoved && path != null) {
            Node nextStep = path.peek();
            if (nextStep != null) {
                hasMoved = false;
                switch(nextStep.action.actType) {
                    case Noop:
                        break;
                    case Move:
                        board.revertPositionChange(this, nextStep.agentX, nextStep.agentY);
                        break;
                    case Push:
                        Box movedObject = (Box) board.getElement(nextStep.boxX, nextStep.boxY);
                        board.revertPositionChange(this, nextStep.agentX, nextStep.agentY);
                        board.revertPositionChange(movedObject, nextStep.boxX, nextStep.boxY);
                        break;
                    case Pull:
                        Box movedObject2 = (Box) board.getElement(nextStep.boxX, nextStep.boxY);
                        board.revertPositionChange(movedObject2, nextStep.boxX, nextStep.boxY);
                        board.revertPositionChange(this, nextStep.agentX, nextStep.agentY);
                        break;
                }

            }
            System.err.println("Agent "+getID()+" has been reverted");
        }
    }
    public Box getAttachedBox() {
        return attachedBox;
    }

}
