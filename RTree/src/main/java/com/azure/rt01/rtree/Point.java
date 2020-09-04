package com.azure.rt01.rtree;

public class Point {
    double x, y;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point other = (Point) obj;
            return Double.compare(x, other.x) == 0 && Double.compare(y, other.y) == 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("(%f,%f)", x, y);
    }
}
