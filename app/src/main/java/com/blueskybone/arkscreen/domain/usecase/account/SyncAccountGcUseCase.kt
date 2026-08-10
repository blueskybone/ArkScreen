package com.blueskybone.arkscreen.domain.usecase.account

import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.repository.AccountRepository

/**
 * Created by blueskybone
 * Date: 2026/2/26
 */
class SyncAccountGcUseCase(private val repo: AccountRepository) {

    //TODO: 想一下cookie类怎么做;解析应该还是在UI层完成。 需要定义一个Cookie类吗？
    sealed class LoginWay {
        data class Token(
            val token: String,
            val akUserCenter: String,
            val xrToken: String,
            val channelMasterId: Int
        ) : LoginWay()

        data class Cookie(val cookie: String) : LoginWay()
    }

    suspend operator fun invoke(way: LoginWay): Result<Int> = when (way) {
        is LoginWay.Cookie -> repo.loadAccountFromCookie(AccountType.GC, way.cookie)
        is LoginWay.Token -> repo.loginOfficialWeb(
            way.token,
            way.akUserCenter,
            way.xrToken,
            way.channelMasterId
        )
    }
}