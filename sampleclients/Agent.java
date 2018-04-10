package sampleclients;


import java.io.*;
import java.util.*;
import java.awt.Point;
import static sampleclients.Command.dir;
import static sampleclients.Command.type;

public class Agent extends MovingObject {
    private static final int WAITING_MAX = 3;
    private boolean waiting = false;


    public Box getAttachedBox() {
        return attachedBox;
    }

    public Point getAgentPoint(){
        return new Point(this.getX(),this.getY());
    }

    private Box attachedBox = null;


    public Point getAttachedBoxPoint() {
        Point tmp = new Point(attachedBox.getX(),attachedBox.getY());
        return tmp;
    }

    boolean pushingBox = false;

    boolean isMovingBox = false;
    boolean pushing = false;
    public Node nextPullingPosition = null;
    int waitingCounter = 0;

    public Agent( char id, String color, int y, int x ) {
        super(id, color, y, x, "Agent");
    }
    public String act() {
        //TODO surprisingly server does not allow agents to follow each other, need to find a workaround
        if(attachedBox == null) {
            if(!findABox()) {
                return waitingProcedure();
            }
        }
        if(!isMovingBox) {//then move towards box
            String result = executePathWithoutBox();
            if(result != null) return result;
            else if(nextToBox(attachedBox)) {
                isMovingBox = true;
            }
            else if(findPathToBox(attachedBox) == null) {
                return waitingProcedure();
            }
            else return executePathWithoutBox();
        }
        //no assigned goal
        if (attachedBox.assignedGoal == null) {
            //try finding a goal
            attachedBox.assignedGoal = RandomWalkClient.goals.get(Character.toLowerCase(attachedBox.getID()));
            if (attachedBox.assignedGoal == null) {
                //no goal that satisfies the box on the map
                attachedBox.noGoalOntheMap = true;
                attachedBox.clearOwnerReferences();
                attachedBox = null;
                //try finding a box again
                return act();
            }
        }


        //box at the goal position!
        if (attachedBox.assignedGoal.atGoalPosition(attachedBox)) {
            attachedBox.clearOwnerReferences();
            attachedBox.atGoalPosition = true;
            attachedBox = null;
            isMovingBox = false;
            //try finding a box again
            return act();
        }
        //box attached and not at the goal position
        else {
            //now you must make a move
            if (attachedBox.path == null) {
                attachedBox.findPath(attachedBox.assignedGoal.getX(), attachedBox.assignedGoal.getY());
            }
            String result = executePathWithBox();
            if (result != null) return result;
            else {
                //path blocked?
                attachedBox.path = null;
                return waitingProcedure();

            }
        }
    }
    boolean findABox() {
        Box newBox = null;
        Box bestBox = null;
        LinkedList<Node> bestPath = null;
        for(MovingObject currentBox : RandomWalkClient.ColorGroups.get(getColor())) {
            if(currentBox instanceof Box) {
                newBox = (Box) currentBox;
                if (!newBox.atGoalPosition && (newBox.assignedAgent == null) && !newBox.noGoalOntheMap) {
                    findPathToBox(newBox);
                    if(bestPath == null) {
                        bestPath = path;
                        bestBox = newBox;
                        continue;
                    }
                    else if( nextToBox(newBox)) { // can find a path to box, or is next to!
                        attachedBox = newBox;
                        attachedBox.assignedAgent = this;
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
    boolean nextToBox(Box current) {
        return (Math.abs(getX() - current.getX()) == 1) && (Math.abs(getY() - current.getY()) == 0)
                || (Math.abs(getX() - current.getX()) == 0) && (Math.abs(getY() - current.getY()) == 1);
    }
    String executePathWithoutBox( ) {
        if (path != null) {
            Node nextStep = path.peek();
            if (nextStep != null) {
                return getMoveDirection(nextStep.getX(), nextStep.getY()).toString();
            }
        }
        path = null;
        return null;
    }
    String executePathWithBox( ) {
        if (attachedBox.path != null) {
            Node nextStep = attachedBox.path.peek();
            if (nextStep != null)
                return getMoveDirectionWithBox(nextStep.getX(), nextStep.getY()).toString();
        }
        path = null;
        return null;
    }
    LinkedList<Node> findPathToBox(Box BoxToMoveTo) {
        RandomWalkClient.MainBoard[BoxToMoveTo.getY()][ BoxToMoveTo.getX()] = ' ';
        findPath(BoxToMoveTo.getX(), BoxToMoveTo.getY());
        if(path != null) path.pollLast();
        RandomWalkClient.MainBoard[BoxToMoveTo.getY()][ BoxToMoveTo.getX()] = BoxToMoveTo.getID();
        return path;
    }
    String waitingProcedure() {
        waiting = true;
        if(waitingCounter >= WAITING_MAX) {
            if(attachedBox != null) {
                attachedBox.noGoalOntheMap = true; // this is only temporary to see it work without conflict resolutions!!
                attachedBox.clearOwnerReferences();
                attachedBox = null;
            }
        }
        return "NoOp";
    }
    Command getMoveDirectionWithBox(int x, int y) throws UnsupportedOperationException {
        if(x == getX() && y == getY()) {
            pushing = false;
            Node currentPos = new Node(getX(), getY());
            nextPullingPosition = currentPos.getNeighbours().iterator().next();
            if(nextPullingPosition != null) {
                return new Command( type.Pull, getDirection(nextPullingPosition.getX(), nextPullingPosition.getY()), invertDirection(attachedBox.getDirection(getX(), getY())));
            }
            else {
                return null;
                //TODO handle situation where you have no place to move
            }
        }
        else {
            pushing = true;
            return new Command(type.Push, getDirection(attachedBox.getX(),attachedBox.getY()), attachedBox.getDirection(x, y));
        }
    }
    Command getCommand(int i) {
        Node somePosition= null;
        try{
            if(isMovingBox) {
                somePosition = attachedBox.path.get(i);
                return getMoveDirection(somePosition.getX(), somePosition.getY());
            }
            else {
                somePosition = path.get(i);
                return getMoveDirectionWithBox(somePosition.getX(), somePosition.getY());
            }
        }
        catch (IndexOutOfBoundsException exc) {
            return null;
        }
    }

    boolean replacePath(List<Command> commands) {
        for(int i = 0; i< commands.size(); i++) {
            Command current = commands.get(i);
            if(current.actType == type.Move) {
                path.clear();
                Point nextCoords = current.getNext(new Point(getX(), getY()));
                path.add(new Node(nextCoords.x, nextCoords.y));
            }
            else if(current.actType == type.Push || current.actType == type.Pull) {
                List<Point> newList = new ArrayList<Point>();
                newList.add(new Point(getX(), getY()));
                newList.add(new Point(attachedBox.getX(), attachedBox.getY()));
                List<Point> nextCoords = current.getNext(newList);
                attachedBox.path.clear();
                attachedBox.path.add(new Node(nextCoords.get(1).x, nextCoords.get(1).y));
            }
            else {
                path.add(new Node(getX(), getY()));
            }
        }
        return true;
    }
    dir invertDirection(dir Direction) {
        switch (Direction) {
            case N:
                return dir.S;
            case S:
                return dir.N;
            case E:
                return dir.W;
            case W:
                return dir.E;
        }
        return null;
    }

    public boolean isBoxAttached() {
    	return attachedBox != null;
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
                AttachedBoxCoordX = attachedBox.getX();
                AttachedBoxCoordY = attachedBox.getY();
                Node nextStep = attachedBox.path.pollFirst();
                if(pushing) {
                    attachedBox.changePosition(nextStep.getX(), nextStep.getY(), RandomWalkClient.MainBoard);
                    changePosition(AttachedBoxCoordX, AttachedBoxCoordY,  RandomWalkClient.MainBoard);
                }
                else{
                    changePosition(nextPullingPosition.getX(), nextPullingPosition.getY(), RandomWalkClient.MainBoard);
                    attachedBox.changePosition(nextStep.getX(), nextStep.getY(), RandomWalkClient.MainBoard);
                }
            }
            else {
                Node nextStep = path.pollFirst();
                changePosition(nextStep.getX(), nextStep.getY(), RandomWalkClient.MainBoard);
            }
        }
        catch(UnsupportedOperationException exc) {
            forceNewPosition(currentX, currentY,  RandomWalkClient.MainBoard);
            if(isMovingBox) {
                attachedBox.forceNewPosition(AttachedBoxCoordX, AttachedBoxCoordY,  RandomWalkClient.MainBoard);
            }
            throw exc;
        }

    }
}
