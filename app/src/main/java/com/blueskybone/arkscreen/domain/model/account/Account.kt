package com.blueskybone.arkscreen.domain.model.account

/**
 * Created by blueskybone
 * Date: 2026/3/9
 */

enum class AccountType {
    SK, GC, EF
}

/**
 * 还是涉及了实现。这个可能没办法，因为前端就需要显式的知道cookie的构成。算了不动了
 * */
interface Account {
    val uid: String
    val nickName: String

    val official: Boolean
}

data class AccountSk(
    override val uid: String,
    override val nickName: String,
    val token: String,
    override val official: Boolean,
    val dId: String,
    val channelMasterId: String
): Account{
    fun geneCookieString(): String{
        return token
    }
}

data class AccountEf(
    override val uid: String,
    override val nickName: String,
    val token: String,
    override val official: Boolean,
    val dId: String,
    val channelMasterId: String,
    val roleId: String,
    val serverId: String
): Account

data class AccountGc(
    override val uid: String,
    override val nickName: String,
    val token: String,
    override val official: Boolean,
    val channelMasterId: Int,
    val akUserCenter: String,
    val xrToken: String
): Account{
    fun geneCookieString(): String{
        return "${token}@${akUserCenter}@${xrToken}"
    }
}