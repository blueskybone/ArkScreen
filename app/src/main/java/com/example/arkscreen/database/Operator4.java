package com.example.arkscreen.database;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "star_4_op")
public class Operator4 extends Operator{

    public Operator4()
    {

    }
    public Operator4(String opeName, String opeClass, String opeStar,
                    String opePos,String tag1,String tag2,String tag3) {
        this.opeName = opeName;
        this.opeClass = opeClass;
        this.opeStar = opeStar;
        this.opePos = opePos;
        this.opeTag1 = tag1;
        this.opeTag2 = tag2;
        this.opeTag3 = tag3;
    }
}
