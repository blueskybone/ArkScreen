package com.blueskybone.arkscreen.ui.recruit

import com.blueskybone.arkscreen.domain.model.recruit.RecruitResult
import com.blueskybone.arkscreen.ui.UiStatus

data class RecruitUiState(
    val status: UiStatus = UiStatus.Idle,

    val selectedTags: List<String> = emptyList(),

    val result: List<RecruitResult> = emptyList(),
    val update: String = "",
    val newOpe: List<String> = emptyList()
)

sealed interface RecruitEvent {
    data class ShowError(val message: String) : RecruitEvent
}
