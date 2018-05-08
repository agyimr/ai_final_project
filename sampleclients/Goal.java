package sampleclients;

import java.io.*;
import java.util.*;


public class Goal extends BasicObject {
    public Box assignedBox = null;
    public List<Goal> deps = new ArrayList<Goal>();
    public Goal( char id, int y, int x ) {
        super( y, x,id, "Goal");
    }
    public boolean solved(){
        BasicObject el = RandomWalkClient.gameBoard.getElement(this.getX(),this.getY());
        if(el instanceof Box) {
            return (Character.toLowerCase(el.getID())==this.getID());
        }
        else {
            return false;
        }
    }
    public boolean canBeSolved(){
        for (Goal g : deps){
            if(!g.solved()){
                return false;
            }
        }
        return true;

    }

}
