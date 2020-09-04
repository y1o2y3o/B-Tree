package com.azure.disk.bptree01;

import java.util.ArrayList;
import java.util.HashMap;

public class BTree { //
    public static final long NIL = -1L;
    private HashMap<Long, BTNode> btMap = new HashMap<>();

    private final int m; // m阶B树
    private long root;
    private long sqt;
    private FileMapper mapper;

    public BTree(FileMapper mapper) {
        this.mapper = mapper;
        this.m = mapper.m;
        this.root = mapper.root;
        this.sqt = mapper.sqt;
    }

    /**
     * 插入B树中的某个key: 删除成功返回true
     *
     * @param key 关键字
     */
    public boolean delete(long key) {
        MergeFlag flag = new MergeFlag();
        return deleteAndAdjust(null, -1, getBTNode(root), key, flag);
    }

    /**
     * 范围搜索
     *
     * @param lb 左边界
     * @param rb 右边界
     * @return 数组
     */
    public ArrayList<Long> searchRange(long lb, long rb) {
        ArrayList<Long> list = new ArrayList<>();
        if (lb <= rb) {
            Result result = searchKey(lb);
            long p = result.pt.pageIndex;
            BTNode pbt = result.pt;
            int i = result.index;
            boolean end = false;
            while (p != NIL && !end) {
                for (int j = i; j < pbt.size; ++j) {
                    if (pbt.keys[j] <= rb) {
                        list.add(pbt.ptrs[j]);
                    } else {
                        end = true;
                        break;
                    }
                }
                i = 0;
                p = pbt.next;
                if (p != NIL) pbt = getBTNode(p);
            }
        }
        return list;
    }

    /**
     * 在B树上查找关键字key,返回 Result(pt, index, tag)。若查找成功，则特征值tag 为 true, pt.keys[index] 等于 key；
     * 否则tag = false, 等于key的关键字应该插入在 pt.keys[index-1] 和 pt.keys[index]之间。
     *
     * @param key 关键字
     * @return 结果
     */
    private Result searchKey(long key) {
        long p = root;
        BTNode pbt = getBTNode(p);
        int index = 0;
        while (!pbt.isleaf) {
            index = lowerBound(pbt, key);
            if (index >= pbt.size)
                index = pbt.size - 1;
            p = pbt.ptrs[index];
            pbt = getBTNode(p);
        }
        index = lowerBound(pbt, key);
        if (pbt.size > 0 && key == pbt.keys[index]) {
            return new Result(pbt, index, true);
        } else {
            return new Result(pbt, index, false);
        }
    }

    /**
     * 打印树结构
     */
    public void print() {
        print(getBTNode(root), 0);
    }

    /**
     * 顺序遍历keys
     */
    public void printKeysInorder() {
        long p = sqt;
        long last = NIL;
        while (p != NIL) {
            BTNode pbt = getBTNode(p);
            for (int i = 0; i < pbt.size; ++i) {
                long k = pbt.keys[i];
                System.out.println(k + " ");
                if (last != NIL && last > k)
                    throw new Error("B树错误: 元素不是有序的！");
                last = k;
            }
            p = pbt.next;
        }
    }

    /**
     * 插入key 和 value 到树中
     *
     * @param key   关键字
     * @param value 卫星数据
     */
    public boolean insert(long key, long value) {
        RBranchRef ref = new RBranchRef();
        return insertAndAdjust(null, -1, getBTNode(root), key, value, ref);
    }

    // flush缓存(btMap)
    public void flush() {
        long delCnt = 0, updCnt = 0;
        for (BTNode bt : btMap.values()) {
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

    /**
     * 搜索key对应的value(long)
     *
     * @param key 关键字
     * @return value
     */
    public long search(long key) {
        long p = root;
        BTNode pbt = getBTNode(p);
        int i;
        while (!pbt.isleaf) {
            i = lowerBound(pbt, key);
            if (i == pbt.size) return NIL;
            p = pbt.ptrs[i];
            pbt = getBTNode(p);
        }
        i = lowerBound(pbt, key);
        if (i < pbt.size && pbt.keys[i] == key) return pbt.ptrs[i];
        return NIL;
    }

    /*---------------------------------------------------------⬇️私有方法⬇️-----------------------------------------------*/

    /**
     * 返回第一个>=key的位置,如果找不到则返回最后一个元素的位置
     *
     * @param T   结点
     * @param key 关键字
     * @return 位置
     */
    private int lowerBound(BTNode T, long key) {
        int low = 0;
        int high = T.size;
        while (low < high) {
            int mid = (low + high) / 2;
            long k = T.keys[mid];
            long cmp = key - k;
            if (cmp <= 0) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }
        return low;
    }

    /**
     * 分支引用类,用来传递待插入的分支引用(作用相当于C++里的引用)
     */
    private static class RBranchRef {
        long rbranch = NIL;
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
    private boolean insertAndAdjust(BTNode parent, int pindex, BTNode T, long key, long value, RBranchRef ref) {
        int i = lowerBound(T, key);
        if (i == T.size) --i;

        // 向下递归插入
        if (!T.isleaf) {
            boolean insertFlag = insertAndAdjust(T, i, getBTNode(T.ptrs[i]), key, value, ref);
            if (!insertFlag) return false; // 数据已经存在, 不需要插入
        } else {
            // 卫星数据已存在, 更新
            if (T.size > 0 && key == T.keys[i]) {
                T.ptrs[i] = value;
                return false;
            }
        }

        // 向上调整
        long curKey = NIL;
        if (T.isleaf) {
            curKey = key;
            ref.rbranch = value;
        } else if (ref.rbranch != NIL) {
            curKey = getBTNode(ref.rbranch).getMaxKey();
        }
        // 插入
        if (curKey != NIL) {
            if (T.size >= m)
                insertAndRSplit(T, curKey, ref);
            else
                insert(T, curKey, ref);
        }

        // 更新父亲key结点
        if (T.pageIndex == root) {
            // 根结点长高一层
            if (ref.rbranch != NIL) {
                BTNode rbt = createBTNode();
                rbt.isleaf = false;
                rbt.size = 2;
                rbt.keys[0] = T.getMaxKey();
                rbt.keys[1] = getBTNode(ref.rbranch).getMaxKey();
                rbt.ptrs[0] = T.pageIndex;
                rbt.ptrs[1] = ref.rbranch;
                root = rbt.pageIndex;
                mapper.setNewRoot(root); // 设置新的根结点索引
            }
        } else {
            // 更新父亲key结点
            parent.keys[pindex] = T.getMaxKey();
            parent.change = true;
        }

        return true;
    }

    /**
     * 简单插入
     *
     * @param T      当前结点
     * @param curKey 插入的Key
     * @param ref    插入的分支
     */
    private void insert(BTNode T, long curKey, RBranchRef ref) {
        int i = lowerBound(T, curKey);
        for (int j = T.size - 1; j >= i; --j) {
            T.ptrs[j + 1] = T.ptrs[j];
            T.keys[j + 1] = T.keys[j];
        }
        T.ptrs[i] = ref.rbranch;
        T.keys[i] = curKey;
        T.size++;
        ref.rbranch = NIL;
        T.change = true;
    }

    /**
     * 插入并向右分裂
     *
     * @param T      当前结点
     * @param curKey 插入的Key
     * @param ref    插入的分支
     */
    private void insertAndRSplit(BTNode T, long curKey, RBranchRef ref) {
        BTNode rb = createBTNode();
        insert(T, curKey, ref);
        int splitpos = (int) Math.ceil((double) m / 2.0);
        for (int i = splitpos, k = 0; i < T.size; ++i, ++k) { // 分裂
            rb.keys[k] = T.keys[i];
            rb.ptrs[k] = T.ptrs[i];
            T.keys[i] = T.ptrs[i] = NIL; // 剪掉分支
        }
        rb.size = T.size - splitpos;
        T.size = splitpos;
        rb.isleaf = T.isleaf;
        ref.rbranch = rb.pageIndex;

        // 更新叶结点之间的指针（双向链表）
        if (T.isleaf) {
            rb.prev = T.pageIndex;
            rb.next = T.next;
            if (T.next != NIL) {
                BTNode tn = getBTNode(T.next);
                tn.prev = rb.pageIndex;
                tn.change = true;
            }
            T.next = rb.pageIndex;
        }
    }

    // 根据pageIndex 获取 已经存在的 BTNode
    private BTNode getBTNode(long pageIndex) {
        if (btMap.containsKey(pageIndex)) {
            return btMap.get(pageIndex);
        } else {
            BTNode btNode = mapper.readPage(pageIndex);
            btNode.change = false;
            btMap.put(pageIndex, btNode);
            return btNode;
        }
    }

    // 创建一个新的BTNode（需要手动回收）
    private BTNode createBTNode() {
        BTNode newNode = new BTNode(m);
        newNode.pageIndex = mapper.allocatePage();
        btMap.put(newNode.pageIndex, newNode);
        return newNode;
    }

    // 将缓存中的某个结点覆盖到文件中
    private void updateBTNode(long pageIndex) {
        if (btMap.containsKey(pageIndex))
            mapper.writePage(btMap.get(pageIndex));
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
                    BTNode t = getBTNode(T.ptrs[i]);
                    print(t, level + 1);
                }
            }
        }
    }

    /**
     * 合并标志类
     */
    private static class MergeFlag {
        boolean left = false;
        boolean right = false;
    }

    /**
     * 简单删除
     *
     * @param T    当前树
     * @param i    待删除key的下标
     * @param flag 合并标志
     */
    private void delete(BTNode T, int i, MergeFlag flag) {
        for (int j = i; j < T.size - 1; ++j) {
            T.keys[j] = T.keys[j + 1];
            T.ptrs[j] = T.ptrs[j + 1];
        }
        T.keys[T.size - 1] = T.ptrs[T.size - 1] = NIL;
        T.size--;
        flag.left = flag.right = false;
        T.change = true;
    }

    /**
     * 删除并借走左兄弟一个元素
     *
     * @param parent 父亲
     * @param pindex 父亲中当前结点的索引下标
     * @param T      当前结点
     * @param index  待删除的关键字下标
     * @param flag   合并标志
     */
    private void deleteAndBorrowLeft(BTNode parent, int pindex, BTNode T, int index, MergeFlag flag) {
        delete(T, index, flag);
        BTNode lBrother = getBTNode(parent.ptrs[pindex - 1]);
        for (int j = T.size - 1; j >= 0; --j) {
            T.keys[j + 1] = T.keys[j];
            T.ptrs[j + 1] = T.ptrs[j];
        }
        int li = lBrother.size - 1;
        T.keys[0] = lBrother.keys[li];
        T.ptrs[0] = lBrother.ptrs[li];
        T.size++;
        lBrother.keys[li] = lBrother.ptrs[li] = NIL;
        lBrother.size--;
        lBrother.change = true;
    }

    /**
     * 删除并借走右兄弟一个元素
     *
     * @param parent 父亲
     * @param pindex 父亲中当前结点的索引下标
     * @param T      当前结点
     * @param index  待删除的关键字下标
     * @param flag   合并标志
     */
    private void deleteAndBorrowRight(BTNode parent, int pindex, BTNode T, int index, MergeFlag flag) {
        delete(T, index, flag);
        BTNode rBrother = getBTNode(parent.ptrs[pindex + 1]);
        T.keys[T.size] = rBrother.keys[0];
        T.ptrs[T.size] = rBrother.ptrs[0];
        T.size++;
        for (int j = 0; j < rBrother.size - 1; ++j) {
            rBrother.keys[j] = rBrother.keys[j + 1];
            rBrother.ptrs[j] = rBrother.ptrs[j + 1];
        }

        int ri = rBrother.size - 1;
        rBrother.keys[ri] = rBrother.ptrs[ri] = NIL;
        rBrother.size--;
        rBrother.change = true;
    }

    /**
     * 删除并合并至左兄弟
     *
     * @param parent 父亲
     * @param pindex 父亲中当前结点的索引下标
     * @param T      当前结点
     * @param index  待删除的关键字下标
     * @param flag   合并标志
     */
    private void deleteAndMergeToLeft(BTNode parent, int pindex, BTNode T, int index, MergeFlag flag) {
        delete(T, index, flag);
        BTNode lBrother = getBTNode(parent.ptrs[pindex - 1]);
        for (int i = lBrother.size, j = 0; j < T.size; ++j, ++i) {
            lBrother.keys[i] = T.keys[j];
            lBrother.ptrs[i] = T.ptrs[j];
            T.keys[j] = T.ptrs[j] = NIL;
        }
        lBrother.size += T.size;
        flag.left = true;
        lBrother.change = true;
        T.delete = true;
    }

    /**
     * 删除并合并至右兄弟
     *
     * @param parent 父亲
     * @param pindex 父亲中当前结点的索引下标
     * @param T      当前结点
     * @param index  待删除的关键字下标
     * @param flag   合并标志
     */
    private void deleteAndMergeToRight(BTNode parent, int pindex, BTNode T, int index, MergeFlag flag) {
        delete(T, index, flag);
        BTNode rBrother = getBTNode(parent.ptrs[pindex + 1]);
        for (int j = rBrother.size - 1; j >= 0; --j) {
            rBrother.keys[j + T.size] = rBrother.keys[j];
            rBrother.ptrs[j + T.size] = rBrother.ptrs[j];
        }
        rBrother.size += T.size;
        for (int j = 0; j < T.size; ++j) {
            rBrother.keys[j] = T.keys[j];
            rBrother.ptrs[j] = T.ptrs[j];
            T.keys[j] = T.ptrs[j] = NIL;
        }
        flag.right = true;
        rBrother.change = true;
        T.delete = true;
    }

    /**
     * 递归删除B树中的key和它所对应的值
     *
     * @param parent 当前结点父亲
     * @param pindex 当前结点在父结点中的索引下标
     * @param T      当前结点
     * @param key    待删除关键字
     * @param flag   合并标志
     * @return 删除成功
     */
    private boolean deleteAndAdjust(BTNode parent, int pindex, BTNode T, long key, MergeFlag flag) {
        int i = lowerBound(T, key);
        if (i >= T.size) return false;

        // 向下递归删除
        if (!T.isleaf) {
            boolean deleteFlag = deleteAndAdjust(T, i, getBTNode(T.ptrs[i]), key, flag);
            if (!deleteFlag) return false; // 数据不存在, 删除失败
        } else {
            // 待删除的卫星数据不存在,删除失败
            if (key != T.keys[i]) {
                return false;
            }
        }

        // 向上调整
        // 删除
        if (T.isleaf || flag.left || flag.right) {
            int low = (int) Math.ceil((double) m / 2.0);
            // 直接删除
            if (T.pageIndex == root || T.size > low) {
                delete(T, i, flag);
            } else {
                // 下溢
                int psize = parent.size;
                boolean borrowed = false;
                BTNode lBrother = null, rBrother = null;

                // 向兄弟借key
                if (pindex > 0) { // 尝试借左兄弟
                    lBrother = getBTNode(parent.ptrs[pindex - 1]);
                    if (lBrother.size > low) {
                        deleteAndBorrowLeft(parent, pindex, T, i, flag);
                        borrowed = true;
                        // 调整父结点
                        parent.keys[pindex - 1] = lBrother.getMaxKey();
                        parent.change = true;
                    }
                } else if (pindex < psize - 1) { // 尝试借右兄弟
                    rBrother = getBTNode(parent.ptrs[pindex + 1]);
                    if (rBrother.size > low) {
                        deleteAndBorrowRight(parent, pindex, T, i, flag);
                        borrowed = true;
                        // 调整父结点
                        parent.keys[pindex] = T.getMaxKey();
                        parent.change = true;
                    }

                }
                // 借兄弟不成,则合并至兄弟
                if (!borrowed) {
                    if (lBrother != null) {
                        deleteAndMergeToLeft(parent, pindex, T, i, flag);
                        // 调整父结点
                        parent.keys[pindex - 1] = lBrother.getMaxKey();
                        parent.change = true;
                        // 调整叶子链表
                        if (T.isleaf) {
                            lBrother.next = T.next;
                            if (T.next != NIL) {
                                BTNode tn = getBTNode(T.next);
                                tn.prev = lBrother.pageIndex;
                            }
                        }
                    } else if (rBrother != null) {
                        deleteAndMergeToRight(parent, pindex, T, i, flag);
                        // 调整父结点
                        //parent.keys[pindex + 1] = rBrother.getMaxKey();
                        // 调整叶子链表
                        if (T.isleaf) {
                            rBrother.prev = T.prev;
                            if (T.prev != NIL) {
                                BTNode tp = getBTNode(T.prev);
                                tp.next = rBrother.pageIndex;
                            } else sqt = rBrother.pageIndex;
                        }
                    }
                }
            }
        }

        // 如果根处下溢
        if (T.pageIndex == root && !T.isleaf && T.size <= 1) {
            // 根结点降低一层
            root = T.ptrs[0];
            mapper.setNewRoot(root);
            T.delete = true; // 已删除
        }
        return true;
    }

    // 删除一个结点, 连带缓存中数据一起删除
    private void deleteNode(long pageIndex) {
        if (btMap.containsKey(pageIndex)) {
            mapper.recyclePage(pageIndex);
        }
        btMap.remove(pageIndex);
    }
}
