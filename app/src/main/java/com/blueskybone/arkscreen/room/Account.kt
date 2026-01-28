package com.blueskybone.arkscreen.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */
//open class Account(
//    open val uid: String,
//    open val nickName: String,
//    open val token: String,
//    open val official: Boolean
//)

interface Account{
    val uid: String
    val nickName: String
    val token: String
    val official: Boolean
}

@Entity
data class AccountSk(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    override val uid: String,
    val channelMasterId: String,
    override val nickName: String,
    override val token: String,
    val dId: String,
    override val official: Boolean
) : Account {
    companion object {
        fun default(): AccountSk {
            return AccountSk(-1L, "", "", "", "", "", true)
        }
    }
}

@Entity
data class AccountGc(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    override val uid: String,
    val channelMasterId: Int,
    override val nickName: String,
    override val token: String,
    override val official: Boolean,
    var akUserCenter: String,
    var xrToken: String
) : Account {
    companion object {
        fun default(): AccountGc {
            return AccountGc(-1L, "", -1, "", "", true, "", "")
        }
    }
}

@Entity
data class AccountEf(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    override val uid: String,
    val channelMasterId: String,
    override val nickName: String,
    override val token: String,
    val dId: String,
    override val official: Boolean,
    val roleId: String,
    val serverId: String
): Account {
    companion object {
        fun default(): AccountEf {
            return AccountEf(-1L, "", "", "", "", "", true, "", "1")
        }
    }
}
