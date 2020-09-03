package com.azure.disk.bptree01;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

public class BTTest {
    @Test
    public void test(){
        FileMapper mapper = new FileMapper.FileMapperFactory()
                .setPageSize(4 * FileMapper._1KB)
                .create();
        BTree tree = new BTree(mapper);
        System.out.println(mapper.root);
        System.out.println(mapper.usedPages);

        long[] arr = new long[100000];
        for(int i = 0; i < arr.length; ++i){
            arr[i] = i;
            tree.insert(arr[i], i);
        }
        for(int i = 99; i < arr.length; ++i){
            tree.delete(arr[i]);
        }
        tree.flush();
       // tree.search(10);
        ArrayList<Long> longs = tree.searchRange(0, 1010102);
       System.out.println(longs);
       // tree.printKeysInorder();
    }

    public static void shuffle(long[] nums) {
        Random rnd = new Random();
        for (int i = nums.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            //swap index i, j
            long t = nums[i];
            nums[i] = nums[j];
            nums[j] = t;
        }
    }
}
