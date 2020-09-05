package com.azure.rt01.rtree;

import com.azure.rt01.util.ByteUtil;

import java.util.Arrays;

public class RTNode {
    public static int pageHeaderCapacity = 44;
    final int m; // m阶B树
    long nextFree; // 8B
    long pageIndex; // 8B
    int size; // 当前ptr的数量 4B
    long next; // 下一个叶子结点 8B
    long prev; // 上一个叶子结点 8B
    boolean isleaf; // 4B
    int height; // 4B
    Rectangle[] keys; // 关键字数组
    long[] ptrs; // 指向子树(数据)的指针数组
    boolean change = true; // 是否更改过
    boolean delete = false; // 删除标记

    RTNode(int m) {
        this.m = m;
        nextFree = -1L;
        pageIndex = 0L;
        keys = new Rectangle[m + 1];
        ptrs = new long[m + 1];
        size = 0;
        prev = next = -1L;
        isleaf = true;
        height = 0;
    }

    public void print() {
        System.out.println("Print rtnode " + pageIndex);
        System.out.println("\tm: " + m);
        System.out.println("\tnextFree: " + nextFree);
        System.out.println("\tpageIndex: " + pageIndex);
        System.out.println("\tsize: " + size);
        System.out.println("\tnext: " + next);
        System.out.println("\tprev: " + prev);
        System.out.println("\tisleaf: " + isleaf);
        for (int i = 0; i < size; ++i) {
            System.out.print("[" + keys[i].toString() + "," + ptrs[i] + "] ");
        }
        System.out.println();
    }

    @Override
    public String toString() {
        return String.format("Node_%s: %d|%d%s", hashCode(), size, height, Arrays.toString(keys));
    }

    static int getM(int pageSize) {
        return ((pageSize - RTNode.pageHeaderCapacity) / (Rectangle.CAPACITY + 8));
    }

    // 写page
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
        ByteUtil.int2Bytes(height, bytes, offset);
        offset += 4;
        for (int i = 0; i < size; ++i) {
            Rectangle b = keys[i];
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

    // 读page
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
        rt.height = ByteUtil.bytes2Int(bytes, offset);
        offset += 4;
        for (int i = 0; i < rt.size; ++i) {
            rt.keys[i] = new Rectangle(ByteUtil.bytes2Double(bytes, offset)
                    , ByteUtil.bytes2Double(bytes, offset + 8)
                    , ByteUtil.bytes2Double(bytes, offset + 16)
                    , ByteUtil.bytes2Double(bytes, offset + 24));
            offset += 32;
            rt.ptrs[i] = ByteUtil.bytes2Long(bytes, offset);
            offset += 8;
        }
        return rt;
    }

    // 在末尾增加一个entry
    void addEntry(Rectangle key, long value) {
        keys[size] = key;
        ptrs[size] = value;
        size++;
    }

    // 寻找插入点
    int getInsertPos(Rectangle key) {
        double minIncr = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < size; ++i) {
            double curIncr = Rectangle.incrArea(keys[i], key);
            if (curIncr < minIncr) {
                minIncr = curIncr;
                index = i;
            }
        }
        return index;
    }

    // 计算此结点mbr
    Rectangle getMbr() {
        double xmax, ymax, xmin, ymin;
        xmax = ymax = Double.MIN_VALUE;
        xmin = ymin = Double.MAX_VALUE;
        for (int i = 0; i < size; ++i) {
            Rectangle r = keys[i];
            xmax = Double.max(xmax, r.p2.x);
            ymax = Double.max(ymax, r.p2.y);
            xmin = Double.min(xmin, r.p1.x);
            ymin = Double.min(ymin, r.p1.y);
        }
        return new Rectangle(xmin, ymin, xmax, ymax);
    }

    // 查找key的位置
    int indexOfKey(Rectangle key) {
        for (int i = 0; i < size; ++i) {
            if (key.equals(keys[i])) return i;
        }
        return -1;
    }

    // 删除index位置的entry
    void delete(int index) {
        if(index < size && index >= 0){
            for (int i = index; i < size - 1; ++i) {
                keys[i] = keys[i + 1];
                ptrs[i] = ptrs[i + 1];
            }
            size--;
        }
    }
}
