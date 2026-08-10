package com.blueskybone.arkscreen.domain.repository

import com.blueskybone.arkscreen.domain.model.AppRemoteConfig

interface RemoteConfigRepository {
    suspend fun fetchAppConfig(): Result<AppRemoteConfig>
}
