package com.blueskybone.arkscreen.domain.usecase.account

import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.repository.AccountRepository

/**
 * Created by blueskybone
 * Date: 2026/2/26
 */
class SyncAccountSkUseCase(private val repo: AccountRepository) {

    sealed class LoginWay {
        data class PhoneAndPassword(val phone: String, val password: String) : LoginWay()
        data class Token(val token: String) : LoginWay()
        data class Cookie(val cookieStr: String) : LoginWay()
    }

    suspend operator fun invoke(way: LoginWay): Result<Int> =
        when (way) {
            is LoginWay.PhoneAndPassword -> repo.loginByPhonePassword(way.phone, way.password)
            is LoginWay.Token -> repo.loginByToken(way.token)
            is LoginWay.Cookie -> repo.loadAccountFromCookie(AccountType.SK, way.cookieStr)
        }

}