package com.azure.membtree01;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TreeNode {
    final int m; // m阶B树
    int size; // 当前ptr的数量
    final Object[] keys; // 关键字数组
    final TreeNode[] ptrs; // 指向子树的指针数组
    TreeNode parent; // 父结点

    TreeNode(int m) {
        this.m = m;
        keys = new Object[m + 1];
        ptrs = new TreeNode[m + 1];
        size = 1;
        parent = null;
    }

    boolean isLeaf() {
        return ptrs[0] == null;
    }



    @Override
    public String toString() {

        return String.format("Node_%s: %d%s", hashCode(), size, Arrays.toString(keys));
    }
}
