package sampleclients;

import java.io.*;
import java.util.*;


public class Goal extends BasicObject {
    public Box assignedBox = null;
    public boolean boxAtGoalPosition = false;
    public List<Goal> deps = new ArrayList<Goal>();
    public Goal( char id, int y, int x ) {
        super( y, x,id, "Goal");
    }
    public boolean solved(){
        if(boxAtGoalPosition == true){

            BasicObject el = RandomWalkClient.gameBoard.getElement(this.getX(),this.getY());
            return Character.toLowerCase(el.getID())==this.getID();
        }
        return false;

    }
    public boolean canBeSolved(){
        for (Goal g : deps){
            if(!g.solved()){
                return false;
            }
        }
        return true;

    }

    /*public boolean atGoalPosition(Box c) {
        if( Integer.compare(getX(), c.getX()) == 0
                && Integer.compare(getY(), c.getY()) == 0 ) {
            boxAtGoalPosition = c;
            return true;
        }
        return false;
    }*/
}
