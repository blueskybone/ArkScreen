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

    private val settings: com.blueskybone.arkscreen.data.local.pref.SettingPrefManager = org.koin.mp.KoinPlatform.getKoin().get()
    private var ballIcon: ImageView? = null
    private var ballRoot: android.widget.FrameLayout? = null
    fun refreshAppearance() {
        val root = ballRoot ?: return
        val style = com.blueskybone.arkscreen.ui.recruit.screenshot.FloatingAppearance.read(settings)
        val sample = style.ball(application).getChildAt(0) as ImageView
        ballIcon?.apply {
            background = sample.background; elevation = sample.elevation
            setPadding(sample.paddingLeft, sample.paddingTop, sample.paddingRight, sample.paddingBottom)
            layoutParams = sample.layoutParams
            colorFilter = sample.colorFilter
        }
        root.requestLayout()
    }
    private var isShowing = false
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var busy = false
    private var pulse: android.animation.ObjectAnimator? = null
    private var generation = 0

    private fun capture(icon: ImageView) {
        if (busy) return
        busy = true
        val request = ++generation
        icon.setImageResource(R.drawable.ic_ball_busy)
        icon.setColorFilter(com.blueskybone.arkscreen.ui.recruit.screenshot.FloatingAppearance.read(settings).iconColor(application))
        pulse = android.animation.ObjectAnimator.ofFloat(icon, "alpha", 1f, 0.35f, 1f).apply {
            duration = 900
            repeatCount = android.animation.ValueAnimator.INFINITE
            start()
        }
        fun complete(success: Boolean) {
            handler.post {
                if (request != generation || !busy) return@post
                busy = false
                pulse?.cancel()
                icon.alpha = 1f
                icon.setImageResource(if (success) R.drawable.ic_ball_success else R.drawable.ic_ball_error)
                handler.postDelayed({
                    if (request == generation) {
                        icon.setImageResource(R.drawable.ic_ball_scan)
                        icon.setColorFilter(com.blueskybone.arkscreen.ui.recruit.screenshot.FloatingAppearance.read(settings).iconColor(application))
                    }
                }, 1200)
            }
        }
        // Permission cancellation or interrupted capture must not leave the ball locked.
        handler.postDelayed({ complete(false) }, 30_000)
        starter.start(application, ScreenshotStartSource.FloatingBall, ::complete)
    }

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
        generation++
        busy = false
        pulse?.cancel()
        handler.removeCallbacksAndMessages(null)
        EasyWindow.cancelAll()

        val root = com.blueskybone.arkscreen.ui.recruit.screenshot.FloatingAppearance.read(settings).ball(application)
        ballRoot = root
        ballIcon = root.findViewById(android.R.id.icon)
        EasyWindow.with(application)
            .setContentView(root)
            .setDraggable()
            .setOnClickListener(
                android.R.id.icon,
                EasyWindow.OnClickListener { _: EasyWindow<*>?, icon: ImageView? ->
                    icon?.let(::capture)
                } as EasyWindow.OnClickListener<ImageView?>
            )
            .show()

        isShowing = true
        Timber.tag("RecruitCapture").i("Floating recruit ball shown")
    }

    fun hide() {
        ballRoot = null
        ballIcon = null
        generation++
        busy = false
        pulse?.cancel()
        handler.removeCallbacksAndMessages(null)
        EasyWindow.cancelAll()
        floatWindowController.close()
        ScreenshotCaptureService.stop(application)
        isShowing = false
        Timber.tag("RecruitCapture").i("Floating recruit ball hidden and capture session stopped")
    }
}
