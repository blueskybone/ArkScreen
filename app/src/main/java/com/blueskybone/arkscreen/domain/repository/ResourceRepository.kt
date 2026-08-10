package com.blueskybone.arkscreen.domain.repository

import com.blueskybone.arkscreen.domain.model.DownloadStatus
import com.blueskybone.arkscreen.domain.model.AppUpdateInfo
import com.blueskybone.arkscreen.domain.model.BiliVideo
import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.model.recruit.RecruitDatabase
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.flow.Flow
import java.net.URL

/**
 * Created by blueskybone
 * Date: 2026/3/9
 */
interface ResourceRepository {

    suspend fun syncResource(type: ConfigType): Result<Unit>

    suspend fun getResourceDate(type: ConfigType): Result<String>

    suspend fun getRecruitDb(): Result<RecruitDatabase>

    suspend fun getI18nDb(): Result<Map<String, String>>

    suspend fun getCharInfoMap(): Result<JsonNode>

    suspend fun checkAppUpdate(): Result<AppUpdateInfo>

    suspend fun downloadFile(url: URL): Flow<DownloadStatus>

    suspend fun fetchAnnounce(): Result<String>

    suspend fun getBiliVideo(): Result<List<BiliVideo>>

}