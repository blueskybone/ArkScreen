package com.blueskybone.arkscreen.domain.model

data class AppVersion(
    val versionCode: Int,
    val name: String,
    val buildTime: String = ""
)