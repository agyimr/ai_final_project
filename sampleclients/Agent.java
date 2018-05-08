package sampleclients;


import java.util.*;

import static sampleclients.Agent.possibleStates.*;
import static sampleclients.Command.type;

public class Agent extends MovingObject {
    private Box attachedBox = null;
    private SearchClient pathFindingEngine;
    private int waitingCounter = 0;
    public int conflictSteps = 0;
    private Box nextBoxToPush = null;
    private boolean pendingHelp = false;
    public LinkedList<Node> path;
    private possibleStates currentState = unassigned;
    private possibleStates previousState = currentState;
    private possibleStates beforeObstacleState = unassigned;
    enum possibleStates {
        jobless,
        unassigned,
        waiting,
        movingTowardsBox,
        movingBox,
        inConflict,
        pathBlocked
    }
    String serverOutput = null;
    public Agent( char id, String color, int y, int x ) {
        super(id, color, y, x, "Agent");
        pathFindingEngine = new SearchClient(this);
    }
    public String act(){
        serverOutput = null;
        if(pendingHelp) {

            pendingHelp = false;
        }
        System.err.println("Starting CurrentState: "+currentState);
        switch (currentState) {
            case waiting:
                waitForSomeMiracle();
                break;
            case unassigned:
                searchForJob();
                break;
            case jobless:
                //previousState=unassigned;
                //waitingProcedure(5);
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
                checkPath();
                break;
        }
        if(serverOutput != null) {
            System.err.println("Ending current state: "+currentState);
            System.err.println("ServerOutput: "+serverOutput);
            return serverOutput;
        }
        return act(); // Temporary, just to cause stackOverflow instead of infinite loop, for better debugging
    }
    public String collectServerOutput() {
        if(serverOutput == null) throw new NegativeArraySizeException();
        return serverOutput;
    }
    private void checkPath() {
        pathFindingEngine.immovableObstacles.clear();
        if(executePath() == null) {
            waitingProcedure(3);
        }
        else {
            changeState(beforeObstacleState);
            beforeObstacleState = null;
        }
    }
    private void searchForJob() {
        if(nextBoxToPush != null) {
            attachedBox = nextBoxToPush;
            nextBoxToPush = null;
        }
        else if(isBoxAttached()) {
            changeState(movingTowardsBox);
        }
        else if( !findClosestBox()) {
            System.err.println("Cant find box: ");
            changeState(jobless);
        }
        //maybe some other job?
    }
    private void resolveConflict() {
        if(path!= null && !path.isEmpty()){
            System.err.println("executing path to resolve conflict");
            serverOutput = executePath();
        }
        else {
            revertState();
        }
    }
    private void moveToTheBox() {
        String result = executePath();
        if(result != null) serverOutput = result;
        else if(nextToBox(attachedBox)) {
            System.err.println("isNext to box: ");
            changeState(movingBox);
        }
        else if(!findPathToBox(attachedBox)) {
            waitingProcedure(3);
        }
        else {
            System.err.println("Moving towards box: ");
            serverOutput = executePath();
        }
    }
    private void moveWithTheBox() {
        if ((attachedBox.unassignedGoal() && !attachedBox.tryToFindAGoal())) {
            replan();
        }
        else if(attachedBox.atGoalPosition()) {
            attachedBox.resetDependencies();
            replan();
        }
        else {
            System.err.println("Moving box towards goal: ");
            String result = executePath();
            if (result != null) serverOutput = result;
            else if (!findPathWithBox()) {
                waitingProcedure(3);
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
        for(MovingObject currentBox : MainBoard.BoxColorGroups.get(getColor())) {
            if(currentBox instanceof Box) {
                newBox = (Box) currentBox;
                if(newBox.assignedGoal == null) {
                    newBox.tryToFindAGoal();
                }
                if (!newBox.atGoalPosition() && (newBox.assignedAgent == null) && !newBox.noGoalOnTheMap) {
                    if(nextToBox(newBox)) { // can find a path to box, or is next to!
                        attachedBox = newBox;
                        attachedBox.assignedAgent = this;
                        changeState(movingBox);

                        return true;
                    }
                    int currentPath = RandomWalkClient.roomMaster.getPathEstimate(getCoordinates(), newBox.getCoordinates());
                    if(currentPath < bestPath) {
                        bestPath = currentPath;
                        bestBox = newBox;
                    }
                }
            }
        }
        if(bestBox != null) {
            if(findPathToBox(bestBox)) {
                attachedBox = bestBox;
                attachedBox.assignedAgent = this;
                changeState(movingTowardsBox);
                return true;
            }
        }
        return false;
    }
    private void waitingProcedure(int counter) {
        waitingCounter = counter;
        changeState(waiting);
    }
    private void waitForSomeMiracle() {
        if(waitingCounter == 0) {
            revertState();
        }
        else {
            serverOutput = "NoOp";
        }
    }
    private void changeState(possibleStates nextState) {
        if(nextState == currentState) return;
        previousState = currentState;
        currentState = nextState;

    }
    private void revertState() {
        if(previousState == waiting || previousState == inConflict) previousState = unassigned;
        if(previousState == null) throw new NegativeArraySizeException();
        currentState = previousState;
        previousState = null;
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
                    System.err.println(path);
                    path = null;
                    throw new NegativeArraySizeException();
                    // return "NoOp";
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
    private boolean findPathToBox(Box BoxToMoveTo) {
        if( ! pathFindingEngine.getPath(false, BoxToMoveTo.getX(), BoxToMoveTo.getY())) return false;
        path = pathFindingEngine.continuePath();
        if(path != null)
            return true;
        else
            return false;
    }
    private boolean findPathWithBox() {
        if( ! pathFindingEngine.getPath(true, attachedBox.assignedGoal.getX(), attachedBox.assignedGoal.getY())) return false;
        path = pathFindingEngine.continuePath();
        if(path != null)
            return true;
        else
            return false;
    }
    public void replan() {
        path = null;
        if(isBoxAttached()) {
            dropTheBox();
        }
        changeState(unassigned);
    }
    public Command getCommand(int i) {
        try{
            if(path == null) return null;
            return path.get(i).action;
        }
        catch (IndexOutOfBoundsException exc) {
            return null;
        }
    }
    public void helpYourFriend(Box issue, int offset) {
        nextBoxToPush = issue;
        if(this.isMovingBox() && path.size() < offset) {
            //just finish the job
        }
        else {
            pendingHelp = true;
        }
    }
    public static boolean nextTo(int firstX, int firstY, int secondX, int secondY) {
        return (Math.abs(firstX - secondX) == 1) && (Math.abs(firstY - secondY) == 0)
                || (Math.abs(firstX - secondX) == 0) && (Math.abs(firstY - secondY) == 1);
    }
    private void replacePath(List<Command> commands) {
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
        changeState(inConflict);
        replacePath(commands);
    }
    public void handleConflict(int waitingTime) {
        waitingProcedure(waitingTime);
    }
    public boolean isMovingBox() { return currentState == movingBox;}
    public String getCurrentState() { return "" + currentState;}
    public boolean isBoxAttached() {
    	return (attachedBox != null);
    }
    public int getPriority() {return currentState.ordinal();}
    public void updatePosition() throws UnsupportedOperationException {
        switch (currentState) {
            case waiting:
                waitingCounter--;
            case unassigned:
            case jobless:
                return;
            case inConflict:
            case movingTowardsBox:
            case movingBox:
                finalizeNextMove();
                return;
        }
        serverOutput = null;
    }
    private void finalizeNextMove() {
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
        beforeObstacleState = currentState;
        changeState(pathBlocked);
        waitingProcedure(3);
    }
    public void revertMoveIntention(MainBoard board) {
        System.err.println("reverting Agent "+getID());
        if (hasMoved() && path != null && !path.isEmpty()) {
            if(serverOutput.equals("NoOp")) {
                serverOutput = null;
                return;
            }
            Node nextStep = path.peek();
            System.err.println(nextStep.toString());
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


            System.err.println("Agent "+getID()+" has been reverted");
        }
        serverOutput = null;
    }
    public Box getAttachedBox() {
        return attachedBox;
    }
    public boolean hasMoved() { return serverOutput != null;}
    public boolean jobless() { return currentState == jobless;}
    public void moveYourAss() { changeState(unassigned);}
    public boolean isWaiting(){
        return currentState == waiting || currentState == jobless || currentState == pathBlocked;
    }
}
