package com.azure.disk.bptree01;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

public class BTTest {
    @Test
    public void test(){
        FileMapper mapper = new FileMapper.FileMapperFactory()
                .setPageSize(4 * FileMapper._1KB)
                .load();
        BTree tree = new BTree(mapper);
        System.out.println(mapper.root);
        System.out.println(mapper.usedPages);


//        for(int i = 0; i < arr.length; ++i){
//            tree.insert(arr[i], 11);
//        }
       // tree.search(10);
        ArrayList<Long> longs = tree.searchRange(999900, 1010102);
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
