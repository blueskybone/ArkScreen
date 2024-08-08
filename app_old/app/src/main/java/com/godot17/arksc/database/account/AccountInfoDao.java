package com.godot17.arksc.database.account;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AccountInfoDao {
    @Query("SELECT * FROM AccountInfo")
    List<AccountInfo> getAll();

    @Query("SELECT * FROM AccountInfo WHERE uid IN (:userIds)")
    List<AccountInfo> loadAllByIds(int[] userIds);

//    @Query("SELECT * FROM AccountInfo WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    AccountInfo findByName(String first, String last);

    @Insert
    void insertAll(AccountInfo... users);

    @Delete
    void delete(AccountInfo user);
}
