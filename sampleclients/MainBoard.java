package sampleclients;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class MainBoard {
    private List< List<BasicObject>> gameBoard;
    public static Map<String, Map<Character, Box>> BoxColorGroups = new HashMap<>();
    public static List< Agent > agents = new ArrayList<>();
    public static Map<Character, List<Box>> boxesByID = new HashMap<>();
    public static List<Box> allBoxes = new LinkedList<>();
    public static Map<String, Map<Character, Agent>> AgentColorGroups = new HashMap<>();
/*<<<<<<< HEAD
    public static Map<Character, Set<Goal>> goals = new HashMap<>();
    public static Map<Goal,Set<Goal>> Dep = new HashMap<>();
=======*/
    public static Map<Goal,Set<Goal>> Dep = new HashMap<>();
    public static Map<Character, List<Goal>> goalsByID = new HashMap<>();
    public static List<Goal> allGoals = new LinkedList<>();
//>>>>>>> 51ef2cea5c1ac09d2b9dedbefeb6219fe209e671
    public static int MainBoardYDomain = 0, MainBoardXDomain = 0;
    private Map<MovingObject, Goal> steppedOnGoals = new HashMap<>();


    public List<List<BasicObject>> getGameBoard() {
        return gameBoard;
    }
    //GLOBAL HERE
    public MainBoard(BufferedReader in) {
        gameBoard = new ArrayList<>();
        try {
            readMap(in);
        }
        catch (IOException exc) {
            System.err.println("Cannot load map");
        }
    }
    public MainBoard(MainBoard copy) {
        gameBoard = new ArrayList<>();
        for(List<BasicObject> list : copy.getGameBoard()) {
            List<BasicObject> array = new ArrayList<>();
            for(BasicObject obj : list) {
                array.add(obj);
            }
            gameBoard.add(array);
        }
    }

    private void readMap(BufferedReader in) throws IOException{
        Map< Character, String > colors = new HashMap< Character, String >();
        String line, color;

        // Read lines specifying colors
        while ( ( line = in.readLine() ).matches( "^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$" ) ) {
            line = line.replaceAll( "\\s", "" );
            color = line.split( ":" )[0];

            for ( String id : line.split( ":" )[1].split( "," ) )
                colors.put( id.charAt( 0 ), color );
        }

        // Read lines specifying level layout
        ArrayList<String> table = new ArrayList<>();
        int currentX = 0;
        while ( !line.equals( "" ) ) {
            ArrayList<BasicObject> objects = new ArrayList<>();
            for ( int i = 0; i < line.length(); i++ ) {
                char id = line.charAt( i );
                if (isAgent(id)) {
                    String currentColor = colors.get( id );
                    if(currentColor == null) currentColor = "blue";
                    Agent newAgent = new Agent( id,currentColor, MainBoardYDomain, currentX);
                    objects.add(i, newAgent);
                    agents.add( newAgent );
                    Map<Character, Agent> result = AgentColorGroups.get(currentColor);
                    if (result == null) {
                        result = new HashMap<>();
                        result.put(id, newAgent);
                        AgentColorGroups.put(currentColor, result);
                    }
                    else {
                        result.put(id, newAgent);
                    }

                }
                else if (isBox(id)) {
                    String currentColor = colors.get( id );
                    if(currentColor == null) currentColor = "blue";
                    Box newBox = new Box( id, currentColor, MainBoardYDomain, currentX);
                    allBoxes.add(newBox);
                    List<Box> boxResult = boxesByID.get(id);
                    objects.add(i, newBox);
                    Map<Character, Box> result = BoxColorGroups.get(currentColor);
                    if (result == null) {

                        result = new HashMap<>();
                        result.put(id, newBox);
                        BoxColorGroups.put(currentColor, result);
                    }
                    else {
                        result.put(id, newBox);
                    }
                    if(boxResult == null) {
                        boxResult = new LinkedList<>();
                        boxResult.add(newBox);
                        boxesByID.put(id, boxResult);
                    }
                    else {
                        boxResult.add(newBox);
                    }
                }
                else if(isGoal(id)) {
                    Goal goal = new Goal(id, MainBoardYDomain, currentX);
/*
<<<<<<< HEAD

                    Set<Goal> set = goals.get(id);

                    if(set!=null){
                        set.add(goal);
                    } else{
                        set = new HashSet<Goal>();
                    }
                    goals.put(id,set);

=======
*/
                    allGoals.add(goal);
//>>>>>>> 51ef2cea5c1ac09d2b9dedbefeb6219fe209e671
                    objects.add(i, goal);
                    List<Goal> goalRes = goalsByID.get(id);
                    if(goalRes == null) {
                        goalRes = new LinkedList<>();
                        goalRes.add(goal);
                        goalsByID.put(id, goalRes);
                    }
                    else {
                        goalRes.add(goal);
                    }
                }
                else if(isWall(id)) {
                    objects.add(i, new Wall(id, currentX, MainBoardYDomain));
                }
                else {
                    objects.add(i, null);
                }
                ++currentX;
            }
            if(MainBoardXDomain < currentX) { MainBoardXDomain = currentX; }
            table.add(line);
            gameBoard.add(MainBoardYDomain, objects);
            line = in.readLine();
            currentX = 0;
            ++MainBoardYDomain;
        }
        Collections.sort(agents, (left, right) -> left.getID() - right.getID());

    }
    public static boolean isAgent (char id) { return ( '0' <= id && id <= '9' );}
    public static boolean isBox (char id) { return ( 'A' <= id && id <= 'Z' );}
    public static boolean isGoal (char id) { return ( 'a' <= id && id <= 'z' ); }
    public static boolean isWall (char id) {return (id == '+');}

    //returns object under given coordinates
    BasicObject getElement(int x, int y) {
        if(yOutOfBounds(y) || xOutOfBounds(x)) throw new UnsupportedOperationException();
        return gameBoard.get(y).get(x);
    }
    //used only internally, never expose this
    private void setElement(int x, int y, BasicObject obj) {
        gameBoard.get(y).set(x, obj);
    }
    public boolean isAgent (int x, int y) {
        if(getElement(x, y) == null) return false;
        return (isAgent(getElement(x, y).getID()));
    }
    public boolean isBox (int x, int y) {
        if(getElement(x, y) == null) return false;
        return (isBox(getElement(x, y).getID()));
    }
    public boolean isGoal (int x, int y) {
        if(getElement(x, y) == null) return false;
        return (isGoal(getElement(x, y).getID()));
    }
    public boolean isWall (int x, int y) {
        if(getElement(x, y) == null) return false;
        return (isWall(getElement(x, y).getID()));
    }
    public boolean isFree (int x, int y) {
        return getElement(x, y) == null || isGoal(x,y);
    }

    //Function assumes that passed object is at its' getX and getY location on the map
    public void changePositionOnMap(MovingObject obj, int x, int y) {
        if(!spaceEmpty(x,y) || obj == null) throw new UnsupportedOperationException();
        manageMovingThroughGoal(obj, x, y);
        if(getElement(obj.getX(), obj.getY()) == obj) {
            setElement(obj.getX(), obj.getY(), null);
        }
        setElement(x, y, obj);
    }
    public void revertPositionChange(MovingObject obj, int xFrom, int yFrom) {
        if(!spaceEmpty(obj.getX(),obj.getY())) throw new UnsupportedOperationException();
        manageMovingThroughGoal(obj, obj.getX(), obj.getY());
        if(getElement(xFrom, yFrom) == obj) {
            setElement(xFrom, yFrom, null);
        }
        setElement(obj.getX(), obj.getY(), obj);
    }
    private boolean yOutOfBounds(int y) { return (y >= (MainBoardYDomain) || y < 0);}
    private boolean xOutOfBounds(int x) {return (x >= (MainBoardXDomain) || x < 0);}
    private boolean spaceEmpty(int x, int y) {return isGoal(x, y) || getElement(x, y) == null; }
    private void manageMovingThroughGoal(MovingObject obj, int x, int y) {
        Goal steppedOnGoal = steppedOnGoals.get(obj);
        if(steppedOnGoal  != null) {
            setElement(steppedOnGoal.getX(), steppedOnGoal.getY(), steppedOnGoal);
            steppedOnGoals.remove(obj);
        }
        if(isGoal(x,y)) {
            steppedOnGoals.put(obj, (Goal) getElement(x,y));
        }
    }

    @Override
    public String toString () {
        StringBuilder table = new StringBuilder(MainBoardXDomain * (MainBoardYDomain + 1) + 5);
        for(List<BasicObject> list : gameBoard) {
            for(BasicObject obj : list) {
                if(obj != null) {
                    table.append(obj.getID());
                }
                else {
                    table.append(' ');
                }
            }
            table.append(System.getProperty("line.separator"));
        }
        return table.toString();
    }
}
