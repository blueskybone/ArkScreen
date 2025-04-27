package com.blueskybone.arkscreen.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blueskybone.arkscreen.room.AccountSk

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */
@Dao
interface AccountSkDao {
    @Insert
    suspend fun insert(account: AccountSk)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(accounts: List<AccountSk>)

    @Query("SELECT * FROM AccountSk")
    suspend fun getAll(): List<AccountSk>

    @Query("SELECT * FROM AccountSk")
    fun getAllLiveData(): LiveData<List<AccountSk>>

    @Query("DELETE FROM AccountSk WHERE id = :id")
    suspend fun delete(id: Long)
}