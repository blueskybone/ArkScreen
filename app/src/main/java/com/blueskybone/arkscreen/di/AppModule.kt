package com.blueskybone.arkscreen.di

import android.os.Build
import com.blueskybone.arkscreen.domain.model.AppVersion
import com.blueskybone.arkscreen.platform.notification.AttendanceNotificationController
import com.blueskybone.arkscreen.platform.schedule.AttendanceAlarmController
import com.blueskybone.arkscreen.platform.theme.AppThemeController
import kotlinx.coroutines.Dispatchers
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
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            },
            name = packageInfo.versionName ?: "unknown"
        )
    }

    single { Dispatchers.IO }
    single { AttendanceNotificationController(androidContext()) }
    single { AttendanceAlarmController(androidContext(), get()) }
    single { AppThemeController(get()) }
}
