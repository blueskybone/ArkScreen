package com.blueskybone.arkscreen.platform.tile

import android.app.Application
import android.widget.ImageView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotStartSource
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotCaptureService
import com.blueskybone.arkscreen.ui.recruit.screenshot.RecruitScreenshotStarter
import com.blueskybone.arkscreen.ui.recruit.screenshot.FloatWindowController
import com.hjq.window.EasyWindow
import timber.log.Timber

class RecruitFloatingBallController(
    private val application: Application,
    private val starter: RecruitScreenshotStarter,
    private val floatWindowController: FloatWindowController,
) {

    private var isShowing = false

    fun toggle(): Boolean {
        return if (isShowing) {
            hide()
            false
        } else {
            show()
            true
        }.also {
            isShowing = it
        }
    }

    fun show() {
        EasyWindow.cancelAll()

        EasyWindow.with(application)
            .setContentView(R.layout.ic_tile)
            .setDraggable()
            .setOnClickListener(
                android.R.id.icon,
                EasyWindow.OnClickListener { _: EasyWindow<*>?, _: ImageView? ->
                    starter.start(
                        context = application,
                        source = ScreenshotStartSource.FloatingBall
                    )
                } as EasyWindow.OnClickListener<ImageView?>
            )
            .show()

        isShowing = true
        Timber.tag("RecruitCapture").i("Floating recruit ball shown")
    }

    fun hide() {
        EasyWindow.cancelAll()
        floatWindowController.close()
        ScreenshotCaptureService.stop(application)
        isShowing = false
        Timber.tag("RecruitCapture").i("Floating recruit ball hidden and capture session stopped")
    }
}
