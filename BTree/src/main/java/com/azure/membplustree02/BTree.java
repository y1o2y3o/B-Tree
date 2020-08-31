package com.azure.membplustree02;

public class BTree<K extends Comparable<K>> {
    private int m; // m阶B树
    private BTNode root;
    private BTNode sqt;

    public BTree(int m) {
        if (m < 3) throw new ExceptionInInitializerError("m is too small: at least 3");
        this.m = m;
        root = new BTNode(m);
        sqt = root;
    }



    /*---------------------------------------------------------⬇️私有方法⬇️-----------------------------------------------*/

    /**
     * 在B树上查找关键字key,返回 Result(pt, index, tag)。若查找成功，则特征值tag 为 true, pt.keys[index] 等于 key；
     * 否则tag = false, 等于key的关键字应该插入在 pt.keys[index-1] 和 pt.keys[index]之间。
     *
     * @param key 关键字
     * @return 结果
     */
    private Result searchKey(K key) {
        BTNode p = root;
        int index = 0;
        while (!p.isleaf) {
            index = lowerBound(p, key);
            if (index >= p.size)
                index = p.size - 1;
            p = (BTNode) p.ptrs[index];
        }
        index = lowerBound(p, key);
        if (p.size > 0 && key.compareTo((K) p.keys[index]) == 0) {
            return new Result(p, index, true);
        } else {
            return new Result(p, index, false);
        }
    }

    /**
     * 返回第一个>=key的位置,如果找不到则返回最后一个元素的位置
     *
     * @param T   结点
     * @param key 关键字
     * @return 位置
     */
    private int lowerBound(BTNode T, K key) {
        int low = 0;
        int high = T.size;
        while (low < high) {
            int mid = (low + high) / 2;
            K k = (K) T.keys[mid];
            int cmp = key.compareTo(k);
            if (cmp <= 0) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }
        return low;
    }
}
