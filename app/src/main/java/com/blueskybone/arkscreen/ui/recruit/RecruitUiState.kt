package com.blueskybone.arkscreen.ui.recruit

import com.blueskybone.arkscreen.domain.model.recruit.RecruitResult

data class RecruitUiState(
    val loading: Boolean = false,
    val error: String? = null,

    val selectedTags: List<String> = emptyList(),

    val result: List<RecruitResult> = emptyList(),
    val update: String = "",
    val newOpe: List<String> = emptyList()
)