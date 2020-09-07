package com.azure.rbtree01;

public class RBTNode {
    static final int BLACK = 0;
    static final int RED = 1;
    static final RBTNode NULLNODE;

    static {
        NULLNODE = new RBTNode();
        NULLNODE.key = Long.MAX_VALUE;
        NULLNODE.color = BLACK;
        NULLNODE.left = NULLNODE.right = NULLNODE;
    }

    long key;
    RBTNode left = NULLNODE;
    RBTNode right = NULLNODE;
    int color;

    public RBTNode() {
    }

    public RBTNode(long key, int color) {
        this.key = key;
        this.color = color;
    }

    @Override
    public String toString() {
        if(this == NULLNODE) return "NULL--"+(color == RED ? "RED" : "BLACK");
        return String.format("%d--%s", key, color == RED ? "RED" : "BLACK");
    }
}
