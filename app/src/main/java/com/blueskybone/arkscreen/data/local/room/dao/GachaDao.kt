package com.blueskybone.arkscreen.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnoring(records: List<Gacha>): List<Long>

    /**
     * 第三方文件只能增量补充记录，不能覆盖已经由官方接口保存的数据。
     * Room 会在同一事务内完成整批插入；返回值为 -1 的项目表示命中了唯一索引。
     */
    @Transaction
    suspend fun importIgnoringConflicts(records: List<Gacha>): Int =
        insertIgnoring(records).count { rowId -> rowId != -1L }

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
