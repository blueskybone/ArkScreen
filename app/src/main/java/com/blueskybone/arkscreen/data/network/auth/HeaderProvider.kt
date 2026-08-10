package com.blueskybone.arkscreen.data.network.auth

import com.blueskybone.arkscreen.domain.service.AppClock

/**
 * Created by blueskybone
 * Date: 2026/2/26
 */
class HeaderProvider(
    private val clock: AppClock,
) {
    // 基础 Header，通常是固定的
    private fun createBaseHeaders(): Map<String, String> {
        return mapOf(
            "User-Agent" to "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0",
            "Content-Type" to "application/json",
            "Connection" to "close",
            "Content-Type" to "application/json"
        )
    }

    fun createCredHeaders(dId: String): Map<String, String> {
        return createBaseHeaders().toMutableMap().apply {
            put("dId", dId)
        }
    }

    fun createGrantHeaders(dId: String): Map<String, String>{
        return createBaseHeaders().toMutableMap().apply {
            put("dId", dId)
            put("platform", "3")
            put("vName", "1.0.0")
        }
    }

    fun createSignHeaders(
        api: String,
        cred: String,
        token: String,
        params: String,
        dId: String = ""
    ): Map<String, String>{
        val timestamp = clock.currentEpochSeconds().toString()
        val sign = calculateSign(api, params, token, timestamp, dId)
        return mapOf(
            "cred" to cred,
            "User-Agent" to "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0",
            "Connection" to "close",
            "Content-Type" to "application/json",
            "sign" to sign,
            "platform" to "",
            "timestamp" to timestamp,
            "dId" to dId,
            "vName" to ""
        )
    }

    fun createEfSignHeaders(
        api: String,
        cred: String,
        token: String,
        roleId: String,
        serverId: String,
        dId: String,
    ): Map<String, String>{
        return createSignHeaders(api, cred, token, "", dId).toMutableMap().apply {
            put("sk-game-role", "3_${roleId}_${serverId}")
            put("referer", "https://game.skland.com/")
            put("origin", "https://game.skland.com/")
        }
    }


    fun createAkHeader(
        cookie: String,
        token: String,
        xrToken: String
    ): Map<String, String> {
        val baseHeaders = mutableMapOf(
            "accept" to "application/json, text/plain, */*",
            "accept-language" to "zh-CN,zh;q=0.9,en;q=0.8,en-US;q=0.7,zh-TW;q=0.6",
            "cookie" to "ak-user-center=$cookie",
            "referer" to "https://ak.hypergryph.com/user/headhunting",
            "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36",
            "x-role-token" to xrToken
        )
        if (token != "null") {
            baseHeaders["x-account-token"] = token
        }
        return baseHeaders
    }

    private fun calculateSign(
        api: String,
        params: String,
        key: String,
        timeStamp: String,
        dId: String = ""
    ): String {
        val jsonArgs =
            "{\"platform\":\"\",\"timestamp\":\"$timeStamp\",\"dId\":\"$dId\",\"vName\":\"\"}"
        val data = api + params + timeStamp + jsonArgs
        val hmacData = hmacSha256(key, data)
        return hmacData.toMD5()
    }
}
