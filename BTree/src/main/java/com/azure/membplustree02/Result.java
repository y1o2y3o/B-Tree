package com.azure.membplustree02;


public class Result {
    BTNode pt;
    int index;
    boolean tag;

    public Result(BTNode pt, int index, boolean tag){
        this.pt = pt;
        this.index = index;
        this.tag = tag;
    }
}
