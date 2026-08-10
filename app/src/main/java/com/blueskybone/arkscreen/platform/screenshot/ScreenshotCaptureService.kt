package com.blueskybone.arkscreen.platform.screenshot

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ScreenshotCaptureService : Service() {

    private val screenshotCapturer: ScreenshotCapturer by inject()
    private val taskFlowStore: ScreenshotTaskFlowStore by inject()
    private val notificationFactory: ScreenshotNotificationFactory by inject()

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()

        startForeground(
            NOTIFICATION_ID,
            notificationFactory.createNotification()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            val flow = taskFlowStore.consume()

            if (flow == null) {
                stopSelfSafely()
                return@launch
            }

            when (val result = screenshotCapturer.captureOnce()) {
                is ScreenshotResult.Success -> {
                    flow.onScreenshot(result.bitmap)
                }

                is ScreenshotResult.Failure -> {
                    flow.onFailure(result.error)
                }
            }

            stopSelfSafely()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopSelfSafely() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        private const val NOTIFICATION_ID = 10086

        fun start(context: Context) {
            val intent = Intent(context, ScreenshotCaptureService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}