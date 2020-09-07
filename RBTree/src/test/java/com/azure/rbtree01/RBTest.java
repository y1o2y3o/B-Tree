package com.azure.rbtree01;

import org.junit.Test;

import java.util.*;

public class RBTest {
    @Test
    public void testDelete() throws Exception {
        RBTree tree = new RBTree();
        Integer[] arr = new Integer[10000000];//{52, 78, 4, 10, 94, 98, 95, 74, 96, 39, 44, 58, 28, 70, 41, 75, 21, 60, 63, 6, 32, 64, 57, 65, 99, 40, 91, 90, 8, 0, 30, 86, 43, 12, 37, 36, 11, 67, 53, 66, 56, 97, 55, 42, 22, 62, 49, 38, 33, 50, 82, 29, 47, 7, 89, 35, 79, 5, 25, 77, 92, 15, 61, 68, 84, 1, 72, 14, 73, 88, 71, 80, 19, 3, 59, 13, 27, 26, 17, 83, 23, 24, 81, 34, 48, 85, 93, 2, 31, 16, 51, 87, 20, 9, 69, 46, 54, 45, 76, 18};//new Integer[100];//{2, 0, 15, 6, 7, 8, 29, 20, 22, 16, 26, 14, 23, 19, 24, 9, 25, 4, 13, 18, 21, 1, 27, 10, 5, 28, 11, 3, 17, 12};
        for (int i = 0; i < arr.length; ++i) arr[i] = i;
        //shuffle(arr, arr.length);
        //System.out.println(Arrays.toString(arr));
        long start = System.nanoTime();
        for (Integer integer : arr) tree.insert(integer);
        long end = System.nanoTime();
        System.out.println("插入用时:" + ((end - start)/1000000)+"ms");
        //tree.printTree();

        shuffle(arr, arr.length);
        long start2 = System.nanoTime();
        for (int i = 0; i < arr.length / 2; ++i){
            tree.delete(arr[i]);
        //    System.out.println(tree.validate());
        }


        //tree.delete(arr[j]);
        //tree.delete(17);
        long end2 = System.nanoTime();
        System.out.println("删除用时:" + ((end2 - start2)/1000000)+"ms");
        System.out.println(tree.validate());
        long last = Long.MIN_VALUE;
        List<Long> keys = tree.keysInorder();
        for (long num : keys) {
            if (num <= last) throw new Exception();
            last = num;
        }
//        System.out.println(keys);
        //tree.printTree();
        System.out.println(tree.keysInorder().size());
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
