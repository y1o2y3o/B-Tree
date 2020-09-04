package com.azure.rt01.util;

public class MathUtil {
    public static double max(double... items) {
        double maxItem = Double.MIN_VALUE;
        for (double d : items) {
            if (d > maxItem) maxItem = d;
        }
        return maxItem;
    }

    public static double min(double... items) {
        double minItem = Double.MAX_VALUE;
        for (double d : items) {
            if (d < minItem) minItem = d;
        }
        return minItem;
    }
}
