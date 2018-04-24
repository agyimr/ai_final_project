package sampleclients;

import java.io.*;
import java.util.*;


public class Goal extends BasicObject {
    public Box assignedBox = null;
    public boolean boxAtGoalPosition = false;
    public Goal( char id, int y, int x ) {
        super( y, x,id, "Goal");
    }

}
