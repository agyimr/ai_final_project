package sampleclients;


import java.awt.*;
import java.util.*;
import java.util.List;

import static sampleclients.Agent.possibleStates.*;
import static sampleclients.Command.type;
import static sampleclients.RandomWalkClient.anticipationPlanning;

//TODO reset goal and box upon completion of job
public class Agent extends MovingObject {
    private class ScheduledObstacle {
        Box obstacle;
        Agent inTrouble;
        public ScheduledObstacle(Box obstacle, Agent inTrouble) {
            this.obstacle = obstacle;
            this.inTrouble = inTrouble;
        }
        @Override
        public String toString() {
            return obstacle + ", " + inTrouble;
        }
    }
    private Box attachedBox = null;
    private SearchClient pathFindingEngine;
    private int waitingCounter = 0;

    private boolean safeSpotFound = false;

    private LinkedHashMap<Box, ScheduledObstacle> scheduledObstacles = new LinkedHashMap<>();
    private boolean pendingHelp = false;
    public Agent inTrouble = null;
    private boolean obstacleForced = false;
    private boolean rescueNotNeeded = false;

    private boolean handlingConflict = false;

    private boolean myPathIsBlocked = false;
    private int obstacleCounter = 0;

    private int joblessCounter = 0;
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
        removingObstacle,
        movingTowardsBox,
        movingBox,
        inConflict
    }
    String serverOutput = null;
    public Agent( char id, String color, int y, int x, int objID ) {
        super(id, color, y, x,objID, "Agent");
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
                    if(--joblessCounter < 0) {
                        changeState(unassigned);
                    }
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
        if(--obstacleCounter > 0) {
            if(!myPathIsBlocked) {
                obstacleCounter = 1;
            }
            System.err.println(ObstacleArbitrator.agentDictionary);
            System.err.println(scheduledObstacles);
            serverOutput = "NoOp";
        }
        else {
            clearPath();
            changeState(beforeObstacleState); //Causes problems with looping. TODO
            beforeObstacleState = null;
            rescueNotNeeded = false;
            myPathIsBlocked = false;
        }
    }
    private void searchForJob() {
        if(!scheduledObstacles.isEmpty()) {
            startObstacleRemoval();
        }
        else if(isBoxAttached()) {
            System.err.println("Box already attached: " + attachedBox);
            if(nextToBox(attachedBox)) {
                changeState(movingBox);
            }
            else {
                changeState(movingTowardsBox);
            }
        }
        else if( !findClosestBox()) {
            System.err.println("Cant find box: ");
            if(!safeSpotFound) {
                safeSpot = FindSafeSpot.safeSpotBFS(new Point(getX(), getY()));
                safeSpotFound = true;
                if(safeSpot == null ) {
                    changeState(jobless);
                } else {
                    findPathToSpot(safeSpot.x, safeSpot.y);
                    changeState(removingObstacle);
                }

            }
            else {
                joblessCounter = 40;
                safeSpotFound = false;
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
        if ((attachedBox.unassignedGoal() && !attachedBox.tryToFindAGoal()) || !attachedBox.assignedGoal.canBeSolved()) {
            finishTheJob();
        }
        else if(attachedBox.reachedAssignedGoal()) {
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
        if(attachedBox != null) {
            attachedBox.clearOwnerReferences();
            attachedBox = null;
        }
    }
    private boolean findClosestBox() {
        System.err.println("finding some box");
        Box newBox;
        Box bestBox = null;
        int bestPath = Integer.MAX_VALUE;
        if(MainBoard.BoxColorGroups.get(getColor()) == null) return false;
        for(MovingObject currentBox : MainBoard.BoxColorGroups.get(getColor())) {
            if(currentBox instanceof Box) {
                newBox = (Box) currentBox;
                if(newBox.noGoalOnTheMap || newBox.boxRemovalTime > anticipationPlanning.getClock() ||  (newBox.assignedAgent != null || newBox.atGoalPosition() )
                        || ((!newBox.canBeSolved()))) {
                    System.err.println("Box: " + newBox + "not for me!");
                    continue;
                }
                else {
                    System.err.println("here's my box!");
                    if(nextToBox(newBox)) { // can find a path to box, or is next to!
                        attachedBox = newBox;
                        attachedBox.assignedAgent = this;
                        changeState(movingBox);
                        attachedBox.tryToFindAGoal();
                        return true;
                    }
                    int currentPath;
                    if(MainBoard.singleAgentMap) {
                        currentPath = RandomWalkClient.roomMaster.getEmptyPathEstimate(getCoordinates(), newBox.getCoordinates());
                    }
                    else {
                        currentPath = RandomWalkClient.roomMaster.getPathEstimate(getCoordinates(), newBox.getCoordinates(), this.getColor());
                    }
                    if(currentPath < bestPath) {
                        bestPath = currentPath;
                        bestBox = newBox;
                    }
                }
            }
        }
        if(bestBox != null) {
            System.err.println("Found box: " + bestBox + " with a goal: " + bestBox.assignedGoal);
            attachedBox = bestBox;
            attachedBox.assignedAgent = this;
            if(findPathToBox(attachedBox)) {
                attachedBox.tryToFindAGoal();
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
        System.err.println("reverting state " + currentState + " to " + previousState);
        if(previousState == jobless) previousState = unassigned;
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
            anticipationPlanning.removePath(path, this, anticipationPlanning.getClock());
        path = null;
    }
    private void startObstacleRemoval() {
        clearPath();
        Map.Entry<Box,ScheduledObstacle> entry = scheduledObstacles.entrySet().iterator().next();
        ScheduledObstacle obs = entry.getValue();
        if(!isBoxAttached() || attachedBox != obs.obstacle) {
            dropTheBox();
            attachedBox = obs.obstacle;
            attachedBox.assignedAgent = this;
        }
        changeState(removingObstacle);
        System.err.println("Obstacle scheduled, removing: " + obs);
        this.inTrouble = obs.inTrouble;
        pendingHelp = false;
        findObstaclePath();

    }
    private void findObstaclePath() {
        System.err.println("Agent: " + this + "Trying to remove obstacle:" + getAttachedBox());
        try {
            if (isBoxAttached()) {
                if (nextToBox(attachedBox)) {
                    if (attachedBox.unassignedGoal() && !attachedBox.tryToFindAGoal()) {
                        safeSpot = FindSafeSpot.safeSpotBFS(new Point(attachedBox.getX(), attachedBox.getY()));
                        findPathWithBox(safeSpot.x, safeSpot.y);

                    } else {
                        if (!obstacleForced && findPathWithBox(attachedBox.assignedGoal.getX(), attachedBox.assignedGoal.getY())
                                && !agentOnMyPathWithBox()) {
                            changeState(possibleStates.movingBox);
                        } else {
                            safeSpot = FindSafeSpot.safeSpotBFS(new Point(attachedBox.getX(), attachedBox.getY()));
                            findPathWithBox(safeSpot.x, safeSpot.y);
                        }
                    }
                    releaseTroubledAgent();
                } else {
                    findPathToBox(attachedBox);
                }
            } else {
                safeSpot = FindSafeSpot.safeSpotBFS(new Point(getX(), getY()));
                findPathToSpot(safeSpot.x, safeSpot.y);
                releaseTroubledAgent();
            }
        }
        catch (NullPointerException exc) {
            System.err.println("Can't remove obstacle");
            obstacleJobIsDone();
            //throw new NegativeArraySizeException();
        }
    }
    private boolean agentOnMyPathWithBox() {
        for(Node step : path) {
            if(RandomWalkClient.gameBoard.getElement(step.agentX, step.agentY) == inTrouble ||
                    RandomWalkClient.gameBoard.getElement(step.boxX, step.boxY) == inTrouble) {
                System.err.println("Agent: " + inTrouble + " is on my path, and I'm trying to save him. Reverting to safeSpot");
                return true;
                //throw new NegativeArraySizeException();
            }
        }
        return false;
    }
    private void removeObstacle() {
        System.err.println(inTrouble);
        System.err.println(scheduledObstacles);
        System.err.println(attachedBox);
        if(!executePath()) {
            if (safeSpot == null) { // I either just arrived at the box position or havent found a path at all
                findObstaclePath();
            }
            else {
                if(isBoxAttached()) {
                    if( attachedBox.getCoordinates().equals(safeSpot) || getCoordinates().equals(safeSpot)) {
                        attachedBox.boxRemovalTime = anticipationPlanning.getClock() + 5;
                        obstacleJobIsDone();
                    }
                    else {
                        findPathWithBox(safeSpot.x, safeSpot.y);
                    }
                }
                else if(getCoordinates().equals(safeSpot) || SearchClient.nextTo(getX(), getY(), safeSpot.x, safeSpot.y)) {
                    obstacleJobIsDone();
                }
                else {
                    safeSpot = null;
                    findObstaclePath();
                }
            }
        }
    }
    private void obstacleJobIsDone() {
        System.err.println("Obstacle removed! ");
        releaseTroubledAgent();
        obstacleForced = false;
        safeSpot = null;
        dropTheBox();
        changeState(unassigned);
        waitingProcedure(1);
    }
    private void releaseTroubledAgent() {
        if(inTrouble != null) {
            System.err.println(inTrouble);
            System.err.println(attachedBox);
            System.err.println(scheduledObstacles);
            ObstacleArbitrator.jobIsDone(this, inTrouble);
            if(scheduledObstacles.remove(attachedBox) == null) {
                throw new NegativeArraySizeException();
            }
            inTrouble = null;
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
    }
    private boolean isRemovingObstacle() {
        return (currentState == removingObstacle || (handlingConflict && previousState == removingObstacle));
    }
    //external handlers
    public void youShallPass() {
        System.err.println("I can go now");
        myPathIsBlocked = false;
        pathFindingEngine.pathBlocked = false;
        rescueNotNeeded = true;
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
    public void scheduleObstacleRemoval(Box issue, Agent toRescue, int offset) {
        if(issue.assignedAgent!= null) {
            if(issue.assignedAgent != this) {
                issue.assignedAgent.scheduleObstacleRemoval(issue, toRescue, offset);
                return;
            }
        }
        scheduledObstacles.put(issue, new ScheduledObstacle(issue, toRescue));
        if((this.isMovingBox() && attachedBox != issue && pathSmallerThanOffset(offset)) || isRemovingObstacle() ) {
            System.err.println("Finishing job first!");
            //just finish the job
        }
        else {
            System.err.println("Help is pending says agent: " + this);
            pendingHelp = true;
            if(hasMoved() && isJobless()) {
                act();
            }
        }
    }
    public void forceObstacleRemoval(Box issue, Agent toRescue, int offset) {
        System.err.println("Forcing obstacle removal");
        ScheduledObstacle previous = scheduledObstacles.remove(issue);
        if(previous != null && toRescue != previous.inTrouble) {
            previous.inTrouble.youShallPass();
        }
        scheduledObstacles.put(issue, new ScheduledObstacle(issue, toRescue));
        pendingHelp = true;
        obstacleForced = true;
    }
    public void changeObstacle(Box issue) {
        System.err.println("Changing obstacle");
        ScheduledObstacle result = scheduledObstacles.remove(issue);
        if(result == null) {
            throw new NegativeArraySizeException();
        }
        scheduledObstacles.put(issue,result);
        pendingHelp = true;
    }

    private boolean pathSmallerThanOffset(int offset) {
        int pathLength;
        if(path != null) {
            pathLength = this.path.size();
        }
        else {
            pathLength = 0;
        }
        if(! pathFindingEngine.inGoalRoom()) {
            pathLength += pathFindingEngine.getNextRoomPathLengthEstimate();
        }
        return (pathLength < offset);
    }
    public void rescueIsNotNeeded() {
        System.err.println("Rescue not needed for agent: " + this);
        rescueNotNeeded = true;
    }
    public void handleConflict(List<Command> commands, boolean conflictOrigin,boolean act) {
        boolean needsToMove = false;
        if(hasMoved()) {
            revertMoveIntention(RandomWalkClient.nextStepGameBoard);
            needsToMove = true;
        }
        handlingConflict = true;
        changeState(inConflict);
        replacePath(commands);
        if(act && (needsToMove || conflictOrigin) ) {
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
    public boolean isMovingBox() {
        return (currentState == movingBox || (handlingConflict && previousState == movingBox))
            ||  (isRemovingObstacle() && attachedBox != null && (isBoxAttached() && nextToBox(attachedBox)));}
    public boolean isBoxAttached() {
    	return (attachedBox != null);
    }
    public int getPriority() {return currentState.ordinal();}
    public void waitForObstacleToBeRemoved() {
        if(rescueNotNeeded) {
            myPathIsBlocked = false;
            obstacleCounter = 2;
            System.err.println("Waiting 2 turns to test this shit");
            return;
        }
        else {
            myPathIsBlocked = true;
            obstacleCounter = 30;
        }
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
    public boolean isMyBox(Box issue) {
        if(attachedBox == issue) return true;
        return false;
    }
    public possibleStates getCurrentState() {
        return currentState;
    }
}
