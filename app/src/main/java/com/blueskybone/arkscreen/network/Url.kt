package com.blueskybone.arkscreen.network

import com.blueskybone.arkscreen.APP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.net.URL

/**
 *   Created by blueskybone
 *   Date: 2025/1/21
 */

enum class RequestMethod { GET, POST }
data class Response(var responseCode: Int, var responseContent: String)
data class CredAndToken(val cred: String, val token: String)
const val avatarUrl = "https://web.hycdn.cn/arknights/game/assets/char_skin/avatar/"
val skinCachePath = "${APP.externalCacheDir}/skin_avatar"

//const val equipUrl = "https://gitee.com/blueskybone/ArknightsGameResource/raw/master/equip/"
const val equipUrl = "https://cdn.jsdelivr.net/gh/blueskybone/ArkScreenResource@master/equip/"
val equipCachePath = "${APP.externalCacheDir}/equip_icon"

//const val skillUrl = "https://gitee.com/blueskybone/ArknightsGameResource/raw/master/skill/"
//const val skillUrl = "https://cdn.jsdelivr.net/gh/blueskybone/ArkScreenResource@master/skill/"
const val skillUrl = "https://web.hycdn.cn/arknights/game/assets/char_skill/"
val skillCachePath = "${APP.externalCacheDir}/skill_icon"

const val biliSettingUrl = "https://space.bilibili.com/ajax/settings/getSettings?mid=161775300"
const val titleImageUrl = "https://i0.hdslb.com/"

//const val biliUserInfoApi = "https://api.bilibili.com/x/web-interface/card?mid=161775300&photo=true"
//const val videoInfoApi = "https://api.bilibili.com/x/web-interface/wbi/view?bvid=BV1zaETzJEcj"
/*
* 需要在原本的参数基础上加上wbi w_rid=e6955dba2297a749a7ee9162078f972c  &wts=1747409727，
* https://github.com/SocialSisterYi/bilibili-API-collect/blob/3cd6dfbd367bd710a5b3be906ab6bba8787ac71f/docs/misc/sign/wbi.md#kotlin
* */

const val announceUrl = "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/announce.json"
const val poolTypeUrl = "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/pool_type.json"

suspend fun makeSuspendRequest(url: URL): String {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()
        try {
            val response = client.newCall(request).execute()
            response.use {
                if (!it.isSuccessful) throw IOException("Unexpected code: ${it.code}")
                it.body?.string() ?: throw IOException("Empty response")
            }
        } catch (e: Exception) {
            throw e
        }
    }
}

suspend fun downloadFile(url: String, savePath: String): Boolean {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("!response.isSuccessful")
                response.body?.let { body ->
                    File(savePath).outputStream().use { output ->
                        body.byteStream().copyTo(output)
                    }
                }
                true
            }
        } catch (e: Exception) {
            false
        }
    }

}