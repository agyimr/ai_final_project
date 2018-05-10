package sampleclients;


import java.awt.*;
import java.util.*;
import java.util.List;

import static sampleclients.Agent.possibleStates.*;
import static sampleclients.Command.type;

public class Agent extends MovingObject {
    private Box attachedBox = null;
    private SearchClient pathFindingEngine;
    private int waitingCounter = 0;
    public int conflictSteps = 0;
    private Box nextBoxToPush = null;
    private boolean pendingHelp = false;
    private boolean myPathIsBlocked = false;
    public LinkedList<Node> path;
    private possibleStates currentState = unassigned;
    private possibleStates previousState = currentState;
    private possibleStates beforeObstacleState = unassigned;
    private Point safeSpot = null;
    enum possibleStates {
        jobless,
        unassigned,
        waiting,
        pathBlocked,
        movingTowardsBox,
        movingBox,
        removingObstacle,
        inConflict
    }
    String serverOutput = null;
    public Agent( char id, String color, int y, int x ) {
        super(id, color, y, x, "Agent");
        pathFindingEngine = new SearchClient(this);
    }
    public void act(){
        serverOutput = null;
        if(pendingHelp) {
            startObstacleRemoval();
        }
        System.err.println("Agent "+getID()+" acting");
        System.err.println("Starting CurrentState: "+currentState);
        while(serverOutput == null) {
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
                case removingObstacle:
                    removeObstacle();
            }
        }
        System.err.println("Ending current state: "+currentState);
        System.err.println("ServerOutput: "+serverOutput);
//        act(); // Temporary, just to cause stackOverflow instead of infinite loop, for better debugging
    }
    public String collectServerOutput() {
        System.err.println("Agent "+getID());
        if(serverOutput == null) throw new NegativeArraySizeException();
        return serverOutput;
    }
    private void checkPath() {
        if(myPathIsBlocked) {
            serverOutput = "NoOp";
        }
        else {
            changeState(beforeObstacleState);
            beforeObstacleState = null;
        }
    }
    private void searchForJob() {
        if(nextBoxToPush != null) {
            startObstacleRemoval();
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
        if(path.isEmpty() || !executePath()){
            revertState();
        }
    }
    private void moveToTheBox() {
        if(executePath()) return;
        else if(nextToBox(attachedBox)) {
            System.err.println("isNext to box: ");
            changeState(movingBox);
        }
        else if(!findPathToBox(attachedBox)) {
            waitingProcedure(3);
        }
        else {
            System.err.println("Moving towards box: ");
            executePath();
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
            if ( executePath()) return;
            else if (!findPathWithBox(attachedBox.assignedGoal.getX(), attachedBox.assignedGoal.getY())) {
                waitingProcedure(2);
            }
            else {
                executePath();
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
        if(waitingCounter <= 0) {
            revertState();
        }
        else {
            serverOutput = "NoOp";
        }
    }
    private void changeState(possibleStates nextState) {
        System.err.println("next state: " + nextState + "current: " + currentState + "previous: " + previousState);
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
    private boolean executePath( ) {
        if(path == null || path.isEmpty()) path = pathFindingEngine.continuePath();
        if (path != null) {
            Node nextStep = path.peek();
            if (nextStep != null) {
                System.err.println("try to move");
                if(!tryToMove(nextStep)) {
                    //System.err.println(path);
                    clearPath();
                    return false;

                }
                serverOutput = nextStep.action.toString();
                return true;
            }
        }
        clearPath();
        return false;
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
    private boolean findPathWithBox(int goalX, int goalY) {
        if( ! pathFindingEngine.getPath(true, goalX, goalY)) return false;
        path = pathFindingEngine.continuePath();
        if(path != null)
            return true;
        else
            return false;
    }
    private boolean findPathToSpot(int goalX, int goalY) {
        if( ! pathFindingEngine.getPath(false, goalX, goalY)) return false;
        path = pathFindingEngine.continuePath();
        if(path != null)
            return true;
        else
            return false;
    }
    private void clearPath() {
        if(path != null)
            RandomWalkClient.anticipationPlanning.removePath(path, this, RandomWalkClient.anticipationPlanning.getClock());
        path = null;
    }

    private void startObstacleRemoval() {
        clearPath();
        if(isBoxAttached()) {
            dropTheBox();
        }
        changeState(removingObstacle);
        attachedBox = nextBoxToPush;
        nextBoxToPush = null;
        pendingHelp = false;
        findObstaclePath();
    }
    private void findObstaclePath() {
        if (isBoxAttached()) {
            if (nextToBox(attachedBox)) {
                if (attachedBox.assignedGoal == null && !attachedBox.tryToFindAGoal()) {
                    safeSpot = FindSafeSpot.safeSpotBFS(new Point(attachedBox.getX(), attachedBox.getY()));
                    findPathWithBox(safeSpot.x, safeSpot.y);
                } else {
                    if (findPathWithBox(attachedBox.assignedGoal.getX(), attachedBox.assignedGoal.getY())) {
                        changeState(possibleStates.movingBox);
                    } else {
                        safeSpot = FindSafeSpot.safeSpotBFS(new Point(attachedBox.getX(), attachedBox.getY()));
                        findPathWithBox(safeSpot.x, safeSpot.y);
                    }
                }
                //TODO wake the agent up
                ObstacleArbitrator.jobIsDone(this);
            } else {
                findPathToBox(attachedBox);
            }
        } else {
            safeSpot = FindSafeSpot.safeSpotBFS(new Point(attachedBox.getX(), attachedBox.getY()));
            findPathToSpot(safeSpot.x, safeSpot.y);
            ObstacleArbitrator.jobIsDone(this);
            //TODO wake the agent up
        }
    }
    private void removeObstacle() {
        if(!executePath()) {
            if (safeSpot == null) { // I either just arrived at the box position or havent found a path at all
                findObstaclePath();
            }
            else {
                safeSpot = null;
                changeState(unassigned);
            }
        }
    }
    //external handlers
    public void youShallPass() {
        myPathIsBlocked = false;
        pathFindingEngine.pathBlocked = false;
    }
    private void replan() {
        clearPath();
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
    public void scheduleObstacleRemoval(Box issue, int offset) {
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
    public void handleConflict(List<Command> commands, boolean conflictOrigin) {
        boolean needsToMove = false;
        if(hasMoved()) {
            revertMoveIntention(RandomWalkClient.nextStepGameBoard);
            needsToMove = true;
        }
        changeState(inConflict);
        clearPath();
        replacePath(commands);
        if(needsToMove || conflictOrigin) {
            act();
        }
    }
    public void handleConflict(int waitingTime, boolean conflictOrigin, boolean replanNeeded) {
        boolean needsToMove = false;
        if(hasMoved()) {
            revertMoveIntention(RandomWalkClient.nextStepGameBoard);
            needsToMove = true;
        }
        if(replanNeeded) {
            clearPath();
        }
        waitingProcedure(waitingTime);
        if(needsToMove || conflictOrigin) {
            act();
        }

    }
    public boolean isMovingBox() { return currentState == movingBox;}
    public String getCurrentState() { return "" + currentState;}
    public boolean isBoxAttached() {
    	return (attachedBox != null);
    }
    public int getPriority() {return currentState.ordinal();}
    public void updatePosition() throws UnsupportedOperationException {
        serverOutput = null;
        switch (currentState) {
            case waiting:
                waitingCounter--;
            case unassigned:
            case jobless:
            case pathBlocked:
                return;
            case inConflict:
            case movingTowardsBox:
            case removingObstacle:
            case movingBox:
                finalizeNextMove();
                return;
        }
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
                    clearPath();
                    return;
                }
                Box movedObject = (Box) RandomWalkClient.gameBoard.getElement(nextStep.boxX, nextStep.boxY);
                setCoordinates(nextStep.agentX, nextStep.agentY);
                movedObject.setCoordinates(nextStep.boxX, nextStep.boxY);
        }
    }
    public void waitForObstacleToBeRemoved() {
        myPathIsBlocked = true;
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
            serverOutput = null;
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
    }
    public Box getAttachedBox() {
        return attachedBox;
    }
    public boolean hasMoved() { return serverOutput != null;}
    public boolean jobless() { return currentState == jobless;}
    public void moveYourAss() {
        changeState(unassigned);
    }
    public boolean isWaiting(){
        return currentState == waiting || currentState == jobless || currentState == pathBlocked || currentState == unassigned;
    }
}
