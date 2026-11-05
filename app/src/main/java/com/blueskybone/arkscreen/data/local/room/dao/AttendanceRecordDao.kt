package com.blueskybone.arkscreen.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blueskybone.arkscreen.data.local.room.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceRecordDao {
    @Query(
        "SELECT * FROM AttendanceRecord " +
            "WHERE accountType = :accountType AND accountUid = :accountUid LIMIT 1"
    )
    suspend fun get(accountType: String, accountUid: String): AttendanceRecord?

    @Query(
        "SELECT * FROM AttendanceRecord " +
            "WHERE accountType = :accountType AND accountUid = :accountUid LIMIT 1"
    )
    fun observe(accountType: String, accountUid: String): Flow<AttendanceRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: AttendanceRecord)
}
