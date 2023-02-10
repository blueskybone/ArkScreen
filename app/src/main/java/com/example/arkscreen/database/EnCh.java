package com.example.arkscreen.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "en_to_ch")
public class EnCh {
    @PrimaryKey(autoGenerate = true) public int id;

    @ColumnInfo(name = "en_key") public String enKey;
    @ColumnInfo(name = "ch_key") public String chKey;
    public EnCh() {

    }
    @Ignore
    public EnCh(String enKey, String chKey) {
        this.enKey = enKey;
        this.chKey = chKey;
    }
}
