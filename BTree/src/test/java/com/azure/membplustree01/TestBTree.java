package com.azure.membplustree01;

import org.junit.Test;

import java.util.HashMap;
import java.util.Random;


public class TestBTree {
    @Test
    public void test(){
        BPlusTree<String> bt = new BPlusTree<>(300);
        String[] arr = new String[10000];
        for(int i = 0; i < arr.length; ++i){
            arr[i] = ""+i;
        }
        shuffle(arr);
        for(int i = 0; i < arr.length; ++i){
            bt.insert(arr[i],"");
        }

//        for(int i = 0; i < arr.length; ++i){
//            System.out.println(bt.get(arr[i]) == arr[i]);
//        }
        //bt.print();
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
