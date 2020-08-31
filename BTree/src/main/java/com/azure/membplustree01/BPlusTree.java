package com.azure.membplustree01;

import apple.laf.JRSUIUtils;

import java.util.Random;

@SuppressWarnings("unchecked")
public class BPlusTree<K extends Comparable<K>> {
    private int m; // m阶B树
    private TreeNode root;
    private TreeNode sqt;

    public BPlusTree(int m) {
        if (m < 3) throw new ExceptionInInitializerError("m is too small: at least 3");
        this.m = m;
        root = new TreeNode(m);
        sqt = root;
    }

    /**
     * 插入key
     *
     * @param key 待插入关键字
     * @return 是否插入成功
     */
    public boolean insert(K key, Object value) {
        TreeNode curNode = search(root, key);

        for (int i = 0; i < curNode.size; ++i) {
            if (key.compareTo((K) curNode.keys[i]) == 0)
                return false; // 关键字已经存在: 叶结点
        }

        // 关键字不存在: 执行插入
        boolean flag = false; // 插入完成的标志
        K curKey = key;
        TreeNode lastPtr = null;
        while (curNode != null) {
            if (!flag) { // 还未插入完成
                // 插入curKey到当前结点
                int j = curNode.size;
                while (j > 0 && curKey.compareTo((K) curNode.keys[j - 1]) < 0) {
                    curNode.keys[j] = curNode.keys[j - 1];
                    curNode.ptrs[j] = curNode.ptrs[j - 1];
                    --j;
                }
                curNode.keys[j] = curKey;
                curNode.ptrs[j] = curNode.isLeaf() ? value : lastPtr; // 插入值或者索引地址
                curNode.size++;

                if (curNode != root) { // 调整最大key
                    K pMaxKey = (K) curNode.parent.getMaxKey();
                    K cMaxKey = (K) curNode.getMaxKey();
                    if (pMaxKey.compareTo(cMaxKey) < 0) {
                        curNode.parent.setMaxKey(cMaxKey);
                    }
                }
                if (curNode.size <= m) {
                    flag = true; // 插入完成
                } else { // 需要split结点
                    int splitpos = (int) Math.ceil((double) m / 2.0); // 分裂点位置
                    TreeNode newNode = new TreeNode(m);
                    for (int i = 0, k = 0; i < splitpos; ++i, ++k) { // 分裂
                        newNode.keys[k] = curNode.keys[i];
                        newNode.ptrs[k] = curNode.ptrs[i];
                        if (newNode.ptrs[k] != null && !curNode.isLeaf())
                            ((TreeNode) newNode.ptrs[k]).parent = newNode; // 父指针指向新的结点
                    }
                    for (int i = splitpos, k = 0; i < curNode.size; ++i, ++k) {
                        curNode.keys[k] = curNode.keys[i];
                        curNode.ptrs[k] = curNode.ptrs[i];
                    }
                    curNode.size -= splitpos;
                    newNode.size = splitpos;
                    newNode.parent = curNode.parent;
                    newNode.isleaf = curNode.isleaf;
                    if (newNode.isleaf) {
                        newNode.prev = curNode.prev;
                        newNode.next = curNode;
                        if (curNode.prev != null)
                            curNode.prev.next = newNode;
                        curNode.prev = newNode;
                        if (newNode.prev == null) sqt = newNode;
                    }
                    curKey = (K) newNode.getMaxKey();
                    lastPtr = newNode;
                    curNode = curNode.parent; // 继续向上插入
                }
            } else { // 插入已经完成
                if (curNode != root) {
                    K pMaxKey = (K) curNode.parent.getMaxKey();
                    K cMaxKey = (K) curNode.getMaxKey();
                    if (pMaxKey.compareTo(cMaxKey) < 0) {
                        curNode.parent.setMaxKey(cMaxKey);
                        curNode = curNode.parent;
                    } else {
                        break; // 调整最大key完成
                    }
                } else {
                    break; // 调整最大key完成
                }
            }

        }

        if (!flag) { // 根结点升高
            TreeNode newNode = new TreeNode(m);
            newNode.ptrs[1] = root;
            newNode.keys[1] = root.getMaxKey();
            newNode.ptrs[0] = lastPtr;
            newNode.keys[0] = curKey;
            newNode.size = 2;
            newNode.isleaf = false;
            lastPtr.parent = root.parent = newNode;
            lastPtr.isleaf = root.isleaf;
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
        for (int i = 0; i < curNode.size; ++i) { // 在结点中找到要删除的位置
            if (key.compareTo((K) curNode.keys[i]) == 0) {
                curIndex = i;
                break;
            }
        }
        if (curIndex == -1) return false; //// 删除失败: key不存在

        // 关键字在叶子上: 执行删除
        int j = curIndex + 1;
        while (j < curNode.size) {
            curNode.keys[j - 1] = curNode.keys[j];
            curNode.ptrs[j - 1] = curNode.ptrs[j];
            j++;
        }
        curNode.size--;

        // 调整过程
        boolean flag = false; // 合并完成
        int minsize = (int) Math.ceil((double) m / 2.0);
        while (true) {
            if (!flag) { // 合并未完成
                if (curNode.size < minsize) { // 结点元素少了
                    if (curNode == root) { // 是根结点
                        if (curNode.size <= 1) {
                            if (!root.isLeaf()) {
                                root = (TreeNode) curNode.ptrs[0]; // 根结点下降
                                root.parent = null;
                            }
                        }
                        flag = true;
                        break; // 完成退出
                    } else { // 是非根结点
                        TreeNode parent = curNode.parent;
                        TreeNode lBrother = null;
                        TreeNode rBrother = null;
                        int pindex = -1; // curNode在父亲结点中的位置
                        for (int i = 0; i < parent.size; ++i) { // 寻找当前结点左右兄弟
                            if (parent.ptrs[i] == curNode) {
                                if (i > 0) lBrother = (TreeNode) parent.ptrs[i - 1];
                                if (i < parent.size - 1) rBrother = (TreeNode) parent.ptrs[i + 1];
                                pindex = i;
                                break;
                            }
                        }
                        parent.keys[pindex] = curNode.getMaxKey(); // 调整curNode索引key
                        if (lBrother != null && lBrother.size > minsize) { // 向左兄弟借一个key及其子树
                            K lkey = (K) lBrother.keys[lBrother.size - 1];
                            Object lptr = lBrother.ptrs[lBrother.size - 1];
                            for (int i = curNode.size; i > 0; --i) {
                                curNode.keys[i] = curNode.keys[i - 1];
                                curNode.ptrs[i] = curNode.ptrs[i - 1];
                            }
                            curNode.keys[0] = lkey; // 借key
                            curNode.ptrs[0] = lptr; // 借子树
                            if (!curNode.isLeaf()) ((TreeNode) lptr).parent = curNode;
                            curNode.size++;
                            lBrother.size--;
                            parent.keys[pindex - 1] = lBrother.getMaxKey(); // 调整lBrother索引key
                            flag = true;
                        } else if (rBrother != null && rBrother.size > minsize) { // 向右兄弟借一个key及其子树
                            K rkey = (K) rBrother.keys[0];
                            Object rptr = rBrother.ptrs[0];
                            curNode.keys[curNode.size] = rkey; // 借key
                            curNode.ptrs[curNode.size] = rptr; // 借子树
                            if (!curNode.isLeaf()) ((TreeNode) rptr).parent = curNode;
                            parent.keys[pindex] = rkey; // 调整curNode索引key
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
                                for (int i = 0, k = lBrother.size; i < curNode.size; ++i, ++k) {
                                    lBrother.keys[k] = curNode.keys[i];
                                    lBrother.ptrs[k] = curNode.ptrs[i];
                                    if (!curNode.isLeaf())
                                        ((TreeNode) lBrother.ptrs[k]).parent = lBrother;
                                }
                                lBrother.size += curNode.size;
                                // 调整parent
                                for (int i = pindex; i < parent.size - 1; ++i) {
                                    parent.keys[i] = parent.keys[i + 1];
                                    parent.ptrs[i] = parent.ptrs[i + 1];
                                }
                                parent.size--;
                                parent.keys[pindex - 1] = lBrother.getMaxKey();
                                if (curNode.isLeaf()) {
                                    lBrother.next = curNode.next;
                                    if (curNode.next != null) {
                                        curNode.next.prev = lBrother;
                                    }
                                }
                                curNode = parent; // 向上调整
                            } else if (rBrother != null) {
                                // 将右兄弟合并
                                for (int i = 0, k = curNode.size; i < rBrother.size; ++i, ++k) {
                                    curNode.keys[k] = rBrother.keys[i];
                                    curNode.ptrs[k] = rBrother.ptrs[i];
                                    if (!curNode.isLeaf())
                                        ((TreeNode) curNode.ptrs[k]).parent = curNode;
                                }
                                curNode.size += rBrother.size;
                                // 调整parent
                                for (int i = pindex + 1; i < parent.size - 1; ++i) {
                                    parent.keys[i] = parent.keys[i + 1];
                                    parent.ptrs[i] = parent.ptrs[i + 1];
                                }
                                parent.size--;
                                parent.keys[pindex] = curNode.getMaxKey();
                                if (curNode.isLeaf()) {
                                    curNode.next = rBrother.next;
                                    if (rBrother.next != null) {
                                        rBrother.next.prev = curNode;
                                    }
                                }
                                curNode = parent; // 向上调整
                            }
                        }
                    }
                } else { // 开始调整索引key的值
                    flag = true;
                    if (curNode == root) {
                        break;// 完成退出
                    }
                    TreeNode parent = curNode.parent;
                    int pindex = -1; // curNode在父亲结点中的位置
                    for (int i = 0; i < parent.size; ++i) { // 寻找当前结点索引位置
                        if (parent.ptrs[i] == curNode) {
                            pindex = i;
                            break;
                        }
                    }
                    K oldKey = (K) parent.keys[pindex];
                    if (oldKey.compareTo((K) (curNode.getMaxKey())) != 0) {
                        parent.keys[pindex] = curNode.getMaxKey();
                        if (pindex == parent.size - 1)
                            curNode = parent; // 向上调整
                        else
                            break; //完成退出
                    } else {
                        break; // 退出
                    }
                }
            } else { // 调整索引key的值
                if (curNode == root) {
                    break;// 完成退出
                }
                TreeNode parent = curNode.parent;
                int pindex = -1; // curNode在父亲结点中的位置
                for (int i = 0; i < parent.size; ++i) { // 寻找当前结点索引位置
                    if (parent.ptrs[i] == curNode) {
                        pindex = i;
                        break;
                    }
                }
                K oldKey = (K) parent.keys[pindex];
                if (oldKey.compareTo((K) (curNode.getMaxKey())) != 0) {
                    parent.keys[pindex] = curNode.getMaxKey();
                    if (pindex == parent.size - 1)
                        curNode = parent; // 向上调整
                    else
                        break; //完成退出
                } else {
                    break; // 退出
                }
            }

        }
        return true; // 删除成功
    }

    /**
     * 查找K
     *
     * @param key 待查找关键字
     * @return 关键字所在的结点(不存在则返回叶结点)
     */
    public TreeNode search(K key) {
        return search(root, key);
    }


    /**
     * 逆中序遍历keys
     */
    public void printKeysInorder() {
        TreeNode p = sqt;
        while (p != null) {
            for (int i = 0; i < p.size; ++i) {
                K k = (K) p.keys[i];
                System.out.println(k);
            }
            p = p.next;
        }
    }

    /**
     * 打印树结构
     */
    public void print() {
        print(root, 0);
    }

    /**
     * 查找并返回key对应的value
     *
     * @param key 关键字
     * @return value
     */
    public Object get(K key) {
        TreeNode p = root;
        int index;
        while (!p.isLeaf()) {
            index = indexOf(p, key);
            if(index == -1)
                return null; // 查找失败
            p = (TreeNode) p.ptrs[index];
        }
        index = indexOf(p, key);
        if(index == -1)
            return null; // 查找失败
        if (key.compareTo((K) p.keys[index]) == 0) {
            return p.ptrs[index];
        }
        return null;
    }

    /*---------------------------------------------------------⬇️私有方法⬇️-----------------------------------------------*/

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
        for (int i = 0; i < T.size; ++i) {
            K k = (K) T.keys[i];
            if (key.compareTo(k) <= 0) {
                return search((TreeNode) T.ptrs[i], key);
            }
        }
        return search((TreeNode) T.ptrs[T.size - 1], key);
    }

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
            if (!T.isLeaf()) {
                for (int i = 0; i < T.size; ++i) {
                    TreeNode t = (TreeNode) T.ptrs[i];
                    print(t, level + 1);
                }
            }
        }
    }

    /**
     * 二分查找key在结点中的位置
     *
     * @param T   结点
     * @param key 关键字
     * @return 位置（-1代表没找到）
     */
    private int indexOf(TreeNode T, K key) {
        int index = lowerBound(T, key);
        if (index >= T.size)
            index = -1;
        return index;
    }

    /**
     * 返回第一个>=key的位置
     *
     * @param T   结点
     * @param key 关键字
     * @return 位置
     */
    private int lowerBound(TreeNode T, K key) {
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
