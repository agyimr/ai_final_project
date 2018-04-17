import java.util.LinkedList;

import static sampleclients.Command.dir;
import static sampleclients.Command.type;

public class Agent extends MovingObject {
    private boolean waiting = false;
    private Box attachedBox = null;
    boolean pushingBox = false;
    public Agent( char id, String color, int y, int x ) {
        super(id, color, y, x, "Agent");
    }
    public String act() {
        //TODO surprisingly server does not allow agents to follow each other, need to find a workaround
        if(attachedBox == null) {
            if(!findABox()) {
                return "NoOp";
            }
        }
        if(!pushingBox) {//then move towards box
            String result = executePathWithoutBox();
            if(result != null) return result;
            else if(nextToBox(attachedBox)) {
                pushingBox = true;
            }
            else return "NoOp";
        }

        //no assigned goal
        if (attachedBox.assignedGoal == null) {
            //try finding a goal
            attachedBox.assignedGoal = RandomWalkClient.goals.get(Character.toLowerCase(attachedBox.getID()));
            if (attachedBox.assignedGoal == null) {
                //no goal that satisfies the box on the map
                attachedBox.noGoalOntheMap = false;
                attachedBox = null;
                //try finding a box again
                return act();
            }
        }

        //box at the goal position!
        if (attachedBox.assignedGoal.atGoalPosition(attachedBox)) {
            attachedBox.atGoalPosition = true;
            attachedBox = null;
            pushingBox = false;
            //try finding a box again
            return act();
        }
        //box attached and not at the goal position
        else {
            //now you must make a move
            while(true) {
                if (attachedBox.path == null) {
                    attachedBox.findPath(attachedBox.assignedGoal.getX(), attachedBox.assignedGoal.getY());
                }
                String result = executePathWithBox();
                if (result != null) return result;
            }
        }

        //find a box!



        //check if move was scheduled and execute

    }
    boolean findABox() {
        Box newBox = null;
        for(MovingObject currentBox : RandomWalkClient.ColorGroups.get(getColor())) {
            if(currentBox instanceof Box) {
                newBox = (Box) currentBox;
                if (!newBox.atGoalPosition && (newBox.assignedAgent == null) && !newBox.noGoalOntheMap) {
                    findPathToBox(newBox);
                    if(path != null || nextToBox(newBox)) { // can find a path to box, or is next to!
                        attachedBox = newBox;
                        attachedBox.assignedAgent = this;
                        return true;
                    }
                }
            }
        }
        return false;
    }
    boolean nextToBox(Box current) {
        return (Math.abs(getX() - current.getX()) == 1) || (Math.abs(getY() - current.getY()) == 1);
    }
    public String executePathWithoutBox( ) {
        if (path != null) {
            Node nextStep = path.pollFirst();
            try {
                if (nextStep != null) {
                    return move(nextStep.getX(), nextStep.getY());
                }
            }
            catch(UnsupportedOperationException exc) {
                if(!waiting) {
//                  wait!
                    path.addFirst(nextStep);
                    waiting = true;
                    return "NoOp";
                }
                else {
//                  or find new path
                    waiting = false;
                    path = null;
                    findPathToBox(attachedBox);
                }
                return executePathWithoutBox();
            }
        }
        path = null;
        return null;
    }

    public String executePathWithBox( ) {
        if (attachedBox.path != null) {
            Node nextStep = attachedBox.path.pollFirst();
            try {
                if (nextStep != null)
                    return moveWithBox(nextStep.getX(), nextStep.getY());
            }
            catch(UnsupportedOperationException exc) {
                attachedBox.path.addFirst(nextStep);
                return "NoOp";
            }
        }
        return null;
    }
    public LinkedList<Node> findPathToBox(Box BoxToMoveTo) {
        RandomWalkClient.MainBoard[BoxToMoveTo.getY()][ BoxToMoveTo.getX()] = ' ';
        findPath(BoxToMoveTo.getX(), BoxToMoveTo.getY());
        if(path != null) path.pollLast();
        RandomWalkClient.MainBoard[BoxToMoveTo.getY()][ BoxToMoveTo.getX()] = BoxToMoveTo.getID();
        return path;
    }

    public String moveWithBox(int x, int y) throws UnsupportedOperationException {
        //save'em so you can restore the state if sth goes wrong`
        int  AttachedBoxCoordX = attachedBox.getX();
        int AttachedBoxCoordY = attachedBox.getY();
        int  currentX= getX();
        int currentY= getY();
        String move = null;
        try {
            if(x == getX() && y == getY()) {
                Node currentPos = new Node(getX(), getY());
                Node nextPos = currentPos.getNeighbours().iterator().next();
                move = type.Pull +  "(" + getDirection(nextPos.getX(), nextPos.getY()) + "," + invertDirection(attachedBox.getDirection(getX(), getY())) + ")";
                changePosition(nextPos.getX(), nextPos.getY());
                attachedBox.changePosition(x, y);
            }
            else {
                move = type.Push +  "(" + getDirection(attachedBox.getX(),attachedBox.getY()) + "," + attachedBox.getDirection(x, y) + ")";
                attachedBox.changePosition(x, y);
                changePosition(AttachedBoxCoordX, AttachedBoxCoordY);
            }
        }
        catch(UnsupportedOperationException exc) {
            forceNewPosition(currentX, currentY);
            attachedBox.forceNewPosition(AttachedBoxCoordX, AttachedBoxCoordY);
            throw exc;
        }
        return move;
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
}