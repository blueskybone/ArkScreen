package com.blueskybone.arkscreen.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blueskybone.arkscreen.data.local.room.AttendanceRecord

@Dao
interface AttendanceRecordDao {
    @Query(
        "SELECT * FROM AttendanceRecord " +
            "WHERE accountType = :accountType AND accountUid = :accountUid LIMIT 1"
    )
    suspend fun get(accountType: String, accountUid: String): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: AttendanceRecord)
}
