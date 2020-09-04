package com.azure.rt01.rtree;

public class Point {
    public double x,y;
    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("(%f,%f)", x, y);
    }
}
