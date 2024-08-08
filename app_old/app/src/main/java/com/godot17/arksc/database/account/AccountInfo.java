package com.godot17.arksc.database.account;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//sqlite 存储账号。以id auto或者uid作为唯一标识。
//cookie
// 手动录入。
@Entity
public class AccountInfo {

    @PrimaryKey
    public int id;
    @ColumnInfo(name = "nick_name")
    public String nickName;
    @ColumnInfo(name = "channel_name")
    public String channelName;
    @ColumnInfo(name = "channel_master_id")
    public String channelMasterId;
    @ColumnInfo(name = "uid")
    public String uid;
    @ColumnInfo(name = "user_info")
    public String userInfo;
    @ColumnInfo(name = "token")
    public String token;
}

