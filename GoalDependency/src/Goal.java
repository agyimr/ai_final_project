public class Goal extends BasicObject {
    public Box boxAtGoalPosition = null;
    public Goal( char id, int y, int x ) {
        super( y, x,id, "Goal");
    }
    public boolean atGoalPosition(Box c) {
        if( Integer.compare(getX(), c.getX()) == 0
                && Integer.compare(getY(), c.getY()) == 0 ) {
            boxAtGoalPosition = c;
            return true;
        }
        return false;
    }
}
