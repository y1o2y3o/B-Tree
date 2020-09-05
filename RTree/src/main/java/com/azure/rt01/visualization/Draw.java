package com.azure.rt01.visualization;

import com.azure.rt01.rtree.Rectangle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;

public class Draw {
    static BufferedImage image;
    static Graphics2D graphics;
    static Color[] colors = {Color.BLACK, Color.RED,Color.BLUE, Color.CYAN, Color.MAGENTA};
    static String outputFile = "rect.png";

    public static void setOutput(String filename) {
        outputFile = filename;
    }

    public static void init() {
        //创建图片对象
        image = new BufferedImage(10000, 10000, BufferedImage.TYPE_4BYTE_ABGR);
        //基于图片对象打开绘图
        graphics = image.createGraphics();

    }

    public static void drawRect(Rectangle r, int level) {
        graphics.setColor(colors[(level -1 +colors.length)% colors.length]);
        Rectangle2D.Double rect = new Rectangle2D.Double(r.p1.x, r.p1.y, r.getWidth(), r.getHeight());
        graphics.draw(rect);
    }

    public static void done() {
        //处理绘图
        graphics.dispose();
        //将绘制好的图片写入到图片
        try {
            ImageIO.write(image, "png", new File("output/" + outputFile));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {


    }
}
