package com.blueskybone.arkscreen.data.local.room

import androidx.room.Entity

@Entity(tableName = "RealtimeCache", primaryKeys = ["game", "serverKey", "roleUid"])
data class RealtimeCacheEntity(
    val game: String,
    val serverKey: String,
    val roleUid: String,
    val syncedAt: Long,
    val dataTimestamp: Long,
    val schemaVersion: Int = 1,
    val payload: String,
)
