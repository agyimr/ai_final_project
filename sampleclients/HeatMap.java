package sampleclients;

import java.awt.*;
import java.util.*;

public class HeatMap {

    private double[][] heatMap;
    static private int width;
    private int height;

    public HeatMap(MainBoard mainBoard) {

        this.width = mainBoard.getWidth();
        this.height = mainBoard.getHeight();

        this.heatMap = new double[height][width];

    }

    public void initialize(boolean reset) {

        for(int j = 0; j < this.height; j++) {
            for(int i = 0; i < this.width; i++) {

                try {

                    if(RandomWalkClient.gameBoard.getElement(i, j) instanceof Wall) {
                        this.heatMap[j][i] = -1000;
                    }
                    else if(RandomWalkClient.gameBoard.isBox(i, j)) {
                        this.heatMap[j][i] = 750;
                    }
                    else if(RandomWalkClient.gameBoard.isAgent(i, j)) {
                        this.heatMap[j][i] = 1000;
                    }
                    else if(RandomWalkClient.gameBoard.isGoal(i, j)) {
                        this.heatMap[j][i] = 1000;
                    } else if(reset) {
                        this.heatMap[j][i] = 0;
                    }

                } catch (Exception e) {

                    this.heatMap[j][i] = 0;

                }

            }

        }
    }

    public double getHeat(int i, int j) {
        return this.heatMap[j][i];
    }

    public void iterate(int n) {
        for(int i = 0; i < n; i++) {
            iterate();
        }
    }

    public void iterate() {

        initialize(false);

        double[][] newHeatMap = new double[this.height][this.width];

        for(int j = 1; j < this.height-1; j++) {
            newHeatMap[j][0] = (this.heatMap[j-1][0] + this.heatMap[j+1][0] + this.heatMap[j][1]) / 3
                    + (this.heatMap[j-1][1] + this.heatMap[j+1][1]) / 4;

            newHeatMap[j][this.width-1] = (this.heatMap[j-1][this.width-1] + this.heatMap[j+1][this.width-1] + this.heatMap[j][this.width-2]) / 3
                    + (this.heatMap[j-1][this.width-2] + this.heatMap[j+1][this.width-2]) / 4;
        }

        for(int i = 1; i < this.width-1; i++) {
            newHeatMap[0][i] = (this.heatMap[0][i-1] + this.heatMap[0][i+1] + this.heatMap[1][i]) / 3
                    + (this.heatMap[1][i-1] + this.heatMap[1][i+1]) / 4;

            newHeatMap[this.height-1][i] = (this.heatMap[this.height-1][i-1] + this.heatMap[this.height-1][i+1] + this.heatMap[this.height-2][i]) / 3
                    + (this.heatMap[this.height-2][i-1] + this.heatMap[this.height-2][i+1]) / 4;
        }

        newHeatMap[0][0] = (newHeatMap[0][1] + newHeatMap[1][0]) /2;
        newHeatMap[0][this.width-1] = (newHeatMap[0][this.width-2] + newHeatMap[1][this.width-1]) /2;
        newHeatMap[this.height-1][this.width-1] = (newHeatMap[this.height-1][this.width-2] + newHeatMap[this.height-2][this.width-1]) /2;
        newHeatMap[this.height-1][0] = (newHeatMap[this.height-1][1] + newHeatMap[this.height-2][0]) /2;

        for(int j = 1; j < this.height-1; j++) {
            for (int i = 1; i < this.width - 1; i++) {

                newHeatMap[j][i] = (this.heatMap[j-1][i] + this.heatMap[j+1][i] + this.heatMap[j][i-1] + this.heatMap[j][i+1]) / 4
                        + (this.heatMap[j-1][i-1] + this.heatMap[j-1][i+1] + this.heatMap[j+1][i-1] + this.heatMap[j+1][i+1]) / 8;

            }
        }

        this.heatMap = newHeatMap;



    }

    @Override
    public String toString() {

        String out = "";

        for(int j = 0; j < this.height; j++) {
            for(int i = 0; i < this.width; i++) {

                try {

                    if(RandomWalkClient.gameBoard.isWall(i, j)) {
                        out += "#\t";
                    } else {
                        out += Math.round(this.heatMap[j][i]) + "\t";
                    }
                } catch(Exception e) {

                    out += "?\t";

                }

            }
            out += "\n";
        }

        return out;
    }
}
