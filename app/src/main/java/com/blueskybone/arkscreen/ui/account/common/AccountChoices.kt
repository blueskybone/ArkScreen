package com.blueskybone.arkscreen.ui.account.common

import android.view.View
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.ui.common.showChoicePopup

fun <T : Account> showAccountChoices(
    anchor: View,
    accounts: List<T>,
    selectedUid: String?,
    onSelect: (T) -> Unit,
): androidx.appcompat.widget.ListPopupWindow {
    val labels = accounts.map { account ->
        if (account !is AccountEf) {
            val server = if (account.official) R.string.official_server else R.string.bilibili_server
            "${account.nickName} · ${anchor.context.getString(server)}"
        } else {
            account.nickName
        }
    }
    return showChoicePopup(anchor, labels, accounts.indexOfFirst { it.uid == selectedUid }) { index ->
        onSelect(accounts[index])
    }
}
