package com.blueskybone.arkscreen.ui.gacha

import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.ui.gacha.model.GachaUiSnapshot
import com.blueskybone.arkscreen.ui.UiStatus

data class GachaUiState(
    val status: UiStatus = UiStatus.Idle,
    val currAccount: AccountGc? = null,
    val accList: List<AccountGc> = emptyList(),
    val gachaUiSnapshot: GachaUiSnapshot? = null,
    val selectedPoolId: String = "ALL",
)

sealed interface GachaEvent {
    data class ShowError(val message: String) : GachaEvent
    data class ShowMessage(val message: String) : GachaEvent
}
