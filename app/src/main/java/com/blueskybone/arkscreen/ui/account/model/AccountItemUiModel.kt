package com.blueskybone.arkscreen.ui.account.model

import com.blueskybone.arkscreen.domain.model.account.Account

/**
 * Created by blueskybone
 * Date: 2026/4/5
 */
data class AccountItemUiModel(
    val account: Account,
    val isDefault: Boolean
)