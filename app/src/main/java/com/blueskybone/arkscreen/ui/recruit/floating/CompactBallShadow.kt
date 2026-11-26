package com.blueskybone.arkscreen.ui.recruit.floating

import android.graphics.*
import android.graphics.drawable.Drawable

/** Finite circular falloff: fully transparent before reaching the overlay boundary. */
class CompactBallShadow(private val density: Float) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun draw(canvas: Canvas) {
        val radius = (minOf(bounds.width(), bounds.height()) / 2f - 2 * density).coerceAtLeast(1f)
        paint.shader = RadialGradient(bounds.exactCenterX(), bounds.exactCenterY() + density,
            radius, intArrayOf(0x33000000, 0x33000000, 0x18000000, 0x00000000),
            floatArrayOf(0f, 0.76f, 0.87f, 1f), Shader.TileMode.CLAMP)
        canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY() + density, radius, paint)
    }
    override fun setAlpha(alpha: Int) { paint.alpha=alpha; invalidateSelf() }
    override fun setColorFilter(filter: ColorFilter?) { paint.colorFilter=filter; invalidateSelf() }
    @Deprecated("Deprecated in Android") override fun getOpacity() = PixelFormat.TRANSLUCENT
}
