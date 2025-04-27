package com.blueskybone.arkscreen.network

import com.blueskybone.arkscreen.APP

/**
 *   Created by blueskybone
 *   Date: 2025/1/21
 */

enum class RequestMethod { GET, POST }
data class Response(var responseCode: Int, var responseContent: String)

const val avatarUrl = "https://web.hycdn.cn/arknights/game/assets/char_skin/avatar/"
val skinCachePath = "${APP.externalCacheDir}/skin_avatar"

const val equipUrl = "https://gitee.com/blueskybone/ArknightsGameResource/raw/master/equip/"
val equipCachePath = "${APP.externalCacheDir}/equip_icon"

const val skillUrl = "https://gitee.com/blueskybone/ArknightsGameResource/raw/master/skill/"
val skillCachePath = "${APP.externalCacheDir}/skill_icon"

const val biliSettingUrl = "https://space.bilibili.com/ajax/settings/getSettings?mid=161775300"
const val titleImageUrl = "https://i0.hdslb.com/"

