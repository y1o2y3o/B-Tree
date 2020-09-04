package com.azure.rt01.rtree;


public class Result {
    RTNode pt;
    int index;
    boolean tag;

    public Result(RTNode pt, int index, boolean tag){
        this.pt = pt;
        this.index = index;
        this.tag = tag;
    }
}
