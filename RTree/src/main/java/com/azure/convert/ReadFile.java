package com.azure.convert;

import com.azure.rtree.Rectangle;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ReadFile {
    public static void main(String[] args) throws IOException {
        RandomAccessFile in = new RandomAccessFile("data", "r");

        while (in.getFilePointer() < in.length()) {
            System.out.println(in.getFilePointer());
            in.readInt();
            Rectangle rectangle = new Rectangle(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
            //System.out.println(rectangle);
        }
        in.close();
    }
}
