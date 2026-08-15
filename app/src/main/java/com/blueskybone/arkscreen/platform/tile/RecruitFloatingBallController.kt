package com.blueskybone.arkscreen.platform.tile

import android.app.Application
import android.widget.ImageView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotStartSource
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotCaptureService
import com.blueskybone.arkscreen.ui.recruit.screenshot.RecruitScreenshotStarter
import com.hjq.window.EasyWindow

class RecruitFloatingBallController(
    private val application: Application,
    private val starter: RecruitScreenshotStarter,
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
    }

    fun hide() {
        EasyWindow.cancelAll()
        ScreenshotCaptureService.stop(application)
        isShowing = false
    }
}
