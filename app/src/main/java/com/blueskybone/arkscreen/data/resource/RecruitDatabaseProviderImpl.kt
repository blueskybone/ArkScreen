package com.blueskybone.arkscreen.data.resource

import com.blueskybone.arkscreen.domain.model.recruit.RecruitDatabase
import com.blueskybone.arkscreen.domain.repository.GameResourceRepository
import com.blueskybone.arkscreen.domain.service.RecruitDatabaseProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
class RecruitDatabaseProviderImpl(
    private val gameResourceRepository: GameResourceRepository
) : RecruitDatabaseProvider {

    private val mutex = Mutex()
    private var cachedDb: RecruitDatabase? = null

    override suspend fun getDatabase(): Result<RecruitDatabase> {
        cachedDb?.let { return Result.success(it) }

        return mutex.withLock {
            cachedDb?.let { return Result.success(it) }

            gameResourceRepository.getRecruitDb()
                .onSuccess { db ->
                    cachedDb = db
                }
        }
    }

    override fun clearCache() {
        cachedDb = null
    }
}