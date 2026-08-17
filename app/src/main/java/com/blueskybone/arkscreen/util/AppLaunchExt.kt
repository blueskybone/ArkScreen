package com.blueskybone.arkscreen.util

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("QueryPermissionsNeeded")
fun Context.launchApp(packageName: String, onAppNotFound: () -> Unit = {}) {
    packageManager.getLaunchIntentForPackage(packageName)
        ?.let(::startActivity)
        ?: onAppNotFound()
}
