package com.blueskybone.arkscreen.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager

fun openLink(context: Context, url: String, prefManager: SettingPrefManager) {
    val intent = if (prefManager.useInnerWeb.get()) {
        Intent(context, WebViewActivity::class.java).putExtra("url", url)
    } else {
        Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }
    context.startActivity(intent)
}
