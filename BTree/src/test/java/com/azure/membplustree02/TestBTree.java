package com.azure.membplustree02;

import org.junit.Test;

import java.util.Random;


public class TestBTree {
    @Test
    public void test() {
        BTree<String> bt = new BTree<>(30);
        String[] arr = new String[100];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = i + "";
        }


        for (String s : arr) {
            bt.insert(s, "null");
        }
        shuffle(arr);

        //       System.out.println(Arrays.toString(arr));
        for (String s : arr) {
            bt.delete(s+"");
        }
        bt.print();
        bt.printKeysInorder();
        for (String s : arr) {
            bt.insert(s, "null");
        }
        shuffle(arr);

        //       System.out.println(Arrays.toString(arr));
        for (String s : arr) {
            bt.delete(s+"");
        }
        bt.print();
        bt.printKeysInorder();
    }

    public static void shuffle(Comparable<?>[] nums) {
        Random rnd = new Random();
        for (int i = nums.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            //swap index i, j
            Comparable<?> t = nums[i];
            nums[i] = nums[j];
            nums[j] = t;
        }
    }
}
