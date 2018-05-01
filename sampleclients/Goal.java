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
        return !(Character.toLowerCase(el.getID())==this.getID());
    }
    public boolean canBeSolved(){
        System.err.println("--------------CAN BE SOLVED:--------------------");
        for (Goal g : deps){
            System.err.println("getID(): "+getID()+" !g.solved():"+!g.solved());
            if(!g.solved()){
                System.err.println("REACHED?");
                return false;
            }
        }
        System.err.println("-------------------------------------------2222222");
        return true;

    }

}
