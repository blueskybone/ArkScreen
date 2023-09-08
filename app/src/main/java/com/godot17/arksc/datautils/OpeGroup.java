package com.godot17.arksc.datautils;

import static java.lang.Math.min;

import com.godot17.arksc.database.Operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpeGroup implements Comparable<OpeGroup> {

    private int star = 7;
    public List<String> tags = new ArrayList<>();
    public List<Operator> operators = new ArrayList<>();

    public void setTags(List<String> newTags) {
        tags.clear();
        tags = newTags;
    }

    public void addOperator(Operator operator) {
        operators.add(operator);
        sort();
        this.star = min(operator.getStar(), this.star);
    }

    public void sort() {
        Collections.sort(operators);
    }

    public int getStar() {
        return this.star;
    }

    public int getSize() {
        return operators.size();
    }

    @Override
    public int compareTo(OpeGroup o) {
        //star优先, tag数其次, 人数再次。
        if (this.star < o.star) {
            return 1;
        } else if (this.star > o.star) {
            return -1;
        } else {
            //6/5/1星时，人数优先，Tag数其次
            if (this.star == 6 || this.star == 1 || this.star == 5) {
                if (this.operators.size() > o.operators.size()) {
                    return 1;
                } else if (this.operators.size() < o.operators.size()) {
                    return -1;
                } else {
                    if (this.tags.size() > o.tags.size()) {
                        return 1;
                    } else return -1;
                }
            }
            if (this.tags.size() > o.tags.size()) {
                return 1;
            } else if (this.tags.size() < o.tags.size()) {
                return -1;
            } else {
                if (this.operators.size() > o.operators.size()) {
                    return 1;
                } else return -1;
            }
        }
    }
}
