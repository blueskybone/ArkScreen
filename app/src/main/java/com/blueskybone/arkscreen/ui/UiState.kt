package com.blueskybone.arkscreen.ui

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    object Cancelled : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
    data class Warning(val message: String): UiState()
}