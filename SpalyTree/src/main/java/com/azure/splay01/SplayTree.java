package com.azure.splay01;

import java.util.Collections;
import java.util.Random;
import java.util.Stack;

public class SplayTree<K extends Comparable<K>> {
    SplayNode root;

    public SplayTree() {
        root = null;
    }

    public void insert(K key) {
        root = insert(key, root);
    }

    public void delete(K key){
        root = delete(key, root);
    }

    public SplayNode search(K key){
        root = access(key, root);
        return root;
    }

    public void printInorder(){
        System.out.println("[");
        printInorder(root);
        System.out.println("\n]");
    }

    private void printInorder(SplayNode T){
        if(T != null){
            printInorder(T.left);
            System.out.print(" "+T.key);
            printInorder(T.right);
        }
    }

    private SplayNode singleRotateWithLeft(SplayNode T1) {
        SplayNode T2 = T1.left;
        T1.left = T2.right;
        T2.right = T1;
        return T2;
    }

    private SplayNode singleRotateWithRight(SplayNode T1) {
        SplayNode T2 = T1.right;
        T1.right = T2.left;
        T2.left = T1;
        return T2;
    }

    private SplayNode doubleRotateWithLeft(SplayNode T1) {
        SplayNode T2 = T1.left;
        T1.left = singleRotateWithRight(T2);
        return singleRotateWithLeft(T1);
    }

    private SplayNode doubleRotateWithRight(SplayNode T1) {
        SplayNode T2 = T1.right;
        T1.right = singleRotateWithLeft(T2);
        return singleRotateWithRight(T1);
    }

    private SplayNode zigzagRotateWithLeft(SplayNode T1) {
        return doubleRotateWithLeft(T1);
    }

    private SplayNode zigzagRotateWithRight(SplayNode T1) {
        return doubleRotateWithRight(T1);
    }

    private SplayNode zigzigRotateWithLeft(SplayNode T1) {
        SplayNode T2 = singleRotateWithLeft(T1);
        return singleRotateWithLeft(T2);
    }

    private SplayNode zigzigRotateWithRight(SplayNode T1) {
        SplayNode T2 = singleRotateWithRight(T1);
        return singleRotateWithRight(T2);
    }

    // splay并返回新的根（path可以为空）
    private SplayNode splay(Stack<SplayNode> path) {
        SplayNode newRoot = null;
        if (path != null && !path.empty()) {
            SplayNode curNode, parent, grandparent;

            curNode = path.pop();
            while (!path.empty()) {
                parent = path.pop();
                if (!path.empty()) {
                    grandparent = path.pop();
                    if (parent == grandparent.left) {
                        curNode = (curNode == parent.left ? zigzigRotateWithLeft(grandparent) : zigzagRotateWithLeft(grandparent));
                    } else {
                        curNode = (curNode == parent.right ? zigzigRotateWithRight(grandparent) : zigzagRotateWithRight(grandparent));
                    }
                    if (!path.empty()) {
                        if (grandparent == path.peek().left) {
                            path.peek().left = curNode;
                        } else {
                            path.peek().right = curNode;
                        }
                    }
                } else {
                    curNode = (curNode == parent.left ? singleRotateWithLeft(parent) : singleRotateWithRight(parent));
                }
            }
            newRoot = curNode;
        }
        return newRoot;
    }

    // 寻找T上的结点,返回路径（不包括该查找结点）
    @SuppressWarnings("unchecked")
    private SplayNode findNode(K key, SplayNode T, Stack<SplayNode> path) {
        SplayNode curNode = T;
        while (curNode != null && !(key.compareTo((K) (curNode.key)) == 0)) {
            path.push(curNode);
            curNode = (key.compareTo((K) (curNode.key)) < 0 ? curNode.left : curNode.right);
        }
        return curNode;
    }

    // 插入, 返回新的根
    @SuppressWarnings("unchecked")
    private SplayNode insert(K key, SplayNode T) {
        Stack<SplayNode> path = new Stack<>();
        SplayNode node = findNode(key, T, path);
        if (node == null) { // 没找到,插入
            SplayNode newNode = new SplayNode(key);
            if (!path.empty()) {
                if (key.compareTo((K) (path.peek().key)) < 0) path.peek().left = newNode;
                else path.peek().right = newNode;
            }
            path.push(newNode);
        } else { // 找到了, 不插入
            path.push(node);
        }
        return splay(path); // splay并返回新的根
    }

    // 访问最大的结点,返回新的根
    private SplayNode accessMax(SplayNode T) {
        SplayNode curNode = T;
        Stack<SplayNode> path = new Stack<>();
        while (curNode != null) {
            path.push(curNode);
            curNode = curNode.right;
        }
        return splay(path);
    }

    // 访问某结点,返回新的根
    public SplayNode access(K key, SplayNode T) {
        Stack<SplayNode> path = new Stack<>();
        SplayNode node = findNode(key, T, path);
        if (node != null) path.push(node);
        return splay(path);
    }

    // 删除结点,返回新的根
    @SuppressWarnings("unchecked")
    private SplayNode delete(K key, SplayNode T) {
        SplayNode delNode, TL, TR, newRoot;

        delNode = access(key, T);
        if (delNode != null && key.compareTo((K) (delNode.key)) == 0) { // 执行删除
            TL = delNode.left;
            TR = delNode.right;
            if (TL != null) {
                newRoot = accessMax(TL);
                newRoot.right = TR;
            } else {
                newRoot = TR;
            }
        } else {
            newRoot = delNode;
        }
        return newRoot;
    }

    public static void main(String[] args) {
        SplayTree<Integer> tree = new SplayTree<>();
        Integer[] arr = new Integer[1000];
        for (int i = 0; i < arr.length; ++i) arr[i] = i;
        shuffle(arr, arr.length);
        long start = System.nanoTime();
        for (Integer d : arr) tree.insert(d);
        long end = System.nanoTime();
        System.out.println("insert用时:" + ((end - start) / 1000000) + "ms");

        shuffle(arr, arr.length);
        long start2 = System.nanoTime();
        for (int i = 0; i < arr.length/2;++i) tree.delete(arr[i]);
        long end2 = System.nanoTime();
        System.out.println("delete用时:" + ((end2 - start2) / 1000000) + "ms");
        tree.printInorder();
    }

    private static void shuffle(Object[] arr, int size) {
        Random rand = new Random();
        for (int i = 0; i < size; ++i) {
            int j = rand.nextInt(i + 1);
            Object t = arr[i];
            arr[i] = arr[j];
            arr[j] = t;
        }
    }
}
