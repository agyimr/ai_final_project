package sampleclients;

import sampleclients.room_heuristics.Section;

import java.util.Comparator;

import static java.lang.Math.abs;

public abstract class Heuristic implements Comparator<Node> {
    boolean pushingBox;
    Agent owner;
    int goalX, goalY;
    Section goalRoom = null;
    public Heuristic(Agent owner) {
        this.owner = owner;
    }
    public void initializeSearch(boolean pushingBox, int goalX, int goalY) {
        this.pushingBox = pushingBox;
        this.goalX = goalX;
        this.goalY = goalY;
        goalRoom = null;
    }
    public void initializeSearch(boolean pushingBox, Section goalRoom) {
        this.pushingBox = pushingBox;
        this.goalX = -1;
        this.goalY = -1;
        this.goalRoom = goalRoom;
    }
	public int h(Node n) {
        if(pushingBox) {
            if(n.boxX == -1) return h(n.parent);
            if(n.boxes[n.boxY][n.boxX] == owner.getAttachedBox().getID()) {
                return ManhattanDistance(n.boxX, n.boxY, goalX, goalY);
            }
            else{
                return h(n.parent) + 1;
            }
        }
        else {
            if (n.action.actType == Command.type.Move ) return ManhattanDistance(n.agentX, n.agentY,goalX,goalY);
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
		public AStar(Agent owner) {
		    super(owner);
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

		public WeightedAStar( int W, Agent owner) {
            super(owner);
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
		public Greedy(Agent owner) {
			super(owner);
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
