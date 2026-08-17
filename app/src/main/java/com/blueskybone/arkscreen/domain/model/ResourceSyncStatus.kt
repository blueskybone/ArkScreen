package com.blueskybone.arkscreen.domain.model

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
sealed interface ResourceSyncStatus {

    data class Checking(
        val type: ConfigType,
    ) : ResourceSyncStatus

    data class UpToDate(
        val type: ConfigType,
    ) : ResourceSyncStatus

    data class Downloading(
        val type: ConfigType,
    ) : ResourceSyncStatus

    data class Updated(
        val type: ConfigType,
    ) : ResourceSyncStatus

    data class Failed(
        val type: ConfigType,
        val throwable: Throwable,
    ) : ResourceSyncStatus
}