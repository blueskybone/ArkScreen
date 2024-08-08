package com.godot17.arksc.database;

import com.godot17.arksc.datautils.OpeGroup;

import java.util.List;

public class Operator implements Comparable<Operator>{
    private String name;
    private int star;
    private List<String> tag;

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public void setStar(int star){
        this.star = star;
    }
    public int getStar(){
        return this.star;
    }
    public void setTag(List<String> tag){
        this.tag = tag;
    }
    public List<String> getTag(){
        return this.tag;
    }

    @Override
    public int compareTo(Operator o) {
        if(this.star > o.star){
            return 1;
        }
        else if(this.star < o.star) {
            return -1;
        }else return 1;
    }
}
