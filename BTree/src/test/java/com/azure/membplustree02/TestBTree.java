package com.azure.membplustree02;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;


public class TestBTree {
    @Test
    public void test(){
        BTree<Integer> bt = new BTree<>(3);
        Integer[] arr =new Integer[100];
        for(int i = 0; i < arr.length; ++i){
            arr[i] = i;
        }


        for(int i = 0; i < arr.length; ++i){
            bt.insert(arr[i],"null");
        }
        shuffle(arr);

 //       System.out.println(Arrays.toString(arr));
        for(int i = 0; i < 100; ++i){
            bt.delete(arr[i]);
        }
        bt.print();
        bt.printKeysInorder();
    }

    public static void shuffle(Comparable[] nums) {
        Random rnd = new Random();
        for (int i = nums.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            //swap index i, j
            Comparable t = nums[i];
            nums[i] = nums[j];
            nums[j] = t;
        }
    }
}
