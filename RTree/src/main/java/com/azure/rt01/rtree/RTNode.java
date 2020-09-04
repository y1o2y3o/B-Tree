package com.azure.rt01.rtree;

import com.azure.rt01.util.ByteUtil;

import java.util.Arrays;

public class RTNode {
    static int pageHeaderCapacity = 48;
    final int m; // m阶B树
    long nextFree; // 8B
    long pageIndex; // 8B
    int size; // 当前ptr的数量 4B
    long next; // 下一个叶子结点 8B
    long prev; // 上一个叶子结点 8B
    boolean isleaf; // 4B
    Box[] keys; // 关键字数组
    long[] ptrs; // 指向子树(数据)的指针数组
    boolean change = true; // 是否更改过
    boolean delete = false; // 删除标记

    RTNode(int m) {
        this.m = m;
        nextFree = -1L;
        pageIndex = 0L;
        keys = new Box[m + 1];
        ptrs = new long[m + 1];
        size = 0;
        prev = next = -1L;
        isleaf = true;
    }

    @Override
    public String toString() {

        return String.format("Node_%s: %d%s", hashCode(), size, Arrays.toString(keys));
    }

    static int getM(int pageSize) {
        return ((pageSize - RTNode.pageHeaderCapacity) / (Box.CAPACITY + 8));
    }

    public byte[] toPageBytes(int pageSize) {
        byte[] bytes = new byte[pageSize];
        int offset = 0;
        ByteUtil.long2Bytes(nextFree, bytes, offset);
        offset += 8;
        ByteUtil.long2Bytes(pageIndex, bytes, offset);
        offset += 8;
        ByteUtil.int2Bytes(size, bytes, offset);
        offset += 4;
        ByteUtil.long2Bytes(next, bytes, offset);
        offset += 8;
        ByteUtil.long2Bytes(prev, bytes, offset);
        offset += 8;
        ByteUtil.int2Bytes((isleaf ? 1 : 0), bytes, offset);
        offset += 4;
        for (int i = 0; i < size; ++i) {
            Box b = keys[i];
            ByteUtil.double2Bytes(b.p1.x, bytes, offset);
            offset += 8;
            ByteUtil.double2Bytes(b.p1.y, bytes, offset);
            offset += 8;
            ByteUtil.double2Bytes(b.p2.x, bytes, offset);
            offset += 8;
            ByteUtil.double2Bytes(b.p2.y, bytes, offset);
            offset += 8;
            ByteUtil.long2Bytes(ptrs[i], bytes, offset);
            offset += 8;
        }
        return bytes;
    }

    public static RTNode fromPageBytes(byte[] bytes) {
        RTNode rt = new RTNode(RTNode.getM(bytes.length));
        int offset = 0;
        rt.nextFree = ByteUtil.bytes2Long(bytes, offset);
        offset += 8;
        rt.pageIndex = ByteUtil.bytes2Long(bytes, offset);
        offset += 8;
        rt.size = ByteUtil.bytes2Int(bytes, offset);
        offset += 4;
        rt.next = ByteUtil.bytes2Long(bytes, offset);
        offset += 8;
        rt.prev = ByteUtil.bytes2Long(bytes, offset);
        offset += 8;
        rt.isleaf = (ByteUtil.bytes2Int(bytes, offset) != 0);
        offset += 4;
        for (int i = 0; i < rt.size; ++i) {
            rt.keys[i] = new Box(ByteUtil.bytes2Double(bytes, offset)
                    , ByteUtil.bytes2Double(bytes, offset + 8)
                    , ByteUtil.bytes2Double(bytes, offset + 16)
                    , ByteUtil.bytes2Double(bytes, offset + 24));
            offset += 32;
            rt.ptrs[i] = ByteUtil.bytes2Long(bytes, offset);
            offset += 8;
        }
        return rt;
    }

}
