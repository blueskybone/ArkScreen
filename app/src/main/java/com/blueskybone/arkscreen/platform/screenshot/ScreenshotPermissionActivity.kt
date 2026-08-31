package com.blueskybone.arkscreen.platform.screenshot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.inject
import timber.log.Timber
class ScreenshotPermissionActivity : AppCompatActivity() {

    private val screenshotSession: ScreenshotSession by inject()

    private val mediaProjectionManager: MediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private val screenCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data

            if (result.resultCode == Activity.RESULT_OK && data != null) {
                Timber.tag("RecruitCapture").i("MediaProjection permission granted")
                screenshotSession.save(
                    resultCode = result.resultCode,
                    data = data
                )
                val appContext = applicationContext
                finish()
                // 让透明授权页先真正退出，避免按授权页方向创建 VirtualDisplay。
                Handler(Looper.getMainLooper()).postDelayed(
                    { ScreenshotCaptureService.start(appContext) },
                    SERVICE_START_DELAY_MS,
                )
                return@registerForActivityResult
            }

            Timber.tag("RecruitCapture").w("MediaProjection permission denied or cancelled")

            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        screenCaptureLauncher.launch(
            mediaProjectionManager.createScreenCaptureIntent()
        )
    }

    companion object {
        private const val SERVICE_START_DELAY_MS = 250L
        fun createIntent(context: Context): Intent {
            return Intent(context, ScreenshotPermissionActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
