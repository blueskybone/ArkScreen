package com.blueskybone.arkscreen.ui.character

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

/** 角色技能和天赋图标共用的圆形底色。 */
internal class EndfieldIconBackground(
    private val accent: Int,
    private val full: Boolean,
) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(canvas: Canvas) {
        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val radius = minOf(bounds.width(), bounds.height()) * 0.43f
        paint.style = Paint.Style.FILL
        paint.color = if (full) accent else Color.rgb(91, 91, 91)
        canvas.drawCircle(cx, cy, radius, paint)
        if (!full) {
            val circle = Path().apply { addCircle(cx, cy, radius, Path.Direction.CW) }
            canvas.save()
            canvas.clipPath(circle)
            paint.color = accent
            val lowerAccent = Path().apply {
                moveTo(cx - radius, cy + radius * 0.38f)
                lineTo(cx, cy - radius * 0.03f)
                lineTo(cx + radius, cy + radius * 0.38f)
                lineTo(cx + radius, cy + radius)
                lineTo(cx - radius, cy + radius)
                close()
            }
            canvas.drawPath(lowerAccent, paint)
            canvas.restore()
        }
        paint.color = Color.rgb(171, 171, 171)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.055f
        canvas.drawCircle(cx, cy, radius * 1.08f, paint)
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
