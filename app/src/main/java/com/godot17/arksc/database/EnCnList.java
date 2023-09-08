package com.godot17.arksc.database;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class EnCnList {
    @JsonProperty("en_cn_list")
    private List<EnCn> enCnList;

    public List<EnCn> getEnCnList() {
        return enCnList;
    }

    public void setEnCnList(List<EnCn> enCnList) {
        this.enCnList = enCnList;
    }
}
