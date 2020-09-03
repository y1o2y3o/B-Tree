package com.azure.disk.bptree01;

import java.util.Arrays;

public class BTNode {
    static int pageHeaderCapacity = 48;
    final int m; // m阶B树
    long nextFree; // 8B
    long pageIndex; // 8B
    int size; // 当前ptr的数量 4B
    long next; // 下一个叶子结点 8B
    long prev; // 上一个叶子结点 8B
    boolean isleaf; // 4B
    // 8B冗余
    long[] keys; // 关键字数组
    long[] ptrs; // 指向子树(数据)的指针数组
    boolean change = true; // 是否更改过
    boolean delete = false; // 删除标记

    BTNode(int m) {
        this.m = m;
        nextFree = -1L;
        pageIndex = 0L;
        keys = new long[m + 1];
        ptrs = new long[m + 1];
        size = 0;
        prev = next = FileMapper.NIL;
        isleaf = true;
    }

    @Override
    public String toString() {

        return String.format("Node_%s: %d%s", hashCode(), size, Arrays.toString(keys));
    }

    long getMaxKey() {
        return keys[size - 1];
    }

    void setMaxKey(Long key) {
        keys[size - 1] = key;
    }



    static int getM(int pageSize) {
        return ((pageSize - BTNode.pageHeaderCapacity) >> 4);
    }

}
