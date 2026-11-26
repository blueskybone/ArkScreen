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

    private var autoRunning = false
    private var autoStarted = false
    private var autoSession: com.blueskybone.arkscreen.ui.recruit.screenshot.AutoRecruitSession? = null
    private val nextCapture = Runnable {
        if (isShowing && autoRunning && settings.recruitAutoMode.get()) ballIcon?.let(::capture)
    }

    private fun idleIcon(): Int = when {
        !settings.recruitAutoMode.get() -> R.drawable.ic_rosm_trans
        autoStarted && !autoRunning -> R.drawable.ic_ball_pause
        else -> R.drawable.ic_ball_play
    }

    fun onAutoModeChanged() {
        autoRunning = false
        autoStarted = false
        autoSession?.active = false
        autoSession = null
        handler.removeCallbacks(nextCapture)
        if (!busy) ballIcon?.setImageResource(idleIcon())
    }

    private fun onBallClick(icon: ImageView) {
        if (!settings.recruitAutoMode.get()) { capture(icon); return }
        autoRunning = !autoRunning
        autoStarted = true
        handler.removeCallbacks(nextCapture)
        if (autoRunning) {
            autoSession = com.blueskybone.arkscreen.ui.recruit.screenshot.AutoRecruitSession()
            if (!busy) capture(icon)
        } else {
            autoSession?.active = false
            pulse?.cancel()
            icon.alpha = 1f
            icon.setImageResource(R.drawable.ic_ball_pause)
        }
    }

    private fun capture(icon: ImageView) {
        if (busy) return
        busy = true
        val request = ++generation
        val session = if (autoRunning) autoSession else null
        val startedAt = android.os.SystemClock.uptimeMillis()
        icon.setImageResource(R.drawable.ic_ball_busy)
        icon.setColorFilter(android.graphics.Color.WHITE)
        pulse = android.animation.ObjectAnimator.ofFloat(icon, "alpha", 1f, 0.35f, 1f).apply {
            duration = 900
            repeatCount = android.animation.ValueAnimator.INFINITE
            start()
        }
        val timeout = Runnable {
            if (request == generation && busy) {
                autoSession?.active = false
                autoRunning = false
                ScreenshotCaptureService.stop(application)
                busy = false
                generation++
                pulse?.cancel()
                icon.alpha = 1f
                icon.setImageResource(R.drawable.ic_ball_error)
            }
        }
        fun complete(success: Boolean) {
            handler.post {
                if (request != generation || !busy) return@post
                handler.removeCallbacks(timeout)
                busy = false
                pulse?.cancel()
                icon.alpha = 1f
                if (session?.active == false) {
                    if (session === autoSession) autoRunning = false
                    icon.setImageResource(idleIcon())
                    if (autoRunning) handler.post(nextCapture)
                    return@post
                }
                icon.setImageResource(if (success) R.drawable.ic_ball_success else R.drawable.ic_ball_error)
                handler.postDelayed({
                    if (request == generation) {
                        icon.setImageResource(idleIcon())
                        icon.setColorFilter(android.graphics.Color.WHITE)
                    }
                }, 1200)
                if (autoRunning && session === autoSession) {
                    val delay = (3000L - (android.os.SystemClock.uptimeMillis() - startedAt)).coerceAtLeast(0L)
                    handler.postDelayed(nextCapture, delay)
                }
            }
        }
        // Permission cancellation or interrupted capture must not leave the ball locked.
        handler.postDelayed(timeout, 30_000)
        starter.start(application, ScreenshotStartSource.FloatingBall, ::complete, session)
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
        onAutoModeChanged()
        generation++
        busy = false
        pulse?.cancel()
        handler.removeCallbacksAndMessages(null)
        EasyWindow.cancelAll()

        val root = com.blueskybone.arkscreen.ui.recruit.screenshot.FloatingAppearance.read(settings).ball(application)
        ballRoot = root
        ballIcon = root.findViewById(android.R.id.icon)
        ballIcon?.setImageResource(idleIcon())
        EasyWindow.with(application)
            .setContentView(root)
            .setDraggable(InitialLayoutAwareDraggable())
            .setOnClickListener(
                android.R.id.icon,
                EasyWindow.OnClickListener { _: EasyWindow<*>?, icon: ImageView? ->
                    icon?.let(::onBallClick)
                } as EasyWindow.OnClickListener<ImageView?>
            )
            .show()

        isShowing = true
        Timber.tag("RecruitCapture").i("Floating recruit ball shown")
    }

    fun hide() {
        onAutoModeChanged()
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
