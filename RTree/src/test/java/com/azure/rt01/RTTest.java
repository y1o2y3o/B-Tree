package com.azure.rt01;

import com.azure.rt01.rtree.*;
import com.azure.rt01.rtree.Rectangle;
import com.azure.rt01.visualization.Draw;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class RTTest {
    @Test
    public void test() throws IOException {
        FileMapper mapper = new FileMapper.FileMapperFactory()
                //.setPageSize(RTNode.pageHeaderCapacity + 40 * 3)
                .create();
        RTree rTree = new RTree(mapper);


        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/zksfromusa/SpringBoot Projects/B-Tree/RTree/src/test/resources/data_00001")));
        String line;
        int end = 1000000;
        int ans = 0;
        while (ans < end && (line = reader.readLine()) != null) {
            String[] split = line.split(",");
            double[] darr = new double[4];
            int i = 0;
            for (String s : split) {
                double d = Double.parseDouble(s);
                darr[i++] = d;
            }
            Rectangle rectangle = new Rectangle(darr[0], darr[1], darr[2], darr[3]);
            rTree.insert(rectangle, 0L);
            ans++;
        }
        rTree.printTree();
        rTree.flush();
        reader.close();

    }

    @Test
    public void test2() {
        FileMapper mapper = new FileMapper.FileMapperFactory()
                .load();
        RTree rTree = new RTree(mapper);
        rTree.printTree();
        rTree.draw();
    }

    @Test
    public void test3() throws IOException {
        FileMapper mapper = new FileMapper.FileMapperFactory()
                .load();
        RTree rTree = new RTree(mapper);
        List<SearchResult> searchResults = rTree.searchRange(new Rectangle(0, 0, 5000, 5000));
        System.out.println(searchResults.size());
        System.out.println(searchResults);
        for (SearchResult searchResult : searchResults) System.out.println(rTree.delete(searchResult.key));
        rTree.flush();
    }

    @Test
    public void test4() throws Exception {
        Draw.init();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("input/data_00001"))));
        String line;
        int end = 100000;
        int ans = 0;
        while (ans < end && (line = reader.readLine()) != null) {
            String[] split = line.split(",");
            double[] darr = new double[4];
            int i = 0;
            for (String s : split) {
                double d = Double.parseDouble(s);
                darr[i++] = d;
            }
            Rectangle rectangle = new Rectangle(darr[0], darr[1], darr[2], darr[3]);
            Draw.drawRect(rectangle, 0);
            ans++;
        }
        reader.close();
        Draw.done();
    }


}
