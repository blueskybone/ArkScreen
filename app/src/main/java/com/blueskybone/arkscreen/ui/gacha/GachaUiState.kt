package com.blueskybone.arkscreen.ui.gacha

import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.ui.gacha.model.GachaUiSnapshot

data class GachaUiState(
    val loading: Boolean = false,
    val syncing: Boolean = false,
    val warning: String? = null,
    val error: String? = null,
    val currAccount: AccountGc? = null,
    val accList: List<AccountGc> = emptyList(),
    val gachaUiSnapshot: GachaUiSnapshot? = null,
    val selectedPoolId: String = "ALL",
)