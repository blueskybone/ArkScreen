package com.example.arkscreen.database;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public abstract class Operator {

    @PrimaryKey(autoGenerate = true) public int id;

    @ColumnInfo(name = "name") public String opeName;
    @ColumnInfo(name = "class") public String opeClass;
    @ColumnInfo(name = "star") public String opeStar;
    @ColumnInfo(name = "pos") public String opePos;
    @ColumnInfo(name = "tag1") public String opeTag1;
    @ColumnInfo(name = "tag2") public String opeTag2;
    @ColumnInfo(name = "tag3") public String opeTag3;

}
