package com.blueskybone.arkscreen.ui.main

/**
 * Created by blueskybone
 * Date: 2026/4/2
 */
sealed interface MainEvent {
    data class ShowUpdateDialog(
        val version: String,
        val changelog: String,
        val url: String,
    ) : MainEvent

    data class StartAppDownload(val url: String) : MainEvent
    data class ShowToast(val message: String) : MainEvent
    data class ShowError(val message: String) : MainEvent
}