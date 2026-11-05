package com.blueskybone.arkscreen.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blueskybone.arkscreen.data.local.room.EndfieldGachaRecord
import com.blueskybone.arkscreen.data.local.room.EndfieldGachaPool
import kotlinx.coroutines.flow.Flow

@Dao
interface EndfieldGachaDao {
    @Query("SELECT * FROM EndfieldGachaRecord WHERE roleId = :roleId")
    fun observe(roleId: String): Flow<List<EndfieldGachaRecord>>

    @Query("SELECT * FROM EndfieldGachaRecord WHERE roleId = :roleId")
    suspend fun getAll(roleId: String): List<EndfieldGachaRecord>

    @Query("SELECT * FROM EndfieldGachaPool WHERE roleId = :roleId")
    fun observePools(roleId: String): Flow<List<EndfieldGachaPool>>

    @Query("SELECT * FROM EndfieldGachaPool WHERE roleId = :roleId")
    suspend fun getPools(roleId: String): List<EndfieldGachaPool>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(records: List<EndfieldGachaRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPools(pools: List<EndfieldGachaPool>)
}
