package sampleclients;

import sampleclients.room_heuristics.Section;

import java.awt.*;
import java.util.Comparator;

import static java.lang.Math.abs;

public abstract class Heuristic implements Comparator<Node> {
    boolean pushingBox;
    Agent owner;
    int goalX, goalY;
    Section nextRoom = null;

    public Heuristic() {
    }

    public void initializeSearch(Agent owner, boolean pushingBox, int goalX, int goalY) {
        this.pushingBox = pushingBox;
        this.owner = owner;
        this.goalX = goalX;
        this.goalY = goalY;
        this.nextRoom = null;
    }

	public void initializeSearch(Agent owner, boolean pushingBox, Section to) {
		this.pushingBox = pushingBox;
		this.owner = owner;
		this.goalX = -1;
		this.goalY = -1;
		this.nextRoom = to;
	}

	public int h(Node n) {
        if(pushingBox) {
            //RandomWalkClient.printBoard(n.boxes);
            if(n.boxes[n.boxY][n.boxX] == owner.getAttachedBox().getID()) {
            	if (nextRoom != null) {
            		return nextRoom.getDistanceFromPoint(new Point(n.agentX, n.agentY));
				}
                //System.err.println(n.agentX + " " + n.boxX + " " + n.agentY + " "+ n.boxY);
                return ManhattanDistance(n.boxX, n.boxY, goalX, goalY);
            }
            else{
                return h(n.parent);
            }
        }
        else {
            if (n.action.actType == Command.type.Move) {
            	if (nextRoom != null) {
					return nextRoom.getDistanceFromPoint(new Point(n.agentX, n.agentY));
				}
            	return ManhattanDistance(n.agentX, n.agentY,goalX,goalY);
			} else {
				if (nextRoom != null) {
					return nextRoom.getDistanceFromPoint(new Point(n.agentX, n.agentY)) * 2;
				}
			}
            return  2 * ManhattanDistance(n.agentX, n.agentY,goalX,goalY);
        }
	}

    int ManhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
	public abstract int f(Node n);

	@Override
	public int compare(Node n1, Node n2) {
		return this.f(n1) - this.f(n2);
	}

	public static class AStar extends Heuristic {
		public AStar() {

		}

		@Override
		public int f(Node n) {
			return n.g() + this.h(n);
		}

		@Override
		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar( int W) {
			this.W = W;
		}

		@Override
		public int f(Node n) {
			return n.g() + this.W * this.h(n);
		}

		@Override
		public String toString() {
			return String.format("WA*(%d) evaluation", this.W);
		}
	}

	public static class Greedy extends Heuristic {
		public Greedy() {
			super();
		}

		@Override
		public int f(Node n) {
			return this.h(n);
		}

		@Override
		public String toString() {
			return "Greedy evaluation";
		}
	}
}
