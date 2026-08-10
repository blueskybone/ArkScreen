package com.blueskybone.arkscreen.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.model.link.LinkUrl

fun openLink(context: Context, url: String, prefManager: SettingPrefManager) {
    val normalizedUrl = LinkUrl.normalize(url).getOrThrow()
    val intent = if (prefManager.useInnerWeb.get()) {
        Intent(context, WebViewActivity::class.java).putExtra(WebViewActivity.EXTRA_URL, normalizedUrl)
    } else {
        Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl))
    }
    context.startActivity(intent)
}
