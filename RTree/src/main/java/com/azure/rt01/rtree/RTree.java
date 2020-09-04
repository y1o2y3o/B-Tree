package com.azure.rt01.rtree;

import java.util.HashMap;

public class RTree {
    public static final long NIL = -1L;
    private HashMap<Long, RTNode> btMap = new HashMap<>();

    private final int m; // m阶B树
    private long root;
    private long sqt;
    private FileMapper mapper;

    public RTree(FileMapper mapper) {
        this.mapper = mapper;
        this.m = mapper.m;
        this.root = mapper.root;
        this.sqt = mapper.sqt;
    }

    /*---------------------------------------------------------⬇️私有方法⬇️-----------------------------------------------*/


    /**
     * 分支引用类,用来传递待插入的分支引用(作用相当于C++里的引用)
     */
    private static class RBranchRef {
        long rbranch = NIL;
        Rectangle tmbr, rmbr;
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
    private boolean insertAndAdjust(RTNode parent, int pindex, RTNode T, Rectangle key, long value, RBranchRef ref) {
        int i = T.getInsertPos(key);

        // 向下递归插入
        if (!T.isleaf) {
            boolean insertFlag = insertAndAdjust(T, i, getRTNode(T.ptrs[i]), key, value, ref);
            if (!insertFlag) return false; // 数据已经存在, 不需要插入
        } else {
            // 卫星数据已存在, 更新
            if (T.size > 0 && key.equals(T.keys[i])) {
                T.ptrs[i] = value;
                return false;
            }
        }

        // 向上调整
        Rectangle curKey = null;
        if (T.isleaf) {
            curKey = key;
            ref.rbranch = value;
        } else if (ref.rbranch != NIL) {
            curKey = ref.rmbr;
        }
        // 插入
        if (curKey != null) {
            if (T.size >= m)
                insertAndQSplit(T, curKey, ref);
            else
                insert(T, curKey, ref);
        }

        // 更新父亲key结点
        if (T.pageIndex == root) {
            // 根结点长高一层
            if (ref.rbranch != NIL) {
                RTNode rbt = createRTNode();
                rbt.isleaf = false;
                rbt.size = 2;
                rbt.keys[0] = ref.tmbr;
                rbt.keys[1] = ref.rmbr;
                rbt.ptrs[0] = T.pageIndex;
                rbt.ptrs[1] = ref.rbranch;
                root = rbt.pageIndex;
                mapper.setNewRoot(root); // 设置新的根结点索引
            }
        } else {
            // 更新父亲key结点
            parent.keys[pindex] = T.getMbr();
            parent.change = true;
        }

        return true;
    }

    // 简单插入
    private void insert(RTNode T, Rectangle curKey, RBranchRef ref) {
        T.addEntry(curKey, ref.rbranch);
        ref.rbranch = NIL;
        T.change = true;
    }

    // 插入并分裂(quadratic split)
    private void insertAndQSplit(RTNode T, Rectangle curKey, RBranchRef ref) {

        insert(T, curKey, ref);
        RTNode rt = createRTNode();
        // 1.选则种子
        Rectangle seed1, seed2;
        seed1 = seed2 = T.keys[0];
        double maxIncr = Double.MIN_VALUE;
        for (int i = 0; i < T.size; ++i) {
            for (int j = i + 1; j < T.size; ++j) {
                double curIncr = Rectangle.incrArea(T.keys[i], T.keys[j]);
                if (curIncr > maxIncr) {
                    maxIncr = curIncr;
                    seed1 = T.keys[i];
                    seed2 = T.keys[j];
                }
            }
        }

        // 2. 按照种子split
        T.size = 0;
        double incr1, incr2;
        for (int i = 0; i < m + 1; ++i) {
            incr1 = Rectangle.incrArea(T.keys[i], seed1);
            incr2 = Rectangle.incrArea(T.keys[i], seed2);
            if (incr1 < incr2) {
                T.addEntry(T.keys[i], T.ptrs[i]);
            } else {
                rt.addEntry(T.keys[i], T.ptrs[i]);
            }
        }
        rt.isleaf = T.isleaf;
        ref.rbranch = rt.pageIndex;
        ref.tmbr = T.getMbr();
        ref.rmbr = rt.getMbr();
    }

    // 根据pageIndex 获取 已经存在的 BTNode
    private RTNode getRTNode(long pageIndex) {
        if (btMap.containsKey(pageIndex)) {
            return btMap.get(pageIndex);
        } else {
            RTNode btNode = mapper.readPage(pageIndex);
            btNode.change = false;
            btMap.put(pageIndex, btNode);
            return btNode;
        }
    }

    // 创建一个新的BTNode（需要手动回收）
    private RTNode createRTNode() {
        RTNode newNode = new RTNode(m);
        newNode.pageIndex = mapper.allocatePage();
        btMap.put(newNode.pageIndex, newNode);
        return newNode;
    }
}
