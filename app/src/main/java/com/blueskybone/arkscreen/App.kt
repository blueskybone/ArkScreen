package com.blueskybone.arkscreen

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.Gravity
import androidx.appcompat.app.AppCompatDelegate
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.CachePolicy
import com.blueskybone.arkscreen.bindinginfo.AppTheme
import com.blueskybone.arkscreen.network.equipCachePath
import com.blueskybone.arkscreen.network.skillCachePath
import com.blueskybone.arkscreen.network.skinCachePath
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.preference.preference.shared.SharedPreferenceStore
import com.blueskybone.arkscreen.receiver.AtdAlarmReceiver
import com.blueskybone.arkscreen.util.getDensityDpi
import com.hjq.toast.Toaster
import com.hjq.toast.style.BlackToastStyle
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent
import java.io.File
import java.util.Calendar
import java.util.Date

/**
 *   Created by blueskybone
 *   Date: 2024/12/30
 */

lateinit var APP: App

class App : Application() {

    companion object {
        var screenDpi: Float = 0F
    }

    init {
        APP = this
        Toaster.init(this)

    }

    private fun setCoilDiskCache() {
        val imageLoader = ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .respectCacheHeaders(false)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
        Coil.setImageLoader(imageLoader)
    }

    override fun onCreate() {
        super.onCreate()
        val screenDensityDpi = getDensityDpi(this)
        setScreenDpi(screenDensityDpi)

        Toaster.setStyle(BlackToastStyle())
        Toaster.setGravity(Gravity.TOP, 0, 60 * screenDpi.toInt())
        val preferenceModule = module {
            single { SharedPreferenceStore(this@App) }
            single { PrefManager(get<SharedPreferenceStore>()) }
        }
        startKoin {
            androidLogger()
            modules(preferenceModule)
        }
        createFolder(skinCachePath)
        createFolder(equipCachePath)
        createFolder(skillCachePath)

        setCoilDiskCache()
        setAppTheme()
        setDailyAlarm()
    }

    private fun createFolder(path: String) {
        val folder = File(path)
        if (!folder.exists()) folder.mkdirs()
    }

    private fun setScreenDpi(densityDpi: Int) {
        screenDpi = if (densityDpi > 480) {
            3F
        } else if (densityDpi > 320) {
            2F
        } else if (densityDpi > 240) {
            1.5F
        } else if (densityDpi > 160) {
            1F
        } else {
            0.75F
        }
    }

    fun setDailyAlarm() {
        val prefManager: PrefManager by getKoin().inject()
        if (!prefManager.backAutoAtd.get()) return
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AtdAlarmReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags)

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, prefManager.alarmAtdHour.get())
            set(Calendar.MINUTE, prefManager.alarmAtdMin.get())
            // 如果设置的时间早于当前时间，设置为明天的同一时间
            if (timeInMillis < now) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDailyAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AtdAlarmReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)
        alarmManager.cancel(pendingIntent)
    }
    private fun setAppTheme() {
        val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()
        when (prefManager.appTheme.get()) {
            AppTheme.light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            AppTheme.dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            AppTheme.system -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}