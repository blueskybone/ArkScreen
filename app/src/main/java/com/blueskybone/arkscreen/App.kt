package com.blueskybone.arkscreen

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.blueskybone.arkscreen.core.logger.FileLoggingTree
import com.blueskybone.arkscreen.data.local.pref.PrefManager
import com.blueskybone.arkscreen.data.network.equipCachePath
import com.blueskybone.arkscreen.data.network.skillCachePath
import com.blueskybone.arkscreen.data.network.skinCachePath
import com.blueskybone.arkscreen.platform.schedule.AtdAlarmReceiver
import com.blueskybone.arkscreen.util.getDensityDpi
import com.hjq.toast.Toaster
import org.koin.android.ext.android.getKoin
import timber.log.Timber
import java.io.File
import java.util.Calendar

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

        //Initialize Logger
        Timber.plant(FileLoggingTree())

        //set coil
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .logger(DebugLogger()) // 开启日志
                .build()
        )


        createFolder(skinCachePath)
        createFolder(equipCachePath)
        createFolder(skillCachePath)

        setCoilDiskCache()

        //依赖注入放在di里
//        val preferenceModule = module {
//            single { SharedPreferenceStore(this@App) }
//            single { PrefManager(get<SharedPreferenceStore>()) }
//        }
//
//        val databaseModule = module {
//
//            single { ArkDatabase.getDatabase(APP) }
//
//
//            single { get<ArkDatabase>().getAccountSkDao() }
//            single { get<ArkDatabase>().getAccountGcDao() }
//            single { get<ArkDatabase>().getLinkDao() }
//            single { get<ArkDatabase>().getAccountEfDao() }
//            single { get<ArkDatabase>().getGachaDao() }
//        }
//
//        val appModule = module {
//            single {
//                AccountRepositoryImpl(
//                    get<AccountSkDao>(),
//                    get<AccountGcDao>(),
//                    get<AccountEfDao>(),
//                    api= apiService,
//                    apiAk = akHypergryphService,
//                    preference = get<InnerPrefManager>()
//                )
//            }
//        }

        //startKoin也放在di
//        startKoin {
//            androidLogger()
//            modules(preferenceModule, databaseModule, appModule)
//        }


        //TODO：这三个你确定要放在这里吗
//        setDailyAlarm()
//        setAppTheme()
//        setToaster()
        //cancelDailyAlarm()
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
        println("setDailyAlarm")
        val prefManager: PrefManager by getKoin().inject()
//        if (!prefManager.backAutoAtd.get()) return
        //TODO:此处逻辑存在问题：应该进行backAutoAtd.get()的判断。包括整个alarmManager的启动与停止的逻辑都没有真正实现。
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
        println("cancelDailyAlarm")
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AtdAlarmReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags)
        alarmManager.cancel(pendingIntent)
    }

//    private fun setAppTheme() {
//        val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()
//        when (prefManager.appTheme.get()) {
//            AppTheme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//            AppTheme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//            AppTheme.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//        }
//    }
//
//    private fun setToaster() {
//        val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()
//        when (prefManager.appTheme.get()) {
//            AppTheme.LIGHT -> Toaster.setStyle(BlackToastStyle())
//            AppTheme.DARK, AppTheme.SYSTEM -> Toaster.setStyle(WhiteToastStyle())
//        }
//        Toaster.setGravity(Gravity.TOP, 0, 60 * screenDpi.toInt())
//    }
}