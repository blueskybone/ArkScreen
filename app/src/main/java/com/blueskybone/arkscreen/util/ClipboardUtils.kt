package com.blueskybone.arkscreen.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.ContextCompat.getString
import com.blueskybone.arkscreen.R
import com.hjq.toast.Toaster

fun copyToClipboard(context: Context, text: String) {
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText("label", text))
    Toaster.show(getString(context, R.string.copied))
}
