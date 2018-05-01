package sampleclients;

import java.util.LinkedList;

public class AnticipationTest {

    public static boolean XConflictTest() {

        AnticipationPlanning anticipationPlanning = new AnticipationPlanning();

        Agent agentA = new Agent('A', "red", 2, 0);

        LinkedList<Node> pathA = new LinkedList<>();
        pathA.add(new Node(null, new Command(Command.type.Move, Command.dir.E, Command.dir.E), 0, 2));
        pathA.add(new Node(null, new Command(Command.type.Move, Command.dir.E, Command.dir.E), 1, 2));
        pathA.add(new Node(null, new Command(Command.type.Move, Command.dir.N, Command.dir.N), 2, 2));
       // pathA.add(new Node(null, new Command(Command.type.Move, Command.dir.N, Command.dir.N), 2, 1));

        anticipationPlanning.addPath(pathA, agentA);

        Agent agentB = new Agent('B', "blue", 2, 4);

        LinkedList<Node> pathB = new LinkedList<>();
        pathB.add(new Node(null, new Command(Command.type.Move, Command.dir.E, Command.dir.E), 4, 2));
        pathB.add(new Node(null, new Command(Command.type.Move, Command.dir.E, Command.dir.E), 3, 2));
        pathB.add(new Node(null, new Command(Command.type.Move, Command.dir.N, Command.dir.N), 2, 2));

        Agent conflictingAgent = anticipationPlanning.addPath(pathB, agentB);

        return conflictingAgent == agentA;
    }

    public static boolean NSEWConflictTest() {

        AnticipationPlanning anticipationPlanning = new AnticipationPlanning();

        Agent agentA = new Agent('A', "red", 2, 0);

        LinkedList<Node> pathA = new LinkedList<>();
        pathA.add(new Node(null, new Command(Command.type.Move, Command.dir.E, Command.dir.E), 0, 2));
        pathA.add(new Node(null, new Command(Command.type.Move, Command.dir.E, Command.dir.E), 1, 2));
        pathA.add(new Node(null, new Command(Command.type.Move, Command.dir.N, Command.dir.N), 2, 2));
        // pathA.add(new Node(null, new Command(Command.type.Move, Command.dir.N, Command.dir.N), 2, 1));

        anticipationPlanning.addPath(pathA, agentA);

        Agent agentB = new Agent('B', "blue", 2, 3);

        LinkedList<Node> pathB = new LinkedList<>();
        pathB.add(new Node(null, new Command(Command.type.Move, Command.dir.E, Command.dir.E), 3, 2));
        pathB.add(new Node(null, new Command(Command.type.Move, Command.dir.E, Command.dir.E), 2, 2));
        pathB.add(new Node(null, new Command(Command.type.Move, Command.dir.N, Command.dir.N), 1, 2));

        Agent conflictingAgent = anticipationPlanning.addPath(pathB, agentB);

        return conflictingAgent == agentA;
    }

    public static boolean NegativeNSEWConflictTest() {

        AnticipationPlanning anticipationPlanning = new AnticipationPlanning();

        Agent agentA = new Agent('A', "red", 1, 0);

        LinkedList<Node> pathA = new LinkedList<>();
        pathA.add(new Node(null, new Command(Command.type.Move, Command.dir.E, Command.dir.E), 0, 2));
        pathA.add(new Node(null, new Command(Command.type.Move, Command.dir.E, Command.dir.E), 1, 2));
        pathA.add(new Node(null, new Command(Command.type.Move, Command.dir.N, Command.dir.N), 2, 2));
        pathA.add(new Node(null, new Command(Command.type.Move, Command.dir.N, Command.dir.N), 2, 1));

        anticipationPlanning.addPath(pathA, agentA);
        ///////

        Agent agentC = new Agent('C', "yellow", 0, 4);

        LinkedList<Node> pathC = new LinkedList<>();
        pathC.add(new Node(null, new Command(Command.type.Move, Command.dir.W, Command.dir.W), 4, 0));
        pathC.add(new Node(null, new Command(Command.type.Move, Command.dir.S, Command.dir.S), 3, 0));
        pathC.add(new Node(null, new Command(Command.type.Move, Command.dir.S, Command.dir.S), 3, 1));
        pathC.add(new Node(null, new Command(Command.type.Move, Command.dir.S, Command.dir.S), 3, 2));

        Agent noConflictingAgent2 = anticipationPlanning.addPath(pathC, agentC);

        Agent agentB = new Agent('B', "blue", 3, 4);

        LinkedList<Node> pathB = new LinkedList<>();
        pathB.add(new Node(null, new Command(Command.type.Move, Command.dir.N, Command.dir.N), 4, 3));
        pathB.add(new Node(null, new Command(Command.type.Move, Command.dir.W, Command.dir.W), 4, 2));
        pathB.add(new Node(null, new Command(Command.type.Move, Command.dir.W, Command.dir.W), 3, 2));
        pathB.add(new Node(null, new Command(Command.type.Move, Command.dir.W, Command.dir.W), 2, 2));

        Agent noConflictingAgent = anticipationPlanning.addPath(pathB, agentB);



        return noConflictingAgent == null && noConflictingAgent2 == null;
    }

    public static void main( String[] args ) {

        System.err.println("XConflictTest : " + XConflictTest());

        System.err.println("NSEWConflictTest : " + NSEWConflictTest());

        System.err.println("\n---\n");

      System.err.println("NegativeNSEWConflictTest : " + NegativeNSEWConflictTest());

    }


}
