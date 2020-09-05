package com.azure.rt01.rtree;

public class SearchResult {
    public Rectangle key;
    public long value;

    public SearchResult(Rectangle key, long value){
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return  key.toString();
    }
}
