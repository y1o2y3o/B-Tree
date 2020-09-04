package com.azure.rt01.rtree;

public class Rectangle {
    public Point p1, p2;

    public Rectangle(double x1, double y1, double x2, double y2) {
        this.p1 = new Point(x1, y1);
        this.p2 = new Point(x2, y2);
    }

    @Override
    public String toString() {
        return p1.toString() + "," + p2.toString();
    }
}
