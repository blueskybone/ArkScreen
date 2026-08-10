package com.blueskybone.arkscreen.ui.main

import com.blueskybone.arkscreen.domain.model.BiliVideo
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.cache.ApCache
import com.blueskybone.arkscreen.domain.model.link.Link
import com.blueskybone.arkscreen.ui.account.model.AccountItemUiModel

/**
 * Created by blueskybone
 * Date: 2026/4/2
 */
data class MainUiState(
    val loading: Boolean = false,
    val announce: String = "",
    val biliVideos: List<BiliVideo> = emptyList(),
    val links: List<Link> = emptyList(),

    val accountSkList: List<AccountItemUiModel> = emptyList(),
    val accountEfList: List<AccountItemUiModel> = emptyList(),
    val currentAccountSk: AccountSk? = null,

    val apCache: ApCache? = null,

    val loginState: ActionState = ActionState.Idle,
    val attendanceState: ActionState = ActionState.Idle,
    val updateState: ActionState = ActionState.Idle,
)

sealed interface ActionState {
    data object Idle : ActionState
    data object Loading : ActionState
    data class Success(val message: String? = null) : ActionState
    data class Error(val message: String) : ActionState
}