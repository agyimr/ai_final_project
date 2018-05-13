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
    public HashSet<Box> obstacles = new HashSet<>();
    private boolean pendingHelp = false;
    private boolean handlingConflict = false;
    private boolean myPathIsBlocked = false;
    private int obstacleCounter = 0;
    public LinkedList<Node> path;
    private possibleStates currentState = unassigned;
    private possibleStates previousState = currentState;
    private possibleStates beforeObstacleState = unassigned;
    public Point safeSpot = null;
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
        if(pendingHelp && !handlingConflict) {
            startObstacleRemoval();
        }
        System.err.println("Agent "+getID()+" acting");
        System.err.println("Starting CurrentState: "+currentState);
        while(serverOutput == null) {
            switch (currentState) {
                case jobless:
                    serverOutput = "NoOp";
                    break;
                case waiting:
                    waitForSomeMiracle();
                    break;
                case unassigned:
                    searchForJob();
                    break;
                case pathBlocked:
                    checkPath();
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

        if(--obstacleCounter <= 0) {
            if(!myPathIsBlocked) {
                obstacleCounter = 1;
            }
            System.err.println(ObstacleArbitrator.helpersDictionary);
            serverOutput = "NoOp";
        }
        else {
            myPathIsBlocked = false;
            clearPath();
            changeState(beforeObstacleState);
            beforeObstacleState = null;
        }
    }
    private void searchForJob() {
        if(nextBoxToPush != null) {
            startObstacleRemoval();
        }
        else if(isBoxAttached()) {
            if(nextToBox(attachedBox)) {
                changeState(movingBox);
            }
            else {
                changeState(movingTowardsBox);
            }
        }
        else if( !findClosestBox()) {
            System.err.println("Cant find box: ");
            if(RandomWalkClient.gameBoard.isGoal(getX(), getY())) {
                safeSpot = FindSafeSpot.safeSpotBFS(new Point(getX(), getY()));
                if(safeSpot.x == getX() && safeSpot.y == getY()) {
                    safeSpot = null;
                    changeState(jobless);
                } else {
                    findPathToSpot(safeSpot.x, safeSpot.y);
                    changeState(removingObstacle);
                }

            }
            else {
                changeState(jobless);
            }
        }
        //maybe some other job?
    }
    private void resolveConflict() {
        if(path.isEmpty() || !executePath()){
            revertState();
            handlingConflict = false;
        }
    }
    private void moveToTheBox() {
        if(nextToBox(attachedBox)) {
            System.err.println("isNext to box: ");
            changeState(movingBox);
        }
        else if(executePath()) return;
        else if(!findPathToBox(attachedBox)) {
            finishTheJob();
            waitingProcedure(1);
        }
        else {
            System.err.println("Moving towards box: ");
            executePath();
        }
    }
    private void moveWithTheBox() {
        if ((attachedBox.unassignedGoal() && !attachedBox.tryToFindAGoal())) {
            finishTheJob();
        }
        else if(attachedBox.atGoalPosition()) {
            attachedBox.resetDependencies();
            finishTheJob();
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
                if(newBox.assignedGoal == null && !newBox.tryToFindAGoal()) {
                    continue;
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
            attachedBox = bestBox;
            if(findPathToBox(bestBox)) {
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
            handlingConflict = false;
        }
        else {
            serverOutput = "NoOp";
        }
    }
    private void changeState(possibleStates nextState) {
        System.err.println("State is about to change!\n\n");
        System.err.println("next state: " + nextState + " current: " + currentState + " previous: " + previousState);
        if(nextState == currentState) return;
        else if( currentState != waiting && currentState != inConflict ){
            previousState = currentState;
        }
        currentState = nextState;

    }
    private void revertState() {
        if(previousState == waiting || previousState == inConflict) previousState = unassigned;
        if(previousState == null) throw new NegativeArraySizeException();
        System.err.println("reverting state " + currentState + " to " + previousState);
        currentState = previousState;
        previousState = null;
    }
    private boolean nextToBox(Box current) {
        return SearchClient.nextTo(getX(), getY(), current.getX(), current.getY());
    }
    private boolean executePath( ) {
        if(path == null || path.isEmpty()) continuePath();
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
        if(nextToBox(BoxToMoveTo)) {
            return true;
        }
        if( ! pathFindingEngine.getPath(false, BoxToMoveTo.getX(), BoxToMoveTo.getY())) return false;
        continuePath();
        if(path != null)
            return true;
        else
            return false;
    }
    private void continuePath() {
        clearPath();
        path = pathFindingEngine.continuePath();
    }
    private boolean findPathWithBox(int goalX, int goalY) {
        if(attachedBox.getX() == goalX && attachedBox.getY() == goalY) {
            return true;
        }
        if( ! pathFindingEngine.getPath(true, goalX, goalY)) return false;
        continuePath();
        if(path != null)
            return true;
        else
            return false;
    }
    private boolean findPathToSpot(int goalX, int goalY) {
        if(getX() == goalX && getY() == goalY) {
            return true;
        }
        if( ! pathFindingEngine.getPath(false, goalX, goalY)) return false;
        continuePath();
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
                ObstacleArbitrator.jobIsDone(this);
            } else {
                findPathToBox(attachedBox);
            }
        } else {
            safeSpot = FindSafeSpot.safeSpotBFS(new Point(getX(), getY()));
            findPathToSpot(safeSpot.x, safeSpot.y);
            ObstacleArbitrator.jobIsDone(this);
        }
    }
    private void removeObstacle() {
        System.err.println(attachedBox);
        if(!executePath()) {
            if (safeSpot == null) { // I either just arrived at the box position or havent found a path at all
                findObstaclePath();
            }
            else {
                if(isBoxAttached()) {
                    if( attachedBox.getCoordinates().equals(safeSpot)) {
                        ObstacleArbitrator.jobIsDone(this);
                        safeSpot = null;
                        changeState(unassigned);
                    }
                    else {
                        findPathWithBox(safeSpot.x, safeSpot.y);
                    }
                }
                else if(getCoordinates().equals(safeSpot) || SearchClient.nextTo(getX(), getY(), safeSpot.x, safeSpot.y)) {
                    ObstacleArbitrator.jobIsDone(this);
                    safeSpot = null;
                    changeState(unassigned);
                }
                else {
                    safeSpot = FindSafeSpot.safeSpotBFS(new Point(getX(), getY()));
                    findPathToSpot(safeSpot.x, safeSpot.y);
                }
            }
        }
    }
    private void revertMoveIntention(MainBoard board) {
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
    private void replacePath(List<Command> commands) {
        clearPath();
        path = new LinkedList<>();
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

    //external handlers
    public void youShallPass() {
        myPathIsBlocked = false;
        pathFindingEngine.pathBlocked = false;
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
    public void finishTheJob() {
        clearPath();
        if(isBoxAttached()) {
            dropTheBox();
        }
        changeState(unassigned);
    }
    public void scheduleObstacleRemoval(Box issue, int offset) {
        if(issue == attachedBox) {
            System.err.println("Already on it!");
            return;
        }
        else if(issue.assignedAgent!= null) {
            issue.assignedAgent.finishTheJob();
        }
        nextBoxToPush = issue;
        if(this.isMovingBox() && pathSmallerThanOffset(offset) ) {
            System.err.println("Finishing job first!");
            //just finish the job
        }
        else {
            System.err.println("Help is pending!");
            pendingHelp = true;
            if(isJobless()) {
                act();
            }
        }
    }
    private boolean pathSmallerThanOffset(int offset) {
        int pathLength;
        pathLength = this.path.size();
        if(! pathFindingEngine.inGoalRoom()) {
            pathLength += pathFindingEngine.getNextRoomPathLengthEstimate();
        }
        return (pathLength < offset);
    }
    public void rescueIsNotNeeded() {
        obstacleCounter = 2;
    }
    public void handleConflict(List<Command> commands, boolean conflictOrigin) {
        boolean needsToMove = false;
        if(hasMoved()) {
            revertMoveIntention(RandomWalkClient.nextStepGameBoard);
            needsToMove = true;
        }
        handlingConflict = true;
        changeState(inConflict);
        replacePath(commands);
        if(needsToMove || conflictOrigin) {
            act();
        }
    }
    public void handleConflict(int waitingTime, boolean conflictOrigin, boolean newPlanNeeded) {
        boolean needsToMove = false;
        if(hasMoved()) {
            revertMoveIntention(RandomWalkClient.nextStepGameBoard);
            needsToMove = true;
        }
        handlingConflict = true;
        if(newPlanNeeded) {
            clearPath();
        }
        waitingProcedure(waitingTime);
        if(needsToMove || conflictOrigin) {
            act();
        }

    }
    public boolean isMovingBox() { return currentState == movingBox;}
    public boolean isBoxAttached() {
    	return (attachedBox != null);
    }
    public int getPriority() {return currentState.ordinal();}
    public void waitForObstacleToBeRemoved() {
        if(obstacleCounter <= 0) obstacleCounter = 10;
        myPathIsBlocked = true;
        beforeObstacleState = currentState;
        changeState(pathBlocked);
        //waitingProcedure(2);
    }
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
    public Box getAttachedBox() {
        return attachedBox;
    }
    public boolean hasMoved() { return serverOutput != null;}
    public boolean isJobless() { return currentState == jobless;}
    public void moveYourAss() {
        changeState(unassigned);
    }
    public boolean isWaiting(){
        return currentState == waiting || currentState == jobless || currentState == pathBlocked || currentState == unassigned;
    }
    public boolean isWithBox(){
        return isMovingBox() || (isBoxAttached() && nextToBox(attachedBox));
    }

    public possibleStates getCurrentState() {
        return currentState;
    }
}
