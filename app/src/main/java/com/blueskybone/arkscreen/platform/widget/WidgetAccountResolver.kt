package com.blueskybone.arkscreen.platform.widget

import com.blueskybone.arkscreen.data.local.room.WidgetConfig
import com.blueskybone.arkscreen.domain.model.account.AccountSk

object WidgetAccountResolver {
    fun resolve(config: WidgetConfig, accounts: List<AccountSk>, current: AccountSk?): AccountSk? {
        if (config.game != "arknights") return null
        return when (config.accountMode) {
            WidgetConfig.FOLLOW_CURRENT -> current?.let { selected ->
                accounts.firstOrNull { it.uid == selected.uid && it.channelMasterId == selected.channelMasterId }
            }
            WidgetConfig.FIXED -> accounts.firstOrNull {
                it.uid == config.roleUid && it.channelMasterId == config.serverKey
            }
            else -> null
        }
    }
}
