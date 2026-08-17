package com.blueskybone.arkscreen.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.blueskybone.arkscreen.data.local.room.Gacha
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM Gacha WHERE uid = :uid")
    fun getFLowByUid(uid: String): Flow<List<Gacha>>

    @Query("DELETE FROM Gacha WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)

    @Query("SELECT * FROM Gacha WHERE uid = :uid")
    suspend fun getByUid(uid: String): List<Gacha>

    @Query("SELECT * FROM Gacha WHERE poolCate = :cate")
    suspend fun getByCate(cate: String): List<Gacha>

    @Update
    suspend fun updateGacha(gacha: Gacha)

    @Update
    suspend fun updateGachas(gachas: List<Gacha>)
}