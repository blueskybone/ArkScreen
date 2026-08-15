package com.blueskybone.arkscreen.data.local.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.blueskybone.arkscreen.data.local.room.AccountGc
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM AccountGc")
    fun getAllLiveData(): LiveData<List<AccountGc>>

    @Query("SELECT * FROM AccountGc")
    fun getAllFlowData(): Flow<List<AccountGc>>

    @Query("DELETE FROM AccountGc WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM AccountGc WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)

    @Query("DELETE FROM AccountGc WHERE uid = :uid AND id != (SELECT MIN(id) FROM AccountGc WHERE uid = :uid)")
    suspend fun deleteDuplicatesByUid(uid: String)

    @Query("SELECT * FROM AccountGc WHERE uid = :uid")
    fun getAccountFlowByUid(uid: String): Flow<AccountGc?>

    @Query("UPDATE accountgc SET channelMasterId = :channelMasterId, nickName = :nickName, token = :token,official = :official,akUserCenter = :akUserCenter,xrToken = :xrToken WHERE uid = :uid")
    suspend fun updateAccount(
        uid: String,
        channelMasterId: Int,
        nickName: String,
        token: String,
        official: Boolean,
        akUserCenter: String,
        xrToken: String
    ): Int

    @Transaction
    suspend fun upsert(account: AccountGc) {
        val updated = updateAccount(
            uid = account.uid,
            channelMasterId = account.channelMasterId,
            nickName = account.nickName,
            token = account.token,
            official = account.official,
            akUserCenter = account.akUserCenter,
            xrToken = account.xrToken,
        )
        if (updated == 0) insert(account)
        deleteDuplicatesByUid(account.uid)
    }
}
