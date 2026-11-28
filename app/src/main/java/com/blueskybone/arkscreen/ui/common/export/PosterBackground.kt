package com.blueskybone.arkscreen.ui.common.export

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.blueskybone.arkscreen.R
import kotlin.random.Random

/** Shared textured background for exported posters, independent of the app theme. */
class PosterBackground(private val context: Context, private val headerHeight: Int, private val footerHeight: Int) {
    fun draw(canvas: Canvas, width: Int, height: Int) {
        canvas.drawColor(BACKGROUND_COLOR)

        val contentTop = headerHeight.toFloat()
        val contentHeight = (height - headerHeight - footerHeight).toFloat()
        val centerX = width * 0.5f
        val centerY = contentTop + contentHeight * 0.48f
        val lightRadius = width * 0.7f
        val verticalScale = maxOf(1f, contentHeight / (lightRadius * 1.45f))

        backgroundLightPaint.shader = RadialGradient(
            centerX,
            centerY,
            lightRadius,
            intArrayOf(
                Color.rgb(50, 53, 56),
                Color.rgb(35, 38, 41),
                Color.rgb(18, 20, 22),
                BACKGROUND_COLOR,
            ),
            floatArrayOf(0f, 0.38f, 0.76f, 1f),
            Shader.TileMode.CLAMP,
        )
        val lightSaveCount = canvas.save()
        canvas.scale(1f, verticalScale, centerX, centerY)
        canvas.drawCircle(centerX, centerY, lightRadius, backgroundLightPaint)
        canvas.restoreToCount(lightSaveCount)
        backgroundLightPaint.shader = null

        noisePaint.shader = BitmapShader(
            noiseTexture,
            Shader.TileMode.REPEAT,
            Shader.TileMode.REPEAT,
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), noisePaint)
        noisePaint.shader = null

        drawWatermark(canvas, width, height)
    }

    private fun drawWatermark(canvas: Canvas, width: Int, height: Int) {
        val drawable = ContextCompat.getDrawable(
            context,
            R.drawable.operator_poster_watermark,
        ) ?: return
        val contentHeight = (height - headerHeight - footerHeight).toFloat()
        val logoSize = minOf(width * 0.94f, contentHeight * 0.72f)
        val centerX = width * 0.86f
        val centerY = headerHeight + contentHeight * 0.57f
        drawable.alpha = WATERMARK_ALPHA
        drawable.bounds = Rect(
            (centerX - logoSize / 2f).toInt(),
            (centerY - logoSize / 2f).toInt(),
            (centerX + logoSize / 2f).toInt(),
            (centerY + logoSize / 2f).toInt(),
        )
        drawable.draw(canvas)
    }

    private fun createNoiseTexture(): Bitmap {
        val random = Random(NOISE_SEED)
        val pixels = IntArray(NOISE_TILE_SIZE * NOISE_TILE_SIZE) {
            if (random.nextBoolean()) {
                Color.argb(random.nextInt(3, 11), 255, 255, 255)
            } else {
                Color.argb(random.nextInt(3, 9), 0, 0, 0)
            }
        }
        return Bitmap.createBitmap(
            pixels,
            NOISE_TILE_SIZE,
            NOISE_TILE_SIZE,
            Bitmap.Config.ARGB_8888,
        )
    }


    private val backgroundLightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val noisePaint = Paint()
    private val noiseTexture by lazy(::createNoiseTexture)
    private companion object {
        const val NOISE_TILE_SIZE = 96
        const val NOISE_SEED = 0x41524B
        const val WATERMARK_ALPHA = 18
        val BACKGROUND_COLOR = Color.rgb(8, 9, 10)
    }
}
