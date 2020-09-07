package com.azure.rbtree01;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class RBTest {
    @Test
    public void testInsert() throws Exception {
        RBTree tree = new RBTree();
        Integer[] arr = new Integer[10000];
        for (int i = 0; i < arr.length; ++i) arr[i] = i;
        shuffle(arr, arr.length);
        long start = System.nanoTime();
        for (Integer integer : arr) tree.insert(integer);
        long end = System.nanoTime();
        System.out.println("插入用时:" + ((end - start)/1000000)+"ms");

        long last = Long.MIN_VALUE;
        List<Long> keys = tree.keysInorder();
        for (long num : keys) {
            if (num <= last) throw new Exception();
            last = num;
        }
        System.out.println(keys);
        //tree.printTree();
        //System.out.println(tree.validate());
    }

    @Test
    public void testTreeSet() {
        TreeSet<Integer> tree = new TreeSet<>();

        Integer[] arr = new Integer[10000000];
        for (int i = 0; i < arr.length; ++i) arr[i] = i;
        shuffle(arr, arr.length);
        long start = System.nanoTime();
        Collections.addAll(tree, arr);
        long end = System.nanoTime();
        System.out.println("插入用时:" + ((end - start)/1000000)+"ms");
        //tree.printTree();
    }

    private void shuffle(Object[] arr, int size) {
        Random rand = new Random();
        for (int i = 0; i < size; ++i) {
            int j = rand.nextInt(i + 1);
            Object t = arr[i];
            arr[i] = arr[j];
            arr[j] = t;
        }
    }
}
