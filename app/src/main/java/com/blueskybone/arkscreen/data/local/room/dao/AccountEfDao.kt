package com.blueskybone.arkscreen.data.local.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.blueskybone.arkscreen.data.local.room.AccountEf
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM AccountEf")
    fun getAllFlowData(): Flow<List<AccountEf>>

    @Query("DELETE FROM AccountEf WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM AccountEf WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)

    @Query("DELETE FROM AccountEf WHERE uid = :uid AND id != (SELECT MIN(id) FROM AccountEf WHERE uid = :uid)")
    suspend fun deleteDuplicatesByUid(uid: String)

    @Query(
        """
        UPDATE AccountEf SET
            channelMasterId = :channelMasterId,
            nickName = :nickName,
            token = :token,
            dId = :dId,
            official = :official,
            roleId = :roleId,
            serverId = :serverId
        WHERE uid = :uid
        """
    )
    suspend fun updateAccount(
        uid: String,
        channelMasterId: String,
        nickName: String,
        token: String,
        dId: String,
        official: Boolean,
        roleId: String,
        serverId: String,
    ): Int

    @Transaction
    suspend fun upsert(account: AccountEf) {
        val updated = updateAccount(
            uid = account.uid,
            channelMasterId = account.channelMasterId,
            nickName = account.nickName,
            token = account.token,
            dId = account.dId,
            official = account.official,
            roleId = account.roleId,
            serverId = account.serverId,
        )
        if (updated == 0) insert(account)
        deleteDuplicatesByUid(account.uid)
    }

    @Transaction
    suspend fun upsert(accounts: List<AccountEf>) {
        accounts.forEach { upsert(it) }
    }
}
