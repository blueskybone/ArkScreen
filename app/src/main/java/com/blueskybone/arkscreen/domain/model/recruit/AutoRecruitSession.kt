package com.blueskybone.arkscreen.domain.model.recruit

/** One run; list equality intentionally includes tag order. */
class AutoRecruitSession {
    @Volatile var active = true
    private var lastTags: List<String>? = null
    fun isDuplicate(tags: List<String>) = lastTags == tags
    fun remember(tags: List<String>) { lastTags = tags.toList() }
}
