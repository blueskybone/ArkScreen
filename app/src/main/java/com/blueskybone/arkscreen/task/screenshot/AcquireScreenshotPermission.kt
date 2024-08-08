package com.blueskybone.arkscreen.task.screenshot

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import com.blueskybone.arkscreen.APP

/**
 *   Created by blueskybone
 *   Date: 2024/2/25
 */
class AcquireScreenshotPermission : Activity() {
    companion object {
        const val SCREENSHOT_REQUEST_CODE = 10453

        const val SCREENSHOT_RECRUIT = "screenshot_recruit"
        const val SCREENSHOT_DEFAULT = "screenshot_default"
        const val SCREENSHOT_PREPARE = "screenshot_prepare"
        var screenshotMode: String? = SCREENSHOT_DEFAULT
    }

//    override fun onNewIntent(intent: Intent?) {
//        setIntent(intent)
//        super.onNewIntent(intent)
//    }

    override fun onStart() {
        super.onStart()
        screenshotMode = intent.getStringExtra("SCREENSHOT_MODE")
        val test = intent.getBooleanExtra("test", false)
        if (test) return
        (getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager)?.apply {
            APP.setMediaProjectionManager(this)
            try {
                startActivityForResult(createScreenCaptureIntent(), SCREENSHOT_REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (SCREENSHOT_REQUEST_CODE == requestCode) {
            if (RESULT_OK == resultCode) {
                data?.run {
                    (data.clone() as? Intent)?.apply {
                        APP.setScreenshotPermission(this)
                        when (screenshotMode) {
                            SCREENSHOT_RECRUIT -> {
                                APP.startScreenshotRecruit()
                            }
                            SCREENSHOT_DEFAULT -> {
                                APP.startScreenshotRecruit()
                            }
                            SCREENSHOT_PREPARE -> {

                            }

                        }
                    }
                }
            } else {
                APP.setScreenshotPermission(null)
            }
        }
        finish()
    }
}