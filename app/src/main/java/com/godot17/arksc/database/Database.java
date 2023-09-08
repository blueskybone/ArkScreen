package com.godot17.arksc.database;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Database {
    @JsonProperty("update")
    private Update update;
    @JsonProperty("new_ope")
    private NewOpe newOpe;
    @JsonProperty("operator_list")
    private List<Operator> opeList;
    @JsonProperty("operator_low_list")
    private List<Operator> opeLowList;
    @JsonProperty("operator_high_list")
    private List<Operator> opeHighList;
    @JsonProperty("operator_robot_list")
    private List<Operator> opeRobotList;

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    public NewOpe getNewOpe() {
        return newOpe;
    }

    public void setNewOpe(NewOpe newOpe) {
        this.newOpe = newOpe;
    }

    public List<Operator> getOpeList() {
        return opeList;
    }

    public void setOpeList(List<Operator> opeList) {
        this.opeList = opeList;
    }

    public List<Operator> getOpeLowList() {
        return opeLowList;
    }

    public void setOpeLowList(List<Operator> opeLowList) {
        this.opeLowList = opeLowList;
    }

    public List<Operator> getOpeHighList() {
        return opeHighList;
    }

    public void setOpeHighList(List<Operator> opeHighList) {
        this.opeHighList = opeHighList;
    }

    public List<Operator> getOpeRobotList() { return opeRobotList; }

    public void setOpeRobotList(List<Operator> opeRobotList) {
        this.opeRobotList = opeRobotList;
    }
}
