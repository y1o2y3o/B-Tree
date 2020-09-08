package com.azure.splay01;

public class SplayNode {
    Object key;
    SplayNode left;
    SplayNode right;

    public SplayNode(Object key) {
        this.key = key;
        left = right = null;
    }
}
