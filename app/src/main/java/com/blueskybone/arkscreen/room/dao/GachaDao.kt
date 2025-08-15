package com.blueskybone.arkscreen.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blueskybone.arkscreen.room.Gacha

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */
@Dao
interface GachaDao {
    @Insert
    suspend fun insert(gacha: Gacha)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(records: List<Gacha>)

    @Query("SELECT * FROM Gacha")
    suspend fun getAll(): List<Gacha>

    @Query("DELETE FROM Gacha WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)

    @Query("SELECT * FROM Gacha WHERE uid = :uid")
    suspend fun getByUid(uid: String): List<Gacha>


}