package sampleclients;

import java.io.*;
import java.util.*;
import java.awt.Point;
import sampleclients.Command.dir;
import sampleclients.Command.type;


public class ConflictNode {
	private final Point pos;
	private final Point boxPos;
	private final List<Point> posLst;
	private ConflictNode parent = null;
	private Command c = null;
	
    public ConflictNode(int x, int y) {
        this.pos = new Point(x,y);
        boxPos = null;
        posLst = new ArrayList<Point>();
        posLst.add(pos);
    }
    public ConflictNode(Point pos) {
        this.pos = pos;
        boxPos = null;
        posLst = new ArrayList<Point>();
        posLst.add(pos);
    }
    public ConflictNode(Point pos, Point box) {
        this.pos = pos;
        this.boxPos = box;
        posLst = new ArrayList<Point>();
        posLst.add(pos);
        posLst.add(boxPos);
    }
    public ConflictNode(List<Point> pos) {
        this.pos = pos.get(0);
        if(pos.size() == 2){
        	this.boxPos = pos.get(1);
        }else{
        	this.boxPos = null;
        }
        
        posLst = new ArrayList<Point>(pos);
    }
    
    public void setParent(ConflictNode p) {
    	parent = p;
    }
    public ConflictNode getParent() {
    	return parent;
    }
    
    public void setCommand(Command c) {
    	this.c = c;
    }
    public Command getCommand() {
    	return c;
    }
    
    public int getX() { return pos.x;}
    public int getY() {return pos.y;}
    public int getBoxX() { return boxPos.x;}
    public int getBoxY() {return boxPos.y;}
    public List<Point> getPoints() {return posLst;}
    public boolean hasBox(){return posLst.size() == 2;}

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        ConflictNode other = (ConflictNode) obj;
        if (pos.x != other.pos.x)
            return false;
        if (pos.y != other.pos.y)
            return false;
        if (!posLst.equals(other.posLst))
            return false;
        return true;
    }

    @Override
    public String toString() {
    	if(boxPos == null){
    		return "(" + pos.x + ", " + pos.y + ")";
    	}else{
    		return "(" + pos.x + ", " + pos.y + ")" + "(" + boxPos.x + ", " + boxPos.y + ")";
    	}
        
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17;
        result = prime * result;
        result = prime * result + pos.x;
        result = prime * result + pos.y;
        return result;
    }
}