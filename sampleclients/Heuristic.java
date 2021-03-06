package sampleclients;

import sampleclients.room_heuristics.Section;

import java.awt.*;
import java.util.Comparator;

import static java.lang.Math.abs;

public abstract class Heuristic implements Comparator<Node> {
    private boolean pushingBox;
    private Agent owner;
    private int goalX, goalY;
    private Section goalRoom = null;
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
        int value;


        if(goalRoom != null) {
            if(pushingBox) {
                value = getBoxHeuristic(n, goalRoom.getDistanceFromPoint(new Point(n.boxX, n.boxY)));
            }
            else {
                value = getAgentHeuristic(n, goalRoom.getDistanceFromPoint(new Point(n.agentX, n.agentY)));
            }
        }
        else {
            if(pushingBox) {
                value = getBoxHeuristic(n, ManhattanDistance(n.boxX, n.boxY, goalX, goalY));
            }
            else {
                value = getAgentHeuristic(n, ManhattanDistance(n.agentX, n.agentY,goalX,goalY));
            }
        }

        if(RandomWalkClient.anticipationPlanning.isConflicting(n, n.timeFrame)) {
            value += 8;
        }
        return value;
    }
    private int getBoxHeuristic(Node n, int distance) {
        if(n.boxX == -1) return h(n.parent);
        if(n.boxes[n.boxY][n.boxX] == owner.getAttachedBox()) {
            if(n.action.actType == Command.type.Pull) {
                return distance + 1;
            }
            return distance;
        }
        else if(n.boxes[n.boxY][n.boxX].assignedAgent != null) {
            return h(n.parent) + 4;
        }
        else if(n.boxes[n.boxY][n.boxX].atGoalPosition()) return h(n.parent) + 4;
        else{
            return h(n.parent) + 1;
        }
    }
    private int getAgentHeuristic(Node n, int distance) {
        switch (n.action.actType) {
            case Move:
            case Noop:
                return distance;
            case Push:
            case Pull:
                if(n.boxes[n.boxY][n.boxX].assignedAgent != null) return distance + 8;
                else if(n.boxes[n.boxY][n.boxX].atGoalPosition()) return distance + 4;
                else return distance + 2;
            default:
                return Integer.MAX_VALUE;
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
