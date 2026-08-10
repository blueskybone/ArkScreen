package com.blueskybone.arkscreen.domain.service

import com.blueskybone.arkscreen.domain.model.recruit.RecruitDatabase

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
interface RecruitDatabaseProvider {

    suspend fun getDatabase(): Result<RecruitDatabase>

    fun clearCache()
}