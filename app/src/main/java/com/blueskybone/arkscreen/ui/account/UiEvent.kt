package com.blueskybone.arkscreen.ui.account

/**
 * Created by blueskybone
 * Date: 2026/4/5
 */
sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
    data class ShowError(val message: String) : UiEvent
}