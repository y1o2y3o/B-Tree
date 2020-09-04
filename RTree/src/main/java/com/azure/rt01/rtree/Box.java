package com.azure.rt01.rtree;

public class Box extends Rectangle{
    public static int CAPACITY = 32;

    public Box(double x1, double y1, double x2, double y2) {
        super(x1, y1, x2, y2);
    }
}
