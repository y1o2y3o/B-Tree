package com.azure.rbtree01;

import javax.swing.text.rtf.RTFEditorKit;
import java.util.Stack;

public class RBTree {
    RBTNode root = new RBTNode(Long.MIN_VALUE, RBTNode.BLACK);

    public boolean insert(long key) {
        Stack<RBTNode> path = new Stack<>();
        if (!insertNode(key, path)) return false;
        adjust(path);
        return true;
    }

    private static RBTNode singleRotateWithLeft(RBTNode T1) {
        RBTNode T2 = T1.left;
        T1.left = T2.right;
        T2.right = T1;
        return T2;
    }

    private static RBTNode singleRotateWithRight(RBTNode T1) {
        RBTNode T2 = T1.right;
        T1.right = T2.left;
        T2.left = T1;
        return T2;
    }

    private static RBTNode doubleRotateWithLeft(RBTNode T1) {
        RBTNode T2 = T1.left;
        T1.left = singleRotateWithRight(T2);
        return singleRotateWithLeft(T1);
    }

    private static RBTNode doubleRotateWithRight(RBTNode T1) {
        RBTNode T2 = T1.right;
        T1.right = singleRotateWithLeft(T2);
        return singleRotateWithRight(T1);
    }

    private RBTNode findNode(long key, Stack<RBTNode> path) {
        RBTNode p = root;
        while (p != RBTNode.NULLNODE && p.key != key) {
            path.push(p);
            if (key < p.key) p = p.left;
            else p = p.right;
        }
        return p;
    }

    private boolean insertNode(long key, Stack<RBTNode> path) {
        if (findNode(key, path) != RBTNode.NULLNODE) return false;
        RBTNode fa = path.peek();
        RBTNode node = new RBTNode(key, RBTNode.RED);
        if (key < fa.key) fa.left = node;
        else fa.right = node;
        path.push(node);
        return true;
    }

//    private static RBTNode rotate(RBTNode grandParent, long key) {
//        if (key < grandParent.key)
//            return key < grandParent.left.key ? singleRotateWithLeft(grandParent) : doubleRotateWithLeft(grandParent);
//        else
//            return key > grandParent.right.key ? singleRotateWithRight(grandParent) : doubleRotateWithRight(grandParent);
//
//    }

    private void adjust(Stack<RBTNode> path) {
        RBTNode X, parent, grandParent, uncle, ans;
        X = path.pop();
        parent = path.pop();
        while (X.color == RBTNode.RED && parent.color == RBTNode.RED) {
            grandParent = path.pop();
            if (parent.key < grandParent.key) {
                uncle = grandParent.right;
                ans = X.key < parent.key ? singleRotateWithLeft(grandParent) : doubleRotateWithLeft(grandParent);
                if (uncle.color == RBTNode.BLACK) {
                    ans.color = RBTNode.BLACK;
                    ans.right.color = RBTNode.RED;
                } else {
                    ans.left.color = RBTNode.BLACK;
                }
            } else {
                uncle = grandParent.left;
                ans = X.key > parent.key ? singleRotateWithRight(grandParent) : doubleRotateWithRight(grandParent);
                if (uncle.color == RBTNode.BLACK) {
                    ans.color = RBTNode.BLACK;
                    ans.left.color = RBTNode.RED;
                } else {
                    ans.right.color = RBTNode.BLACK;
                }
            }
            if (ans.key < path.peek().key) path.peek().left = ans;
            else path.peek().right = ans;

            X = ans;
            parent = path.pop();
        }
        root.right.color = RBTNode.BLACK;
    }


}
