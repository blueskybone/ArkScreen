package com.blueskybone.arkscreen.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.blueskybone.arkscreen.room.AccountGc

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */
@Dao
interface AccountGcDao {
    @Insert
    suspend fun insert(account: AccountGc)

    @Query("SELECT * FROM AccountGc")
    suspend fun getAll(): List<AccountGc>

    @Query("DELETE FROM AccountGc WHERE id = :id")
    suspend fun delete(id: Long)

}