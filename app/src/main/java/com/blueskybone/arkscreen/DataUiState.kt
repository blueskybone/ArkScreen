package com.blueskybone.arkscreen

/**
 *   Created by blueskybone
 *   Date: 2025/1/20
 */
sealed interface DataUiState {
    val msg: String

    data class Success(val message: String) : DataUiState {
        override val msg: String = message
    }

    data class Error(val message: String) : DataUiState {
        override val msg: String = message
    }

    data class Loading(val message: String) : DataUiState {
        override val msg: String = message
    }
}