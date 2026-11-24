package com.blueskybone.arkscreen.ui.common.view

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import com.blueskybone.arkscreen.R

class StarterLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val messageView = TextView(context)
    private val pulseIcon = createIcon()
    private val echoIcon = createIcon().apply { alpha = 0f }
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = CYCLE_DURATION_MS
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { updateAnimation(it.animatedFraction) }
    }
    private val returnInterpolator = DecelerateInterpolator()

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER

        val iconContainer = FrameLayout(context).apply {
            addView(echoIcon, centeredIconParams())
            addView(pulseIcon, centeredIconParams())
        }
        addView(iconContainer, LayoutParams(dp(64), dp(64)))

        addView(messageView.apply {
            setText(R.string.loading)
            gravity = Gravity.CENTER
            setTextColor(resolveThemeColor(android.R.attr.textColorSecondary))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(8)
        })
    }

    fun setMessage(message: CharSequence) {
        messageView.text = message
    }

    fun setMessage(textRes: Int) {
        messageView.setText(textRes)
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) start() else stop()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun start() {
        if (!animator.isStarted) animator.start()
    }

    private fun stop() {
        animator.cancel()
        resetIcons()
    }

    private fun updateAnimation(fraction: Float) {
        val pulseFraction = (fraction / PULSE_ACTIVE_FRACTION).coerceIn(0f, 1f)
        val pulseScale = if (pulseFraction <= PULSE_PEAK_FRACTION) {
            lerp(1f, PULSE_MAX_SCALE, pulseFraction / PULSE_PEAK_FRACTION)
        } else {
            val returnFraction = (pulseFraction - PULSE_PEAK_FRACTION) /
                (1f - PULSE_PEAK_FRACTION)
            lerp(
                PULSE_MAX_SCALE,
                1f,
                returnInterpolator.getInterpolation(returnFraction),
            )
        }
        pulseIcon.scaleX = pulseScale
        pulseIcon.scaleY = pulseScale

        val echoFraction = (fraction / ECHO_ACTIVE_FRACTION).coerceIn(0f, 1f)
        val echoScale = lerp(1f, ECHO_MAX_SCALE, echoFraction)
        echoIcon.scaleX = echoScale
        echoIcon.scaleY = echoScale
        echoIcon.alpha = if (fraction <= ECHO_ACTIVE_FRACTION) {
            ECHO_START_ALPHA * (1f - echoFraction)
        } else {
            0f
        }
    }

    private fun resetIcons() {
        pulseIcon.scaleX = 1f
        pulseIcon.scaleY = 1f
        echoIcon.scaleX = 1f
        echoIcon.scaleY = 1f
        echoIcon.alpha = 0f
    }

    private fun createIcon(): AppCompatImageView = AppCompatImageView(context).apply {
        setImageResource(R.drawable.ic_starter)
        scaleType = ImageView.ScaleType.FIT_CENTER
        ImageViewCompat.setImageTintList(
            this,
            ColorStateList.valueOf(resolveThemeColor(com.google.android.material.R.attr.colorPrimary)),
        )
    }

    private fun centeredIconParams() = FrameLayout.LayoutParams(dp(32), dp(32), Gravity.CENTER)

    private fun resolveThemeColor(attribute: Int): Int {
        val value = TypedValue()
        return if (context.theme.resolveAttribute(attribute, value, true)) {
            if (value.resourceId != 0) context.getColor(value.resourceId) else value.data
        } else {
            Color.GRAY
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun lerp(start: Float, end: Float, fraction: Float): Float =
        start + (end - start) * fraction

    private companion object {
        const val CYCLE_DURATION_MS = 840L
        const val PULSE_ACTIVE_FRACTION = 0.15f
        const val PULSE_PEAK_FRACTION = 0.25f
        const val PULSE_MAX_SCALE = 1.02f
        const val ECHO_ACTIVE_FRACTION = 0.9f
        const val ECHO_MAX_SCALE = 1.85f
        const val ECHO_START_ALPHA = 0.48f
    }
}
