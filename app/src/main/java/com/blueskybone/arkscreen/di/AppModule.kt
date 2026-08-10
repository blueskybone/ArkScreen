package com.blueskybone.arkscreen.di

import android.os.Build
import com.blueskybone.arkscreen.domain.model.AppVersion
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val appModule = module {
    single<AppVersion> {
        val context = androidContext()
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            0
        )
        AppVersion(
            code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            },
            name = packageInfo.versionName ?: "unknown"
        )
    }
}