package com.azure.membtree01;

@SuppressWarnings("unchecked")
public class BTree<K extends Comparable<K>> {
    private int m; // m阶B树
    private TreeNode root; // 根结点

    public BTree(int m) {
        if (m < 3) throw new ExceptionInInitializerError("m is too small: at least 3");
        this.m = m;
        root = new TreeNode(m);
    }

    /**
     * 查找K
     *
     * @param key 关键字
     * @return 关键字所在的结点(不存在则返回叶结点)
     */
    public TreeNode search(K key) {
        return search(root, key);
    }

    /**
     * 插入key
     *
     * @param key 待插入关键字
     * @return 是否插入成功
     */
    public boolean insert(K key) {
        TreeNode curNode = search(root, key);
        if (!curNode.isLeaf()) {
            return false; // 关键字已经存在: 非叶结点
        }
        for (int i = 1; i < curNode.size; ++i) {
            if (key.compareTo((K) curNode.keys[i]) == 0)
                return false; // 关键字已经存在: 叶结点
        }

        // 关键字不存在: 执行插入
        boolean flag = false; // 插入完成的标志
        K curKey = key;
        TreeNode rightPtr = null;
        while (!flag && curNode != null) {
            // 插入curKey到当前结点
            int j = curNode.size;
            while (j > 1 && curKey.compareTo((K) curNode.keys[j - 1]) < 0) {
                curNode.keys[j] = curNode.keys[j - 1];
                curNode.ptrs[j] = curNode.ptrs[j - 1];
                --j;
            }
            curNode.keys[j] = curKey;
            curNode.ptrs[j] = rightPtr;
            curNode.size++;

            if (curNode.size <= m) {
                flag = true; // 插入完成
            } else { // 需要split结点
                int splitpos = (int) Math.ceil((double) m / 2.0); // 分裂点位置
                TreeNode newNode = new TreeNode(m);
                newNode.ptrs[0] = curNode.ptrs[splitpos];
                if (newNode.ptrs[0] != null)
                    newNode.ptrs[0].parent = newNode;
                for (int i = splitpos + 1, k = 1; i < curNode.size; ++i, ++k) {
                    newNode.keys[k] = curNode.keys[i];
                    newNode.ptrs[k] = curNode.ptrs[i];
                    if (newNode.ptrs[k] != null)
                        newNode.ptrs[k].parent = newNode; // 父指针指向新的结点
                }
                newNode.size = curNode.size - splitpos;
                curNode.size = splitpos;
                newNode.parent = curNode.parent;
                curKey = (K) curNode.keys[splitpos];
                rightPtr = newNode;
                curNode = curNode.parent; // 继续向上插入
            }
        }

        if (!flag) { // 根结点升高
            TreeNode newNode = new TreeNode(m);
            newNode.ptrs[0] = root;
            newNode.ptrs[1] = rightPtr;
            newNode.keys[1] = curKey;
            newNode.size = 2;
            rightPtr.parent = root.parent = newNode;
            root = newNode; // 更新根结点
        }

        return true; // 插入成功
    }

    /**
     * 删除key
     *
     * @param key 待输入关键词
     * @return 是否删除成功
     */
    public boolean delete(K key) {
        TreeNode curNode = search(root, key);
        int curIndex = -1;
        for (int i = 1; i < curNode.size; ++i) { // 在结点中找到要删除的位置
            if (key.compareTo((K) curNode.keys[i]) == 0) {
                curIndex = i;
                break;
            }
        }
        if (curIndex == -1) return false; //// 删除失败: key不存在

        if (!curNode.isLeaf()) { // 关键字不在叶子上: 用右子树上的最小key代替
            TreeNode p = curNode.ptrs[curIndex];
            while (!p.isLeaf()) p = p.ptrs[0]; // p指向右子树的最小结点
            K t = (K) p.keys[1];
            p.keys[1] = curNode.keys[curIndex];
            curNode.keys[curIndex] = t;
            curIndex = 1;
            curNode = p;
        }

        // 关键字在叶子上: 执行删除
        int j = curIndex + 1;
        while (j < curNode.size){
            curNode.keys[j - 1] = curNode.keys[j];
            j++;
        }
        curNode.size--;

        // 调整过程
        boolean flag = false; // 合并完成
        int minsize = (int) Math.ceil((double) m / 2.0);
        while (!flag) {
            if (curNode.size < minsize) { // 结点元素少了
                if (curNode == root) { // 是根结点
                    if (curNode.size <= 1) {
                        if (!root.isLeaf()) {
                            root = curNode.ptrs[0]; // 根结点下降
                            root.parent = null;
                        }
                    }
                    flag = true;
                } else { // 是非根结点
                    TreeNode parent = curNode.parent;
                    TreeNode lBrother = null;
                    TreeNode rBrother = null;
                    int pindex = -1; // curNode在父亲结点中的位置
                    for (int i = 0; i < parent.size; ++i) { // 寻找当前结点左右兄弟
                        if (parent.ptrs[i] == curNode) {
                            if (i > 0) lBrother = parent.ptrs[i - 1];
                            if (i < parent.size - 1) rBrother = parent.ptrs[i + 1];
                            pindex = i;
                            break;
                        }
                    }
                    if (lBrother != null && lBrother.size > minsize) { // 向左兄弟借一个key及其子树
                        K lkey = (K) lBrother.keys[lBrother.size - 1];
                        TreeNode lptr = lBrother.ptrs[lBrother.size - 1];
                        for (int i = curNode.size; i > 0; --i) {
                            curNode.keys[i] = curNode.keys[i - 1];
                            curNode.ptrs[i] = curNode.ptrs[i - 1];
                        }
                        curNode.keys[1] = parent.keys[pindex]; // 借key
                        curNode.ptrs[0] = lptr; // 借子树
                        if (lptr != null) lptr.parent = curNode;
                        parent.keys[pindex] = lkey;
                        curNode.size++;
                        lBrother.size--;
                        flag = true;
                    } else if (rBrother != null && rBrother.size > minsize) { // 向右兄弟借一个key及其子树
                        K rkey = (K) rBrother.keys[1];
                        TreeNode rptr = rBrother.ptrs[0];
                        curNode.keys[curNode.size] = parent.keys[pindex + 1]; // 借key
                        curNode.ptrs[curNode.size] = rptr; // 借子树
                        if (rptr != null) rptr.parent = curNode;
                        parent.keys[pindex + 1] = rkey;
                        for (int i = 0; i < rBrother.size - 1; ++i) {
                            rBrother.keys[i] = rBrother.keys[i + 1];
                            rBrother.ptrs[i] = rBrother.ptrs[i + 1];
                        }
                        curNode.size++;
                        rBrother.size--;
                        flag = true;
                    } else {
                        if (lBrother != null) {
                            // 合并到左兄弟
                            lBrother.keys[lBrother.size] = parent.keys[pindex];
                            lBrother.ptrs[lBrother.size] = curNode.ptrs[0];
                            if (lBrother.ptrs[lBrother.size] != null)
                                lBrother.ptrs[lBrother.size].parent = lBrother;
                            for (int i = 1, k = lBrother.size + 1; i < curNode.size; ++i, ++k) {
                                lBrother.keys[k] = curNode.keys[i];
                                lBrother.ptrs[k] = curNode.ptrs[i];
                                if (lBrother.ptrs[k] != null)
                                    lBrother.ptrs[k].parent = lBrother;
                            }
                            lBrother.size += curNode.size;
                            // 调整parent
                            for (int i = pindex; i < parent.size - 1; ++i) {
                                parent.keys[i] = parent.keys[i + 1];
                                parent.ptrs[i] = parent.ptrs[i + 1];
                            }
                            parent.size--;
                            curNode = parent; // 向上调整
                        } else if (rBrother != null) {
                            // 将右兄弟合并
                            curNode.keys[curNode.size] = parent.keys[pindex + 1];
                            curNode.ptrs[curNode.size] = rBrother.ptrs[0];
                            if (curNode.ptrs[curNode.size] != null)
                                curNode.ptrs[curNode.size].parent = curNode;
                            for (int i = 1, k = curNode.size + 1; i < rBrother.size; ++i, ++k) {
                                curNode.keys[k] = rBrother.keys[i];
                                curNode.ptrs[k] = rBrother.ptrs[i];
                                if (curNode.ptrs[k] != null)
                                    curNode.ptrs[k].parent = curNode;
                            }
                            curNode.size += rBrother.size;
                            // 调整parent
                            for (int i = pindex + 1; i < parent.size - 1; ++i) {
                                parent.keys[i] = parent.keys[i + 1];
                                parent.ptrs[i] = parent.ptrs[i + 1];
                            }
                            parent.size--;
                            curNode = parent; // 向上调整
                        }
                    }
                }
            } else {
                flag = true;
            }
        }
        return true; // 删除成功
    }

    /**
     * 打印树结构
     */
    public void print() {
        print(root, 0);
    }


    /**
     * 中序遍历所有keys
     */
    public void printKeysInorder() {
        printKeysInorder(root);
    }


    /*---------------------------------------------------------⬇️私有方法⬇️-----------------------------------------------*/

    /**
     * 递归打印树结构
     *
     * @param T     当前树
     * @param level 第几层
     */
    private void print(TreeNode T, int level) {
        if (T != null) {
            for (int i = 0; i < level; ++i) {
                System.out.print("\t");
            }
            System.out.printf("%s\n", T);
            for (int i = 0; i < T.size; ++i) {
                print(T.ptrs[i], level + 1);
            }
        }
    }

    /**
     * 递归中序遍历keys
     *
     * @param T 当前树
     */
    private void printKeysInorder(TreeNode T) {
        if (T != null) {
            for (int i = 1; i < T.size; ++i) {
                printKeysInorder(T.ptrs[i - 1]);
                K k = (K) T.keys[i];
                System.out.println(k);
            }
            printKeysInorder(T.ptrs[T.size - 1]);
        }
    }

    /**
     * 递归查找K
     *
     * @param T   当前树
     * @param key 关键字
     * @return 关键字所在的结点(不存在则返回叶结点)
     */
    private TreeNode search(TreeNode T, K key) {
        if (T.isLeaf()) // 叶子直接返回
            return T;

        // 查找非叶结点
        for (int i = 1; i < T.size; ++i) {
            K k = (K) T.keys[i];
            if (key.compareTo(k) < 0) {
                return search(T.ptrs[i - 1], key);
            } else if (key.compareTo(k) == 0) // 找到了
                return T;
        }
        return search(T.ptrs[T.size - 1], key);
    }
}
