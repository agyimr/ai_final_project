package sampleclients;

import java.io.*;
import java.util.*;


public class Goal extends BasicObject {
    public Box boxAtGoalPosition = null;
    public Goal( char id, int y, int x ) {
        super( y, x,id, "Goal");
    }

}
