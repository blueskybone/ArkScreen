package com.blueskybone.arkscreen.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */

@Entity
data class AccountSk(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String,
    val channelMasterId: String,
    val nickName: String,
    val token: String,
    val dId: String,
    val official: Boolean
) {
    companion object {
        fun default(): AccountSk {
            return AccountSk(-1L, "", "", "", "", "", true)
        }
    }
}

@Entity
data class AccountGc(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String,
    val channelMasterId: Int,
    val nickName: String,
    val token: String,
    val official: Boolean,
    val akUserCenter: String,
    val xrToken: String
) {
    companion object {
        fun default(): AccountGc {
            return AccountGc(-1L, "", -1, "", "", true, "", "")
        }
    }
}

@Entity
data class AccountEf(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String,
    val channelMasterId: String,
    val nickName: String,
    val token: String,
    val dId: String,
    val official: Boolean,
    val roleId: String,
    val serverId: String
){
    companion object {
        fun default(): AccountEf {
            return AccountEf(-1L, "", "", "", "", "", true, "", "1")
        }
    }
}
