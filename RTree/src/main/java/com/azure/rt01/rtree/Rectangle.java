package com.azure.rt01.rtree;

import com.azure.rt01.util.MathUtil;

public class Rectangle {
    static final int CAPACITY = 32;

    // p1 左下, p2 右上
    public Point p1, p2;

    // 注意: x1 < x2, y1 < y2
    public Rectangle(double x1, double y1, double x2, double y2) {
        if(x1 > x2 || y1 > y2) throw new Error("矩形不合法！");
        this.p1 = new Point(x1, y1);
        this.p2 = new Point(x2, y2);
    }

    // 宽(x轴)
    public double getWidth() {
        return p2.x - p1.x;
    }

    // 高(y轴)
    public double getHeight() {
        return p2.y - p1.y;
    }

    // 判断两个rectangle是否相交
    boolean overlap(Rectangle rectangle) {
        double xmax = MathUtil.max(p2.x, rectangle.p2.x);
        double ymax = MathUtil.max(p2.y, rectangle.p2.y);
        double xmin = MathUtil.min(p1.x, rectangle.p1.x);
        double ymin = MathUtil.min(p1.y, rectangle.p1.y);
        return ((xmax - xmin) > (getWidth() + rectangle.getWidth()))
                && ((ymax - ymin) > getHeight() + rectangle.getHeight());
    }

    // 计算矩形面积
    double area() {
        return getHeight() * getWidth();
    }

    // 返回mbr
    static Rectangle mbr(Rectangle... items) {
        double xmax, ymax, xmin, ymin;
        xmax = ymax = Double.MIN_VALUE;
        xmin = ymin = Double.MAX_VALUE;
        for (Rectangle r : items) {
            xmax = Double.max(xmax, r.p2.x);
            ymax = Double.max(ymax, r.p2.y);
            xmin = Double.min(xmin, r.p1.x);
            ymin = Double.min(ymin, r.p1.y);
        }
        return new Rectangle(xmin, ymin, xmax, ymax);
    }

    // 两个矩形合成mbr的增量
    static double incrArea(Rectangle r1, Rectangle r2){
        return mbr(r1, r2).area() - r1.area() - r2.area();
    }


    @Override
    public String toString() {
        return "<"+p1.toString() + "," + p2.toString()+">";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rectangle) {
            Rectangle other = (Rectangle) obj;
            return p1.equals(other.p1) && p2.equals(other.p2);
        }
        return false;
    }

    public static void main(String[] args) {
        Rectangle r1 = new Rectangle(0, 0, 2, 2);
        Rectangle r2 = new Rectangle(1, 1, 3, 3);
        System.out.println(incrArea(r1, r2));
    }
}
