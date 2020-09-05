package com.azure.rt01.rtree;

import com.azure.rt01.visualization.Draw;
import sun.security.krb5.internal.PAData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

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

    // 删除
    public boolean delete(Rectangle key) {
        Stack<PathNode> path = new Stack<>();
        if (!searchKey(getRTNode(root), key, path)) return false;
        // 删除entry
        RTNode leaf = path.peek().T;
        int index = path.peek().index;
        leaf.delete(index);
        leaf.change = true;
        // 向上调整
        List<Entry> Q = new ArrayList<>();
        adjust(path, Q);
        // reinsert
        for (Entry e : Q) {
            reinsert(e.key, e.value, e.height);
        }
        // 判断root是否下溢
        RTNode rtNode = getRTNode(root);
        if (!rtNode.isleaf && rtNode.size <= 1) {
            rtNode.delete = true;
            root = rtNode.ptrs[0];
            mapper.setNewRoot(root);
        }
        return true;
    }

    // 插入key 和 value 到树中
    public boolean insert(Rectangle key, long value) {
        RBranchRef ref = new RBranchRef();
        return insertAndAdjust(null, -1, getRTNode(root), key, value, ref);
    }

    // flush缓存(btMap)
    public void flush() {
        long delCnt = 0, updCnt = 0;
        for (RTNode bt : btMap.values()) {
            if (bt.delete) {
                mapper.recyclePage(bt.pageIndex);
                delCnt++;
            } else if (bt.change) {
                mapper.writePage(bt);
                updCnt++;
            }
        }
        System.out.println("提交成功！");
        System.out.println("\t写入：" + updCnt);
        System.out.println("\t删除：" + delCnt);
        btMap.clear();
    }


    public void draw() {
        Draw.init();
        draw(getRTNode(root), 0);
        Draw.done();
    }

    // 范围查找
    public List<SearchResult> searchRange(Rectangle range) {
        ArrayList<SearchResult> results = new ArrayList<>();
        searchRange(getRTNode(root), range, results);
        return results;
    }

    // 打印树结构
    public void printTree() {
        print(getRTNode(root), 0);
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
                rbt.height = T.height + 1;
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
        long seed1Ptr, seed2Ptr;
        seed1Ptr = seed2Ptr = T.ptrs[0];
        double maxIncr = Double.MIN_VALUE;
        for (int i = 0; i < T.size; ++i) {
            for (int j = i + 1; j < T.size; ++j) {
                double curIncr = Rectangle.incrArea(T.keys[i], T.keys[j]);
                if (curIncr > maxIncr) {
                    maxIncr = curIncr;
                    seed1 = T.keys[i];
                    seed1Ptr = T.ptrs[i];
                    seed2 = T.keys[j];
                    seed2Ptr = T.ptrs[j];
                }
            }
        }

        // 2. 按照种子split
        T.size = 0;
        double incr1, incr2;
        boolean lFull, rFull;
        Rectangle mbr1 = seed1.cloneRect(), mbr2 = seed2.cloneRect();
        lFull = rFull = false;
        int low = (m / 2) + (m % 2);
        for (int i = 0; i < m + 1; ++i) {
            if (T.keys[i] != seed1 && T.keys[i] != seed2) {
                if (!lFull && !rFull) {
                    incr1 = Rectangle.incrArea(T.keys[i], mbr1);
                    incr2 = Rectangle.incrArea(T.keys[i], mbr2);
                    if (incr1 < incr2) {
                        T.addEntry(T.keys[i], T.ptrs[i]);
                        mbr1 = Rectangle.mbr(mbr1, T.keys[i]);
                        if (T.size + 1 == low) lFull = true;
                    } else if (incr1 > incr2) {
                        rt.addEntry(T.keys[i], T.ptrs[i]);
                        mbr2 = Rectangle.mbr(mbr2, T.keys[i]);
                        if (rt.size + 1 == low) rFull = true;
                    } else if (mbr1.area() < mbr2.area()) {
                        T.addEntry(T.keys[i], T.ptrs[i]);
                        mbr1 = Rectangle.mbr(mbr1, T.keys[i]);
                        if (T.size + 1 == low) lFull = true;
                    } else if (mbr1.area() > mbr2.area()) {
                        rt.addEntry(T.keys[i], T.ptrs[i]);
                        mbr2 = Rectangle.mbr(mbr2, T.keys[i]);
                        if (rt.size + 1 == low) rFull = true;
                    } else if (T.size < rt.size) {
                        T.addEntry(T.keys[i], T.ptrs[i]);
                        mbr1 = Rectangle.mbr(mbr1, T.keys[i]);
                        if (T.size + 1 == low) lFull = true;
                    } else {
                        rt.addEntry(T.keys[i], T.ptrs[i]);
                        mbr2 = Rectangle.mbr(mbr2, T.keys[i]);
                        if (rt.size + 1 == low) rFull = true;
                    }
                } else if (lFull) {
                    rt.addEntry(T.keys[i], T.ptrs[i]);
                } else {
                    T.addEntry(T.keys[i], T.ptrs[i]);
                }
            }
        }

        T.addEntry(seed1, seed1Ptr);
        rt.addEntry(seed2, seed2Ptr);
        rt.isleaf = T.isleaf;
        rt.height = T.height;
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

    // 递归打印树结构
    private void print(RTNode T, int level) {
        if (T != null) {
            for (int i = 0; i < level; ++i) {
                System.out.print("\t");
            }
            System.out.printf("%s\n", T);
            if (!T.isleaf) {
                for (int i = 0; i < T.size; ++i) {
                    RTNode t = getRTNode(T.ptrs[i]);
                    print(t, level + 1);
                }
            }
        }
    }

    // 递归画出索引结构
    private void draw(RTNode T, int level) {
        for (int i = 0; i < T.size; ++i) {
            Draw.drawRect(T.keys[i], level + 1);
            if (!T.isleaf) {
                RTNode t = getRTNode(T.ptrs[i]);
                draw(t, level + 1);
            }
        }
    }

    // 范围查找（递归）
    private void searchRange(RTNode T, Rectangle range, List<SearchResult> result) {
        if (T.isleaf) {
            for (int i = 0; i < T.size; ++i)
                if (T.keys[i].overlap(range)) result.add(new SearchResult(T.keys[i], T.ptrs[i]));
        } else {
            for (int i = 0; i < T.size; ++i)
                if (T.keys[i].overlap(range)) searchRange(getRTNode(T.ptrs[i]), range, result);
        }
    }


    // 路径结点,用于路径保存
    static class PathNode {
        RTNode T;
        int index;

        public PathNode(RTNode t, int index) {
            this.index = index;
            T = t;
        }
    }

    // 路径结点,用于重插入
    static class Entry {
        Rectangle key;
        long value;
        int height; // 所在的结点高度

        public Entry(Rectangle key, long value, int height) {
            this.key = key;
            this.value = value;
            this.height = height;
        }
    }

    // 删除后自下向上调整结点
    private void adjust(Stack<PathNode> path, List<Entry> Q) {
        int low = (m / 2) + (m % 2);
        while (!path.empty()) {
            RTNode curNode = path.pop().T;
            if(curNode.pageIndex != root){
                if (curNode.size < low) {
                    for (int i = 0; i < curNode.size; ++i) {
                        Q.add(new Entry(curNode.keys[i], curNode.ptrs[i], curNode.height));
                    }
                    curNode.delete = true;
                    if (!path.empty()) {
                        RTNode father = path.peek().T;
                        int pindex = path.peek().index;
                        father.delete(pindex);
                        father.change = true;
                    }
                } else {
                    if (!path.empty()) {
                        RTNode father = path.peek().T;
                        int pindex = path.peek().index;
                        father.keys[pindex] = curNode.getMbr();
                        father.change = true;
                    }
                }
            }
        }
    }

    // 查找叶子中是否存在key, 返回path以及key在叶子中的index
    private boolean searchKey(RTNode T, Rectangle key, Stack<PathNode> path) {
        if (T.isleaf) {
            int index = T.indexOfKey(key);
            if (index != -1) {
                path.push(new PathNode(T, index));
                return true; //找到了
            } else {
                return false;
            }
        }
        // 不是叶子
        for (int i = 0; i < T.size; ++i) {
            if (T.keys[i].overlap(key)) {
                path.push(new PathNode(T, i));
                if (searchKey(getRTNode(T.ptrs[i]), key, path)) return true;
                else path.pop();
            }
        }
        return false;
    }

    // 重新插入到指定高度
    private boolean reinsert(Rectangle key, long value, int height) {
        RBranchRef ref = new RBranchRef();
        return reinsert(null, -1, getRTNode(root), key, value, ref, height);
    }

    // 重新递归插入到指定高度(树叶为高度为0)
    private boolean reinsert(RTNode parent, int pindex, RTNode T, Rectangle key, long value, RBranchRef ref, int height) {
        int i = T.getInsertPos(key);

        // 向下递归插入
        if (T.height > height) {
            boolean insertFlag = reinsert(T, i, getRTNode(T.ptrs[i]), key, value, ref, height);
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
        if (T.height == height) {
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
                rbt.height = T.height + 1;
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
}
