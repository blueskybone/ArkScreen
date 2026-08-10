package com.blueskybone.arkscreen.data.repository.utils

import com.blueskybone.arkscreen.data.network.ApiService
import com.blueskybone.arkscreen.data.network.auth.HeaderProvider
import com.blueskybone.arkscreen.data.network.model.CredRequest
import com.blueskybone.arkscreen.data.network.model.GrantRequest
import com.blueskybone.arkscreen.data.network.model.LoginRequest
import com.blueskybone.arkscreen.data.network.safeApiCall

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */

data class CredAndToken(val cred: String, val token: String)

//主要是鉴权这个部分在两个repo有重复。单独提出来
suspend fun fetchCredInfo(
    token: String,
    dId: String,
    headerProvider: HeaderProvider,
    api: ApiService,
    apiAs: ApiService,
): CredAndToken {

    val grantResp = safeApiCall(
        call = {
            val request = GrantRequest(appCode = "4ca99fa6b56cc2ba", token = token, type = 0)
            val headers = headerProvider.createGrantHeaders(dId)
            apiAs.getGrant(request, headers)
        },
        errorMessage = "获取grant授权码失败"
    )
    val grant = grantResp.data.code

    val credResp = safeApiCall(
        call = {
            val request = CredRequest(code = grant, kind = 1)
            val headers = headerProvider.createCredHeaders(dId)
            api.generateCredByCode(request, headers)
        },
        errorMessage = "获取cred凭证失败"
    )
    return CredAndToken(credResp.data.cred, credResp.data.token)
}

suspend fun fetchToken(
    phone: String,
    password: String,
    dId: String,
    headerProvider: HeaderProvider,
    apiAs: ApiService
): String {
    val resp = safeApiCall(
        call = {
            val request = LoginRequest(phone, password)
            val headers = headerProvider.createGrantHeaders(dId)
            apiAs.loginByPassword(
                request,
                headers
            )
        },
        errorMessage = "登录验证失败"
    )
    return resp.data.token
}
