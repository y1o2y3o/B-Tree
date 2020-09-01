package com.azure.membplustree02;

import com.azure.membplustree01.TreeNode;

@SuppressWarnings("unchecked")
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

    /**
     * 插入key 和 value 到树中
     *
     * @param key   关键字
     * @param value 卫星数据
     */
    public boolean insert(K key, Object value) {
        RBranchRef ref = new RBranchRef();
        return insertAndAdjust(null, -1, root, key, value, ref);
    }

    /**
     * 打印树结构
     */
    public void print() {
        print(root, 0);
    }

    /**
     * 顺序遍历keys
     */
    public void printKeysInorder() {
        BTNode p = sqt;
        K last = null;
        while (p != null) {
            for (int i = 0; i < p.size; ++i) {
                K k = (K) p.keys[i];
                System.out.print(k+" ");
                if(last != null && last.compareTo(k) > 0)
                    throw new Error("B树错误: 元素不是有序的！");
                last = k;
            }
            p = p.next;
        }
    }
    /*---------------------------------------------------------⬇️私有方法⬇️-----------------------------------------------*/

    static class RBranchRef {
        Object rbranch = null;
    }

    /**
     * 递归插入key, value 并调整
     *
     * @param parent 当前结点的父亲
     * @param pindex 当前结点在父亲中的索引下标
     * @param T      当前结点
     * @param key    待插入关键字
     * @param value  待插入的卫星数据
     * @param ref    待插入的关键字对应的分支
     */
    private boolean insertAndAdjust(BTNode parent, int pindex, BTNode T, K key, Object value, RBranchRef ref) {
        int i = lowerBound(T, key);
        if (i == T.size) --i;

        // 向下递归插入
        if (!T.isleaf) {
            boolean insertFlag = insertAndAdjust(T, i, (BTNode) T.ptrs[i], key, value, ref);
            if(!insertFlag) return false; // 数据已经存在, 不需要插入
        } else {
            // 卫星数据已存在, 更新
            if(T.size > 0 && key.compareTo((K)(T.keys[i])) == 0) {
                T.ptrs[i] = value;
                return false;
            }
        }

        // 向上调整
        K curKey = null;
        if (T.isleaf) {
            curKey = key;
            ref.rbranch = value;
        } else if (ref.rbranch != null) {
            curKey = (K) ((BTNode) (ref.rbranch)).getMaxKey();
        }
        // 插入
        if (curKey != null) {
            if (T.size >= m)
                insertAndRSplit(T, curKey, ref);
            else
                insert(T, curKey, ref);
        }

        // 更新父亲key结点
        if (T == root) {
            // 根结点长高一层
            if (ref.rbranch != null) {
                root = new BTNode(m);
                root.isleaf = false;
                root.size = 2;
                root.keys[0] = T.getMaxKey();
                root.keys[1] = ((BTNode) (ref.rbranch)).getMaxKey();
                root.ptrs[0] = T;
                root.ptrs[1] = ref.rbranch;
            }
        } else {
            // 更新父亲key结点
            parent.keys[pindex] = T.getMaxKey();
        }

        return true;
    }

    /**
     * 插入并向右分裂
     *
     * @param T      当前结点
     * @param curKey 插入的Key
     * @param ref    插入的分支
     */
    private void insertAndRSplit(BTNode T, K curKey, RBranchRef ref) {
        BTNode rb = new BTNode(m);
        insert(T, curKey, ref);
        int splitpos = (int) Math.ceil((double) m / 2.0);
        for (int i = splitpos, k = 0; i < T.size; ++i, ++k) { // 分裂
            rb.keys[k] = T.keys[i];
            rb.ptrs[k] = T.ptrs[i];
            T.keys[i] = T.ptrs[i] = null; // 剪掉分支
        }
        rb.size = T.size - splitpos;
        T.size = splitpos;
        rb.isleaf = T.isleaf;
        ref.rbranch = rb;

        // 更新叶结点之间的指针（双向链表）
        if (T.isleaf) {
            rb.prev = T;
            rb.next = T.next;
            if (T.next != null) T.next.prev = rb;
            T.next = rb;
        }
    }

    /**
     * 简单插入
     *
     * @param T      当前结点
     * @param curKey 插入的Key
     * @param ref    插入的分支
     */
    private void insert(BTNode T, K curKey, RBranchRef ref) {
        int i = lowerBound(T, curKey);
        for (int j = T.size - 1; j >= i; --j) {
            T.ptrs[j + 1] = T.ptrs[j];
            T.keys[j + 1] = T.keys[j];
        }
        T.ptrs[i] = ref.rbranch;
        T.keys[i] = curKey;
        T.size++;
        ref.rbranch = null;
    }

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

    /**
     * 递归打印树结构
     *
     * @param T     当前树
     * @param level 第几层
     */
    private void print(BTNode T, int level) {
        if (T != null) {
            for (int i = 0; i < level; ++i) {
                System.out.print("\t");
            }
            System.out.printf("%s\n", T);
            if (!T.isleaf) {
                for (int i = 0; i < T.size; ++i) {
                    BTNode t = (BTNode) T.ptrs[i];
                    print(t, level + 1);
                }
            }
        }
    }
}
