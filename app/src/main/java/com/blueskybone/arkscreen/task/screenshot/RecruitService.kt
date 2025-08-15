package com.blueskybone.arkscreen.task.screenshot

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Surface
import androidx.core.app.NotificationCompat
import com.blueskybone.arkscreen.App
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.task.CapturePermission
import com.blueskybone.arkscreen.task.recruit.RecruitManager
import com.blueskybone.arkscreen.ui.bindinginfo.RecruitMode
import com.blueskybone.arkscreen.util.convertImageToBitmap
import com.blueskybone.arkscreen.util.getDensityDpi
import com.blueskybone.arkscreen.util.getEleCombination
import com.blueskybone.arkscreen.util.getRealScreenSize
import com.hjq.toast.Toaster
import org.koin.android.ext.android.getKoin
import timber.log.Timber

/**
 *   Created by blueskybone
 *   Date: 2025/1/23
 */


class RecruitService : Service() {

    private val prefManager: PrefManager by getKoin().inject()
    private val handler = Handler(Looper.getMainLooper())
    private var inactivityRunnable: Runnable? = null
    private val inactivityTimeout: Long = 5 * 60 * 1000 //service kill self timer

    companion object {
        private const val FOREGROUND_SERVICE_ID = 2375
        private const val CHANNEL_FORE_ID = "1842"
        private const val CHANNEL_FORE_NAME = "screenshot_fore_service"

        private var screenWidth = 0
        private var screenHeight = 0
        private var screenDensityDpi = 0

        private var mutex: Boolean = false
    }

    private var mediaProjection: MediaProjection? = null
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var notification: Notification? = null

    private var imageReader: ImageReader? = null
    private var surface: Surface? = null
    private var virtualDisplay: VirtualDisplay? = null

    private var imageProcessor: ImageProcessor? = null
    private var recruitManager: RecruitManager? = null
    private var floatWindow: FloatWindow? = null

    private fun setScreenSize() {
        screenDensityDpi = getDensityDpi(this)
        val point = getRealScreenSize(this)
        screenWidth = point.x
        screenHeight = point.y
    }

    private var sleepScreenshot = false


    override fun onCreate() {
        super.onCreate()
        setScreenSize()
        createNotification()
        startInactivityTimer()

        recruitManager = RecruitManager.instance
        imageProcessor = ImageProcessor.instance
        floatWindow = FloatWindow(this)
        floatWindow!!.initialize()

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val start = intent?.getStringExtra("start_from")
            if (start == "QuickTileService") sleepScreenshot = true
        } catch (e: Exception) {
            Timber.e(e.message)
        }


        if (mutex) {
            return START_NOT_STICKY
        }
        mutex = true
        resetInactivityTimer()
        if (CapturePermission.intent == null) {
            val acquireIntent = Intent(this, AcquireCapturePermission::class.java)
            acquireIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(acquireIntent)
            return super.onStartCommand(intent, flags, startId)
        }
        intent.apply {
            val result = intent?.extras?.getBoolean("setPermission")
            if (result == true) {
                foreground()
                (getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager)?.apply {
                    mediaProjection = this.getMediaProjection(
                        Activity.RESULT_OK,
                        (CapturePermission.intent!!.clone() as Intent)
                    )
                    mediaProjection?.registerCallback(MyCallBack(), null)
                }

                setupImageReader()
                createVirtualDisplay()
            }
        }
//        val emptyIntent = Intent(this, TransActivity::class.java)
//        emptyIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        startActivity(emptyIntent)
        try {
            if (sleepScreenshot) {
                Thread.sleep(1000L)
            }
//            Thread.sleep(prefManager.screenShotDelay.get() * 1000L)
            imageReader!!.acquireLatestImage().use { image ->

                // TransActivity.finishActivity()
                if (image != null) {
                    val bitmap = convertImageToBitmap(image, Bitmap.Config.ARGB_8888)
                    val tags = imageProcessor!!.getRecruitTags(bitmap, screenWidth, screenHeight)
                    bitmap.recycle()
                    if (tags.status != ImageProcessor.OK) {
                        Toaster.show(tags.msg)
                        return@use
                    } else {
                        val tagList = getEleCombination(tags.tags)
                        val recruitResultList = mutableListOf<RecruitManager.RecruitResult>()
                        for (tagsCom in tagList) {
                            val recruitResult = recruitManager!!.getRecruitResult(tagsCom, true)
                            if (recruitResult.operators.isNotEmpty()) {
                                recruitResult.sort()
                                recruitResultList.add(recruitResult)
                            }
                        }
                        val finalList = recruitResultList.toList().sorted()
                        displayRecruitResult(tags.tags, finalList)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //TransActivity.finishActivity()
            mutex = false
        }
        mutex = false
        return START_NOT_STICKY
    }

    private fun displayRecruitResult(
        tags: List<String>,
        finalList: List<RecruitManager.RecruitResult>
    ) {
        when (prefManager.recruitMode.get()) {
            RecruitMode.FLOATWINDOW -> {
                floatWindow!!.openAndUpdateWindow(this, tags, finalList)
            }

            RecruitMode.TOAST -> {
                showToastResult(finalList)
            }

            RecruitMode.AUTO -> {
                if (finalList.isNotEmpty()) {
                    when (finalList[0].rare) {
                        1, 5, 6 -> {
                            floatWindow!!.openAndUpdateWindow(this, tags, finalList)
                        }

                        else -> {
                            showToastResult(finalList)
                        }
                    }
                } else {
                    showToastResult(finalList)
                }
            }
        }
    }


    private fun showToastResult(result: List<RecruitManager.RecruitResult>) {
        if (result.isEmpty()) {
            Toaster.show("无四星以上组合")
            return
        } else {
            val item = result[0]
            val str = StringBuilder()

            val rare = item.rare
            val tags = item.tags
            for (tag in tags) {
                str.append(tag).append(" ")
            }
            str.append("-> ").append(rare).append("★").append(" ")
            for (ope in item.operators) {
                if (ope.rare == rare) {
                    str.append(ope.name)
                    break
                }
            }
            Toaster.show(str)
        }
    }

    private fun setupImageReader() {
        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight, PixelFormat.RGBA_8888, 2
        )
        surface = imageReader!!.surface
    }

    private fun startInactivityTimer() {
        inactivityRunnable = Runnable {
            stopSelf()
        }
        handler.postDelayed(inactivityRunnable!!, inactivityTimeout)
    }

    private fun resetInactivityTimer() {
        handler.removeCallbacks(inactivityRunnable!!)
        handler.postDelayed(inactivityRunnable!!, inactivityTimeout)
    }

    private fun createVirtualDisplay() {
        virtualDisplay = mediaProjection!!.createVirtualDisplay(
            "ScreenShot",
            screenWidth,
            screenHeight,
            screenDensityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            //            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
//                    or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            surface,
            null,
            null
        )
    }

    private fun createNotification() {
        val channelFore = NotificationChannel(
            CHANNEL_FORE_ID,
            CHANNEL_FORE_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channelFore)

        //TODO:把这个改掉
        val snoozeIntent = Intent(this, App::class.java).apply {
            action = "KILL_SERVICE"
            putExtra(NotificationCompat.EXTRA_NOTIFICATION_ID, 0)
        }
        val snoozePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(this, 0, snoozeIntent, PendingIntent.FLAG_IMMUTABLE)

        notification = Notification.Builder(this, CHANNEL_FORE_ID)
            .setChannelId(CHANNEL_FORE_ID)
            .setSmallIcon(R.drawable.ic_rosm_tile)
            .setContentIntent(snoozePendingIntent)
            .setContentTitle("快速公招")
            .setContentText("此通知是快速公招截图的必要操作，可自行关闭")
            .setAutoCancel(true)
            .build()
    }

    private fun foreground() {
        startForeground(FOREGROUND_SERVICE_ID, notification)
    }

//    private fun background() {
//        stopForeground(STOP_FOREGROUND_DETACH)
//    }

    override fun onDestroy() {
        super.onDestroy()
        mediaProjection = null
        mediaProjectionManager = null
        imageReader = null
        surface?.release()
        virtualDisplay?.release()
        CapturePermission.intent = null
        imageProcessor = null
        recruitManager = null
        floatWindow?.close()
        floatWindow = null
        handler.removeCallbacks(inactivityRunnable!!)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    inner class MyCallBack : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
        }
    }

}