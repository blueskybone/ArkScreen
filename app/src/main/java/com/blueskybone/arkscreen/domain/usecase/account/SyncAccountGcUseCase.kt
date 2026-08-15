package com.blueskybone.arkscreen.domain.usecase.account

import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import kotlinx.coroutines.flow.first

/**
 * Created by blueskybone
 * Date: 2026/2/26
 */
class SyncAccountGcUseCase(private val repo: AccountRepository) {

    sealed class LoginWay {
        data class Token(
            val token: String,
            val akUserCenter: String,
            val xrToken: String,
            val channelMasterId: Int
        ) : LoginWay()

        data class Cookie(val cookie: String) : LoginWay()
    }

    suspend operator fun invoke(way: LoginWay): Result<Int> {
        val loginResult = when (way) {
            is LoginWay.Cookie -> repo.loadAccountFromCookie(AccountType.GC, way.cookie)
            is LoginWay.Token -> repo.loginOfficialWeb(
                way.token,
                way.akUserCenter,
                way.xrToken,
                way.channelMasterId
            )
        }

        if (loginResult.isFailure) return loginResult

        if (repo.observeCurrentGcAcc().first() == null) {
            val firstAccount = repo.observeGcAcc().first().firstOrNull()
            if (firstAccount != null) {
                repo.setCurrentAccountGc(firstAccount)
                    .onFailure { return Result.failure(it) }
            }
        }

        return loginResult
    }
}
