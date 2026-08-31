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
import timber.log.Timber

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
        Timber.tag(LOG_TAG).i("Screenshot service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            Timber.tag(LOG_TAG).i("Screenshot session stopped from notification")
            taskFlowStore.clear()
            stopSelfSafely()
            return START_NOT_STICKY
        }
        serviceScope.launch {
            val flow = taskFlowStore.consume()

            if (flow == null) {
                Timber.tag(LOG_TAG).w("Screenshot request ignored: no pending task")
                return@launch
            }

            when (val result = screenshotCapturer.captureOnce(flow.captureDelayMillis)) {
                is ScreenshotResult.Success -> {
                    Timber.tag(LOG_TAG).i("Screenshot delivered to recognition flow")
                    flow.onScreenshot(result.bitmap)
                }

                is ScreenshotResult.Failure -> {
                    Timber.tag(LOG_TAG).w("Screenshot failed: %s", result.error)
                    flow.onFailure(result.error)
                }
            }

            if (!flow.keepCaptureSessionAlive) {
                stopSelfSafely()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        screenshotCapturer.close()
        Timber.tag(LOG_TAG).i("Screenshot service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopSelfSafely() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        const val ACTION_STOP = "com.blueskybone.arkscreen.STOP_SCREENSHOT_CAPTURE"
        private const val NOTIFICATION_ID = 10086
        private const val LOG_TAG = "RecruitCapture"

        fun start(context: Context) {
            val intent = Intent(context, ScreenshotCaptureService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ScreenshotCaptureService::class.java))
        }
    }
}
