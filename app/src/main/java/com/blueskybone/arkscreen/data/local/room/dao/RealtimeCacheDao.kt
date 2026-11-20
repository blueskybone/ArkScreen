package com.blueskybone.arkscreen.data.local.room.dao

import androidx.room.*
import com.blueskybone.arkscreen.data.local.room.RealtimeCacheEntity

@Dao
interface RealtimeCacheDao {
    @Query("SELECT * FROM RealtimeCache WHERE game = :game AND serverKey = :server AND roleUid = :uid")
    suspend fun get(game: String, server: String, uid: String): RealtimeCacheEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(value: RealtimeCacheEntity)
    @Transaction
    suspend fun putIfNewer(value: RealtimeCacheEntity) {
        val previous = get(value.game, value.serverKey, value.roleUid)
        if (previous == null || value.dataTimestamp >= previous.dataTimestamp) put(value)
    }
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun importLegacy(value: RealtimeCacheEntity)
    @Query("DELETE FROM RealtimeCache WHERE game = :game AND serverKey = :server AND roleUid = :uid")
    suspend fun delete(game: String, server: String, uid: String)
}
