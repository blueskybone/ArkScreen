package com.blueskybone.arkscreen.domain.repository

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */

import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.model.I18nTranslations
import com.blueskybone.arkscreen.domain.model.ResourceSyncStatus
import com.blueskybone.arkscreen.domain.model.operator.OperatorBasicInfo
import com.blueskybone.arkscreen.domain.model.gacha.GachaPoolCatalog
import com.blueskybone.arkscreen.domain.model.recruit.RecruitDatabase
import kotlinx.coroutines.flow.Flow

interface GameResourceRepository {

    fun syncResource(type: ConfigType): Flow<ResourceSyncStatus>

    suspend fun getRecruitDb(): Result<RecruitDatabase>

    suspend fun getI18nTranslations(): Result<I18nTranslations>

    suspend fun getOperatorBasicInfoMap(): Result<Map<String, OperatorBasicInfo>>

    suspend fun getGachaPoolCatalog(): Result<GachaPoolCatalog>

    suspend fun getResourceDate(type: ConfigType): Result<String>

    fun clearResourceCache(type: ConfigType)
}
