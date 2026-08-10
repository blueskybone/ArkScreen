package com.blueskybone.arkscreen

import android.app.Application
import android.os.Build
import android.util.Log
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.blueskybone.arkscreen.core.logger.FileLoggingTree
import com.blueskybone.arkscreen.data.network.equipCachePath
import com.blueskybone.arkscreen.data.network.skillCachePath
import com.blueskybone.arkscreen.data.network.skinCachePath
import com.blueskybone.arkscreen.di.appModule
import com.blueskybone.arkscreen.di.databaseModule
import com.blueskybone.arkscreen.di.preferenceModule
import com.blueskybone.arkscreen.di.repositoryModule
import com.blueskybone.arkscreen.di.useCaseModule
import com.blueskybone.arkscreen.di.viewModelModule
import com.blueskybone.arkscreen.di.screenshotModule
import com.blueskybone.arkscreen.platform.theme.AppThemeController
import com.blueskybone.arkscreen.di.recruitScreenshotModule
import com.blueskybone.arkscreen.util.getDensityDpi
import com.hjq.toast.Toaster
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import java.io.File

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
            .apply {
                if (BuildConfig.DEBUG) logger(DebugLogger())
            }
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
        val koinApplication = startKoin {
            if (BuildConfig.DEBUG) androidLogger()
            androidContext(this@App)
            modules(
                appModule,
                databaseModule,
                preferenceModule,
                repositoryModule,
                useCaseModule,
                viewModelModule,
                screenshotModule,
                recruitScreenshotModule,
            )
        }
        // Set the saved mode before the first activity is created.
        koinApplication.koin.get<AppThemeController>().applySavedTheme()
        val screenDensityDpi = getDensityDpi(this)
        setScreenDpi(screenDensityDpi)

        //Initialize Logger
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        Timber.plant(
            FileLoggingTree(
                context = this,
                minimumPriority = if (BuildConfig.DEBUG) Log.DEBUG else Log.WARN,
            )
        )

        createFolder(skinCachePath)
        createFolder(equipCachePath)
        createFolder(skillCachePath)

        setCoilDiskCache()

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

}
