package com.azure.membplustree01;

import org.junit.Test;

import java.util.Arrays;

public class TreeNode {
    final int m; // m阶B树
    int size; // 当前ptr的数量
    TreeNode parent; // 父结点
    TreeNode next; // 下一个叶子结点
    TreeNode prev; // 上一个叶子结点
    final Object[] keys; // 关键字数组
    final Object[] ptrs; // 指向子树(数据)的指针数组
    boolean isleaf;

    TreeNode(int m) {
        this.m = m;
        keys = new Object[m + 1];
        ptrs = new Object[m + 1];
        size = 0;
        prev = next = parent = null;
        isleaf = true;
    }

    boolean isLeaf() {
        return isleaf;
    }

    @Override
    public String toString() {

        return String.format("Node_%s: %d%s", hashCode(), size, Arrays.toString(keys));
    }

    Object getMaxKey(){
        return keys[size - 1];
    }
    void setMaxKey(Object key){
        keys[size - 1] = key;
    }
}
