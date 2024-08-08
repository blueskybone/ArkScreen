package com.godot17.arksc.datautils;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FinalOpeList {
    public String[] tags;
    public int star = -1;
    public final List<OpeGroup> opeGroups = new ArrayList<>();

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public int getStar() {
        if (opeGroups.size() < 1) return 0;
        this.sort();
        star = opeGroups.get(0).getStar();
        return star;
    }

    public void addOpeGroup(OpeGroup opeGroup) {
        if(opeGroup.getSize() < 1) return;
        opeGroups.add(opeGroup);
        star = max(this.star, opeGroup.getStar());
        this.sort();
    }

    private void sort() {
        if (opeGroups.size() < 1) return;
        Collections.sort(opeGroups);
    }
}
