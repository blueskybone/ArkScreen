package com.blueskybone.arkscreen.ui

/** 页面或操作的持久状态；一次性提示应通过 UI 事件传递。 */
sealed interface UiStatus {
    data object Idle : UiStatus
    data class Loading(val message: String? = null) : UiStatus
    data class Success(val message: String? = null) : UiStatus
    data class Empty(val message: String) : UiStatus
    data class Error(val message: String) : UiStatus
}
