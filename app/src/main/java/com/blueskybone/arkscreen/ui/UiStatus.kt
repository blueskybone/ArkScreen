package com.blueskybone.arkscreen.ui

/** Stable screen or action status. Transient messages belong in UI events. */
sealed interface UiStatus {
    data object Idle : UiStatus
    data class Loading(val message: String? = null) : UiStatus
    data class Success(val message: String? = null) : UiStatus
    data class Empty(val message: String) : UiStatus
    data class Error(val message: String) : UiStatus
}
