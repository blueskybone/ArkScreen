package com.blueskybone.arkscreen.ui.account.common

import android.view.View
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.CardAccountSelectorBinding
import com.blueskybone.arkscreen.domain.model.account.AccountSk

fun CardAccountSelectorBinding.bindAccount(account: AccountSk?) {
    CurrentAccount.text = account?.nickName ?: root.context.getString(R.string.skland_choose_account)
    Server.visibility = if (account == null) View.GONE else View.VISIBLE
    account?.let { Server.setText(if (it.official) R.string.official_server else R.string.bilibili_server) }
}
