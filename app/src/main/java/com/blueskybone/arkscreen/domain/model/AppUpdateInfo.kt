package com.blueskybone.arkscreen.domain.model

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */

/**
 * data层直接引用这个，checkupdate的逻辑暴露在usecase里。
 * */
data class AppUpdateInfo(
    var versionCode: Float = 0F,
    var version: String = "",
    var date: String = "",
    var content: String = "",
    var link: String = ""
)