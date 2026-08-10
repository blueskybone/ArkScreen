package com.blueskybone.arkscreen.domain.model.link

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
data class Link(
    val id: Long?,
    val title: String,
    val url: String,
    val icon: String = ""
)