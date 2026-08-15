package com.blueskybone.arkscreen.ui.main

import com.blueskybone.arkscreen.domain.model.BiliVideo
import com.blueskybone.arkscreen.domain.model.AppRemoteConfig
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.cache.ApCache
import com.blueskybone.arkscreen.domain.model.cache.CacheAccountInfo
import com.blueskybone.arkscreen.domain.model.link.Link
import com.blueskybone.arkscreen.ui.account.model.AccountItemUiModel
import com.blueskybone.arkscreen.ui.UiStatus

/**
 * Created by blueskybone
 * Date: 2026/4/2
 */
data class MainUiState(
    val status: UiStatus = UiStatus.Idle,
    val announce: String = "",
    val biliVideos: List<BiliVideo> = emptyList(),
    val appRemoteConfig: AppRemoteConfig = AppRemoteConfig.Default,
    val links: List<Link> = emptyList(),

    val accountSkList: List<AccountItemUiModel> = emptyList(),
    val accountEfList: List<AccountItemUiModel> = emptyList(),
    val currentAccountSk: AccountSk? = null,

    val apCache: ApCache? = null,
    val cacheAccountInfo: CacheAccountInfo? = null,

    val loginStatus: UiStatus = UiStatus.Idle,
    val pendingUpdate: PendingAppUpdate? = null,
    val downloadState: AppDownloadUiState = AppDownloadUiState.Idle,
)

data class PendingAppUpdate(
    val version: String,
    val versionCode: Long,
    val changelog: String,
    val url: String,
)

sealed interface AppDownloadUiState {
    data object Idle : AppDownloadUiState
    data class Downloading(val percent: Int?) : AppDownloadUiState
    data class Completed(val filePath: String) : AppDownloadUiState
    data class Failed(val message: String) : AppDownloadUiState
}
