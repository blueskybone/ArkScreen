package com.blueskybone.arkscreen.domain.repository

import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.account.AccountType
import kotlinx.coroutines.flow.Flow

/**
 * Created by blueskybone
 * Date: 2026/2/26
 */
interface AccountRepository {

    fun observeSkAcc(): Flow<List<AccountSk>>
    fun observeGcAcc(): Flow<List<AccountGc>>
    fun observeEfAcc(): Flow<List<AccountEf>>

    fun observeCurrentSkAcc(): Flow<AccountSk?>
    fun observeCurrentGcAcc(): Flow<AccountGc?>

    //手机密码登入（森空岛）
    suspend fun loginByPhonePassword(phone: String, code: String): Result<Int>

    //token登入（森空岛）
    suspend fun loginByToken(token: String): Result<Int>

    //token akUserCenter xrToken 登录官网
    suspend fun loginOfficialWeb(
        token: String,
        akUserCenter: String,
        xrToken: String,
        channelMasterId: Int
    ): Result<Int>

    // 从cookie导入账号
    suspend fun loadAccountFromCookie(accountType: AccountType, cookieStr: String): Result<Int>

    fun accountCookieEncode(account: Account): String

    fun setCurrentAccountGc(account: AccountGc): Result<Unit>

    fun setCurrentAccountSk(account: AccountSk): Result<Unit>

    suspend fun deleteAccount(account: Account): Result<Unit>
}