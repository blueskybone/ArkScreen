package com.blueskybone.arkscreen.domain.model

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
data class ResourceUpdateInfo(
    var versionCode: Long = 0L,
    var version: String = "",
    var date: String = "",
    var content: String = "",
    var link: String = ""
)
