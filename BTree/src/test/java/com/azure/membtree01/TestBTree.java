package com.azure.membtree01;

import org.junit.Test;


public class TestBTree {
    @Test
    public void test(){
        BTree<Integer> bt = new BTree<>(30);
        for(int i = 0; i < 245; ++i){
            bt.insert(i);
        }
        for(int i = 30; i < 66; ++i){
            bt.delete(i);
        }
        bt.printKeysInorder();
    }
}
