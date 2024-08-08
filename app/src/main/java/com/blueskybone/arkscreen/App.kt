package com.blueskybone.arkscreen

import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.view.Gravity
import com.blueskybone.arkscreen.base.PrefManager
import com.blueskybone.arkscreen.base.preference.shared.SharedPreferenceStore
import com.blueskybone.arkscreen.task.recruit.RecruitManager
import com.blueskybone.arkscreen.task.screenshot.FloatingWindow
import com.blueskybone.arkscreen.task.screenshot.ImageProcessor
import com.blueskybone.arkscreen.task.screenshot.ScreenTaskService
import com.blueskybone.arkscreen.util.LoadingDialog
import com.blueskybone.arkscreen.util.getDensityDpi
import com.hjq.toast.Toaster
import com.hjq.toast.style.BlackToastStyle

import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.ModuleDeclaration
import org.koin.dsl.module

/**
 *   Created by blueskybone
 *   Date: 2024/7/26
 */
lateinit var APP: App


class App : Application() {

    companion object {
        var screenshotPermission: Intent? = null
        var mediaProjection: MediaProjection? = null
        var mediaProjectionManager: MediaProjectionManager? = null
        var alarmManager: AlarmManager? = null
        var loadingDialog: LoadingDialog? = null

        var screenDpi: Float = 0F

        private var mutexActivity = true
        private var mutexService = true

//        const val debugLogFileName = "debug_log.txt"
//        const val traceLogFileName = "trace_log.txt"
//        const val checkLogFileName = "check_log.txt"

//        const val playerInfoFileName = "player_info.txt"
//        const val charInfoMapFileName = "char_info_map.json"
//
//        const val charInfoMapVersion =
//            "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/char_info_map_version.xml"
//        const val recruitDbVersion =
//            "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/recruit_version.xml"
//        const val i18nVersionUrl =
//            "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/i18n_version.xml"
//
//        const val appUpdateInfoUrl =
//            "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/app_version.xml"
//        const val announcementUrl =
//            "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/announcement.xml"

    }

    init {
        Toaster.init(this)
        APP = this
    }

    override fun onCreate() {
        super.onCreate()

        val screenDensityDpi = getDensityDpi(this)
        setScreenDpi(screenDensityDpi)

        Toaster.setStyle(BlackToastStyle())
        Toaster.setGravity(Gravity.TOP, 0, 60 * screenDpi.toInt())

        val resourceModule = module {
            single { UpdateResource() }
        }

        val preferenceModule = module {
            single { SharedPreferenceStore(this@App) }
            single { PrefManager(get<SharedPreferenceStore>()) }
        }

        val screenshotTaskModule = module {
            single { ImageProcessor() }
            single { FloatingWindow(this@App) }
        }
        val recruitModule = module {
            single { I18n() }
            single { RecruitManager() }
        }
        startKoin {
            androidLogger()
            modules(resourceModule)
            modules(preferenceModule)
            modules(screenshotTaskModule)
            modules(recruitModule)
        }
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

    fun getScreenshotPermission(): Intent? {
        return screenshotPermission
    }

    fun setScreenshotPermission(permissionIntent: Intent?) {
        screenshotPermission = permissionIntent
    }

    fun setMediaProjectionManager(mpm: MediaProjectionManager) {
        mediaProjectionManager = mpm
    }

    fun createMediaProjection(): MediaProjection? {
        return mediaProjectionManager!!.getMediaProjection(
            Activity.RESULT_OK,
            (screenshotPermission!!.clone() as Intent)
        )
    }

    /*
    * val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
    startActivityForResult(permissionIntent, REQUEST_CODE_SCREEN_CAPTURE)
    * */
    fun createAlarmManager(): AlarmManager {
        if (alarmManager == null)
            alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        return alarmManager as AlarmManager
    }

    fun resetMediaProjection() {
        mediaProjection = null
    }

    //TODO: remember to use this
//    fun checkOverlayPermission(): Boolean {
//        return if (Settings.canDrawOverlays(this)) true
//        else {
//            Toaster.show("未授予悬浮窗权限")
//            false
//        }
//    }

    fun startScreenshotRecruit() {
        if (mutexActivity && mutexService) {
            mutexActivity = false
            mutexService = false
            val intent = Intent(this, ScreenTaskService::class.java)
            startForegroundService(intent)
        }
    }

    fun releaseMutexService() {
        mutexService = true
    }

    fun releaseMutexActivity() {
        mutexActivity = true
    }

    fun stopScreenTaskService() {
        val intent = Intent(this, ScreenTaskService::class.java)
        stopService(intent)
    }
}