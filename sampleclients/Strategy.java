package sampleclients;

import java.util.*;

public abstract class Strategy {
	public HashSet<Node> explored;

	public Strategy() {
		this.explored = new HashSet<Node>();
	}

	public void addToExplored(Node n) {
		this.explored.add(n);
	}

	public boolean isExplored(Node n) {
		return this.explored.contains(n);
	}



	public abstract Node getAndRemoveLeaf();

	public abstract void addToFrontier(Node n);

	public abstract boolean inFrontier(Node n);

	public abstract int countFrontier();

	public abstract boolean frontierIsEmpty();

	@Override
	public abstract String toString();

	public static class StrategyBFS extends Strategy {
		private ArrayDeque<Node> frontier;
		private HashSet<Node> frontierSet;

		public StrategyBFS() {
			super();
			frontier = new ArrayDeque<Node>();
			frontierSet = new HashSet<Node>();
		}

		@Override
		public Node getAndRemoveLeaf() {
			Node n = frontier.pollFirst();
			frontierSet.remove(n);
			return n;
		}

		@Override
		public void addToFrontier(Node n) {
			frontier.addLast(n);
			frontierSet.add(n);
		}

		@Override
		public int countFrontier() {
			return frontier.size();
		}

		@Override
		public boolean frontierIsEmpty() {
			return frontier.isEmpty();
		}

		@Override
		public boolean inFrontier(Node n) {
			return frontierSet.contains(n);
		}

		@Override
		public String toString() {
			return "Breadth-first Search";
		}
	}

	public static class StrategyDFS extends Strategy {
		private Stack<Node> frontier;
		private HashSet<Node> frontierSet;
		public StrategyDFS() {
			super();
			frontier = new Stack<Node>();
			frontierSet = new HashSet<Node>();
		}

		@Override
		public Node getAndRemoveLeaf() {
			Node n = frontier.pop();
			frontierSet.remove(n);
			return n;
		}

		@Override
		public void addToFrontier(Node n) {
			frontier.push(n);
			frontierSet.add(n);
		}

		@Override
		public int countFrontier() {
			return frontier.size();
		}

		@Override
		public boolean frontierIsEmpty() {
			return frontier.isEmpty();
		}

		@Override
		public boolean inFrontier(Node n) {
			return frontierSet.contains(n);
		}

		@Override
		public String toString() {
			return "Depth-first Search";
		}
	}

	// Ex 3: Best-first Search uses a priority queue (Java contains no implementation of a Heap data structure)
	public static class StrategyBestFirst extends Strategy {
		public Heuristic heuristic;
		private PriorityQueue<Node> frontier;
		private HashSet<Node> frontierSet;

		public StrategyBestFirst(Heuristic h) {
			super();
			this.heuristic = h;
			frontier = new PriorityQueue<>(10, heuristic);
			frontierSet = new HashSet<>();
		}
		void clear() {
			frontier.clear();
			frontierSet.clear();
			explored.clear();
		}
		@Override
		public Node getAndRemoveLeaf() {
            Node n = frontier.remove();
			frontierSet.remove(n);
			return n;
		}

		@Override
		public void addToFrontier(Node n) {
            frontier.add(n);
            frontierSet.add(n);
		}

		@Override
		public int countFrontier() { return frontier.size(); }

		@Override
		public boolean frontierIsEmpty() { return frontier.isEmpty(); }

		@Override
		public boolean inFrontier(Node n) { return frontierSet.contains(n); }

		@Override
		public String toString() { return "Best-first Search using " + this.heuristic.toString(); }
	}
}