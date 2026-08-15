package com.blueskybone.arkscreen.domain.repository

import com.blueskybone.arkscreen.domain.model.BiliVideo

interface HomeContentRepository {
    suspend fun fetchAnnouncement(): Result<String>
    suspend fun fetchBiliVideos(): Result<List<BiliVideo>>
}
