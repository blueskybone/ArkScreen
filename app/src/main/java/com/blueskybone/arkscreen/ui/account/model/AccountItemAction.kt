package com.blueskybone.arkscreen.ui.account.model

import com.blueskybone.arkscreen.domain.model.account.Account

/**
 * Created by blueskybone
 * Date: 2026/4/5
 */
interface AccountItemAction {
    /*
    * 考虑替换成accountUI类
    * */
    fun onClick(account: Account)
    fun onLongClick(account: Account)
}