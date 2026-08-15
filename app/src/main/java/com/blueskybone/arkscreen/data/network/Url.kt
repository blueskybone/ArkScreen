package com.blueskybone.arkscreen.data.network

import com.blueskybone.arkscreen.APP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.TreeMap

/**
 *   Created by blueskybone
 *   Date: 2025/1/21
 */
const val avatarUrl = "https://web.hycdn.cn/arknights/game/assets/char_skin/avatar/"
const val portraitUrl = "https://web.hycdn.cn/arknights/game/assets/char_skin/portrait/"
val skinCachePath = "${APP.externalCacheDir}/skin_avatar"
const val equipUrl = "https://cdn.jsdelivr.net/gh/blueskybone/ArkScreenResource@master/equip/"
val equipCachePath = "${APP.externalCacheDir}/equip_icon"
const val skillUrl = "https://web.hycdn.cn/arknights/game/assets/char_skill/"
val skillCachePath = "${APP.externalCacheDir}/skill_icon"
