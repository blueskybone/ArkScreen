package com.blueskybone.arkscreen.data.network.auth

import com.blueskybone.arkscreen.data.network.ApiService
import com.blueskybone.arkscreen.data.network.model.CredRequest
import com.blueskybone.arkscreen.data.network.model.GrantRequest
import com.blueskybone.arkscreen.data.network.model.LoginRequest
import com.blueskybone.arkscreen.data.network.safeApiCall

data class SklandCredential(
    val cred: String,
    val token: String,
)

/**
 * 封装鹰角账号令牌到森空岛 cred 的完整远端鉴权流程。
 */
class SklandAuthRemoteDataSource(
    private val sklandApi: ApiService,
    private val accountApi: ApiService,
    private val headerProvider: HeaderProvider,
) {
    suspend fun fetchCredential(token: String, dId: String): SklandCredential {
        val grantResponse = safeApiCall(
            call = {
                val request = GrantRequest(
                    appCode = APP_CODE,
                    token = token,
                    type = 0,
                )
                accountApi.getGrant(request, headerProvider.createGrantHeaders(dId))
            },
            errorMessage = "获取grant授权码失败",
        )

        val credentialResponse = safeApiCall(
            call = {
                val request = CredRequest(code = grantResponse.data.code, kind = 1)
                sklandApi.generateCredByCode(
                    request,
                    headerProvider.createCredHeaders(dId),
                )
            },
            errorMessage = "获取cred凭证失败",
        )
        return SklandCredential(
            cred = credentialResponse.data.cred,
            token = credentialResponse.data.token,
        )
    }

    suspend fun loginByPassword(
        phone: String,
        password: String,
        dId: String,
    ): String {
        val response = safeApiCall(
            call = {
                accountApi.loginByPassword(
                    LoginRequest(phone, password),
                    headerProvider.createGrantHeaders(dId),
                )
            },
            errorMessage = "登录验证失败",
        )
        if (response.status != null && response.status != 0) {
            throw IllegalStateException(
                response.msg ?: response.message ?: "登录验证失败 (${response.status})"
            )
        }
        return response.data?.token?.takeIf(String::isNotBlank)
            ?: throw IllegalStateException(
                response.msg ?: response.message ?: "登录验证未返回 token"
            )
    }

    private companion object {
        const val APP_CODE = "4ca99fa6b56cc2ba"
    }
}
