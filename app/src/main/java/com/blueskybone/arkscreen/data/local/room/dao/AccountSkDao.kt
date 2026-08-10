package com.blueskybone.arkscreen.data.local.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blueskybone.arkscreen.data.local.room.AccountSk
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM AccountSk")
    fun getAllFlowData(): Flow<List<AccountSk>>

    @Query("DELETE FROM AccountSk WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM AccountSk WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)

    @Query("SELECT * FROM AccountSk WHERE uid = :uid")
    fun getAccountFlowByUid(uid: String): Flow<AccountSk?>

    @Query("UPDATE accountsk SET channelMasterId = :channelMasterId, nickName = :nickName, token = :token,dId = :dId,official = :official WHERE uid = :uid")
    suspend fun updateAccount(
        uid: String,
        channelMasterId: String,
        nickName: String,
        token: String,
        dId: String,
        official: Boolean
    )
}