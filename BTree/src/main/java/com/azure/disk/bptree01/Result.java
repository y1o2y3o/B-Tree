package com.azure.disk.bptree01;


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
