package com.azure.rt01.convert;

import java.io.*;

public class ConvertFile {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/zksfromusa/SpringBoot Projects/advance-trees/RTree/src/main/resources/data_00001")));
        RandomAccessFile out = new RandomAccessFile("data","rw");
        String line;
        while((line = reader.readLine()) != null){
            String[] split = line.split(",");
            out.writeInt(2);
            for(String s: split){
                double d = Double.parseDouble(s);
                out.writeDouble(d);
            }
        }
        out.close();
        reader.close();
    }
}
