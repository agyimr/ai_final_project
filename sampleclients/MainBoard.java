package sampleclients;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class MainBoard {
    private List< List<BasicObject>> gameBoard;
    public static Map<String, List<Box>> BoxColorGroups = new HashMap<>();
    public static List< Agent > agents = new ArrayList<>();
    public static Map<Character, List<Box>> boxesByID = new HashMap<>();
    public static List<Box> allBoxes = new LinkedList<>();
    public static Map<String, List<Agent>> AgentColorGroups = new HashMap<>();
    public static Map<Character, List<Goal>> goalsByID = new HashMap<>();
    public static List<Goal> allGoals = new LinkedList<>();
    public static int MainBoardYDomain = 0, MainBoardXDomain = 0;
    private Map<MovingObject, Goal> steppedOnGoals = new HashMap<>();

    public int getHeight() {
        return gameBoard.size();
    }

    public int getWidth() {

        int width = 0;

        for(List<BasicObject> line : gameBoard) {
            width = Math.max(width, line.size());
        }

        return width;
    }

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
                    List<Agent> result = AgentColorGroups.get(currentColor);
                    if (result == null) {
                        result = new LinkedList<>();
                        result.add(newAgent);
                        AgentColorGroups.put(currentColor, result);
                    }
                    else {
                        result.add(newAgent);
                    }

                }
                else if (isBox(id)) {
                    String currentColor = colors.get( id );
                    if(currentColor == null) currentColor = "blue";
                    Box newBox = new Box( id, currentColor, MainBoardYDomain, currentX);
                    allBoxes.add(newBox);
                    List<Box> boxResult = boxesByID.get(id);
                    objects.add(i, newBox);
                    List<Box> result = BoxColorGroups.get(currentColor);
                    if (result == null) {
                        result = new LinkedList<>();
                        result.add(newBox);
                        BoxColorGroups.put(currentColor, result);
                    }
                    else {
                        result.add(newBox);
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
                    allGoals.add(goal);
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
        replaceBoxesWithoutAgentWithAWall();
    }
    private void replaceBoxesWithoutAgentWithAWall() {
        ListIterator<Box> iterator = allBoxes.listIterator();
        while(iterator.hasNext()) {
            Box current = iterator.next();
            if(AgentColorGroups.get(current.getColor()) == null) {
                if(boxesByID.get(current.getID()) != null) {
                    boxesByID.remove(current.getID());
                }
                if(BoxColorGroups.get(current.getColor()) != null) {
                    BoxColorGroups.remove(current.getColor());
                }
                iterator.remove();
                setElement( current.getX(), current.getY(), new Wall('+', current.getX(), current.getY()));
            }
        }
    }
    public static boolean isAgent (char id) { return ( '0' <= id && id <= '9' );}
    public static boolean isBox (char id) { return ( 'A' <= id && id <= 'Z' );}
    public static boolean isGoal (char id) { return ( 'a' <= id && id <= 'z' ); }
    public static boolean isWall (char id) {return (id == '+');}

    //returns object under given coordinates
    public BasicObject getElement(int x, int y) {
        if(yOutOfBounds(y) || xOutOfBounds(x)) throw new NullPointerException();
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
        else if(getElement(x, y) instanceof MovingObject) {
            Goal res = steppedOnGoals.get(getElement(x, y));
            if(res != null) {
                return true;
            }
            else {
                return false;
            }
        }
        return (isGoal(getElement(x, y).getID()));
    }
    public boolean isWall (int x, int y) {
        if(getElement(x, y) == null) return false;
        return (isWall(getElement(x, y).getID()));
    }
    public boolean isFree (int x, int y) {
        return getElement(x, y) == null || (getElement(x,y) instanceof Goal);
    }

    //Function assumes that passed object is at its' getX and getY location on the map
    public void changePositionOnMap(MovingObject obj, int x, int y) {
        if(!isFree(x,y) || obj == null) throw new UnsupportedOperationException();
        manageMovingThroughGoal(obj, x, y);
        if(getElement(obj.getX(), obj.getY()) == obj) {
            setElement(obj.getX(), obj.getY(), null);
        }
        setElement(x, y, obj);
    }
    public void revertPositionChange(MovingObject obj, int xFrom, int yFrom) {
        if(!isFree(obj.getX(),obj.getY())) throw new UnsupportedOperationException();
        manageMovingThroughGoal(obj, obj.getX(), obj.getY());
        if(getElement(xFrom, yFrom) == obj) {
            setElement(xFrom, yFrom, null);
        }
        setElement(obj.getX(), obj.getY(), obj);
    }
    private boolean yOutOfBounds(int y) { return (y >= (MainBoardYDomain) || y < 0);}
    private boolean xOutOfBounds(int x) {return (x >= (MainBoardXDomain) || x < 0);}
    private void manageMovingThroughGoal(MovingObject obj, int x, int y) {
        Goal steppedOnGoal = steppedOnGoals.get(obj);
        if(steppedOnGoal  != null) {
            setElement(steppedOnGoal.getX(), steppedOnGoal.getY(), steppedOnGoal);
            steppedOnGoals.remove(obj);
        }
        if(getElement(x,y) instanceof Goal) {
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
