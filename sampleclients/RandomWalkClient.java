package sampleclients;

import java.io.*;
import java.util.*;



public class RandomWalkClient {

    public static enum dir {
        N, W, E, S
    };

    public static enum type {
        Move, Push, Pull
    };
	private static Random rand = new Random();

	public class Object {
        private String ObjectType;
        private int currentRow = 0, currentColumn = 0;
        public Object(int currentRow, int currentColumn, String... ObjectType ) {
            this.currentRow = currentRow;
            this.currentColumn = currentColumn;
            this.ObjectType = ObjectType.length == 1 ? ObjectType[0] : "Object";
//            System.err.println("Found " + color + " agent " + id + "at pos:" + currentColumn + ", " + currentRow);
        }
        public int getRow() { return currentRow;}
        public  void setRow( int row) { currentRow = row;}

        public int getColumn() { return currentColumn;}
        public void setColumn(int column) {currentColumn = column;}

        public String getObjectType() {return ObjectType;}
    }
    public class MovingObject extends Object {
        private String color;
        private char id;
        public MovingObject ( char id, String color, int currentRow, int currentColumn , String ObjectType) {
            super(currentRow, currentColumn, ObjectType);
            this.color = color;
            this.id = id;
        }
        public char getID() { return id;}
        public String getColor(){ return color;}

        public String move(dir Direction) throws UnsupportedOperationException {
            if(MainBoard[getRow()][getColumn()] != getID()) return "NoOp";

            try{
                switch (Direction) {
                    case N:
                        changePosition(getRow() - 1, getColumn());
                        break;

                    case S:
                        changePosition(getRow() + 1, getColumn());
                        break;

                    case E:
                        changePosition(getRow(), getColumn() + 1);
                        break;

                    case W:
                        changePosition(getRow(), getColumn() - 1);
                        break;

                }
            }
            catch(UnsupportedOperationException exc) {

                return "NoOp";
            }
            return type.Move + "(" + Direction + ")";
        }
        void changePosition(int row, int column) throws UnsupportedOperationException {
            if(rowOutOfBounds(row)
            || columnOutOfBounds(column)
            || !spaceEmpty(row,column)) throw new UnsupportedOperationException();
            MainBoard[getRow()][getColumn()] = ' ';
            setRow(row);
            setColumn(column);
            MainBoard[row][column] = getID();
        }
        boolean rowOutOfBounds(int row) { return (row > (numberOfRows - 1) || row < 0);}
        boolean columnOutOfBounds(int column) {return (column > (numberOfColumns - 1) || column < 0);}
        boolean spaceEmpty(int row, int column) {return MainBoard[row][column] == ' '; }
    }

	public class Agent extends MovingObject {
		public Agent( char id, String color, int currentRow, int currentColumn ) {
		    super(id, color, currentRow, currentColumn, "Agent");
//			System.err.println("Found " + getColor() + " agent " + getID() + " at pos: " + getColumn() + ", " + getRow());
		}
		public String act() {
//            String move = Command.every[rand.nextInt( Command.every.length )].toString();
//            System.err.println(move);
			return move(dir.N);
		}
	}
	public class Box extends MovingObject {
		public Box( char id, String color, int currentRow, int currentColumn ) {
            super(id, color, currentRow, currentColumn, "Box");
//            System.err.println("Found " + color + " box " + id + " at pos: " + currentColumn + ", " + currentRow );
        }
	}

	private BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
	private List< Agent > agents = new ArrayList< Agent >();
    private List< Box > boxes = new ArrayList< Box>();
    private int numberOfRows = 0, numberOfColumns = 0;
    private char[][] MainBoard; //every state change is seen on the main board

	public RandomWalkClient() throws IOException {
		readMap();
	}

	private void readMap() throws IOException {
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
		int currentColumn = 0;
        while ( !line.equals( "" ) ) {
            for ( int i = 0; i < line.length(); i++ ) {
                char id = line.charAt( i );
                if ( '0' <= id && id <= '9' ) {
                    agents.add( new Agent( id, colors.get( id ), numberOfRows , currentColumn) );
                }
                else if ( 'A' <= id && id <= 'Z' ) {
                    boxes.add( new Box( id, colors.get( id ), numberOfRows , currentColumn ) );
                }

                ++currentColumn;
            }
//            System.err.println(line); //prints a map
            if(numberOfColumns < currentColumn) {numberOfColumns = currentColumn; }
            table.add(line);
            line = in.readLine();
            currentColumn = 0;
            ++numberOfRows;
        }

        MainBoard = new char[numberOfRows][numberOfColumns];
        for( int row = 0; row < numberOfRows; ++row) {
            MainBoard[row] = table.get(row).toCharArray();
        }
        printBoard(MainBoard);
	}

    void printBoard(char board[][]) {
        System.err.println();
        for(int row = 0; row < numberOfRows; ++row) {
            System.err.println( new String(board[row]));
        }
        System.err.println();
    }

	public boolean update() throws IOException {
		String jointAction = "[";

		for ( int i = 0; i < agents.size() - 1; i++ )
			jointAction += agents.get( i ).act() + ",";
		
		jointAction += agents.get( agents.size() - 1 ).act() + "]";

		// Place message in buffer
		System.out.println( jointAction );
		System.err.println(jointAction);
		printBoard(MainBoard);
		// Flush buffer
		System.out.flush();

		// Disregard these for now, but read or the server stalls when its output buffer gets filled!
		String percepts = in.readLine();
		if ( percepts == null )
			return false;

		return true;
	}

	public static void main( String[] args ) {

		// Use stderr to print to console
		System.err.println( "Hello from RandomWalkClient. I am sending this using the error outputstream" );
		try {
			RandomWalkClient client = new RandomWalkClient();
			while ( client.update() )
				;

		} catch ( IOException e ) {
			// Got nowhere to write to probably
		}
	}
}
