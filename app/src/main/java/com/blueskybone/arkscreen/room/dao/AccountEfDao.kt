package com.blueskybone.arkscreen.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blueskybone.arkscreen.room.AccountEf

/**
 *   Created by blueskybone
 *   Date: 2026/1/28
 */
@Dao
interface AccountEfDao {
    @Insert
    suspend fun insert(account: AccountEf)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(accounts: List<AccountEf>)

    @Query("SELECT * FROM AccountEf")
    suspend fun getAll(): List<AccountEf>

    @Query("SELECT * FROM AccountEf")
    fun getAllLiveData(): LiveData<List<AccountEf>>

    @Query("DELETE FROM AccountEf WHERE id = :id")
    suspend fun delete(id: Long)
}