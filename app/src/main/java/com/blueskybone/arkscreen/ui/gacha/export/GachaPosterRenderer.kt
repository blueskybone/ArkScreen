package com.blueskybone.arkscreen.ui.gacha.export

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.blueskybone.arkscreen.BuildConfig
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.network.avatarUrl
import com.blueskybone.arkscreen.platform.time.TimeUtils.getTimeStr
import com.blueskybone.arkscreen.ui.common.export.PosterBackground
import com.blueskybone.arkscreen.ui.gacha.gachaPullColor
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.net.URLEncoder
import java.util.concurrent.atomic.AtomicInteger

class GachaPosterRenderer(private val context: Context) {
    suspend fun render(poster: GachaPoster, progress: suspend (String) -> Unit): Bitmap = coroutineScope {
        val ids = poster.pools.flatMap { it.hitRecords }.map { it.charId }.distinct()
        val completed = AtomicInteger()
        val semaphore = Semaphore(6)
        val images = ids.associateWith { id -> async {
            semaphore.withPermit {
                val result = context.imageLoader.execute(ImageRequest.Builder(context)
                    .data(avatarUrl + URLEncoder.encode("$id#1.png", "UTF-8"))
                    .allowHardware(false).size(128).build())
                progress(context.getString(R.string.operator_poster_loading_progress,
                    completed.incrementAndGet(), ids.size))
                result.drawable?.toBitmap(128, 128)
            }
        } }.mapValues { it.value.await() }
        progress(context.getString(R.string.operator_poster_drawing))
        val height = HEADER + FOOTER + poster.visiblePools.sumOf { 112 + it.hitRecords.size * ROW + 24 }
        // Keep a single complete image while bounding bitmap memory for exceptionally large histories.
        val scale = minOf(1.0, kotlin.math.sqrt(24_000_000.0 / (WIDTH.toDouble() * height)))
        val bitmap = Bitmap.createBitmap((WIDTH * scale).toInt(), (height * scale).toInt(), Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.scale(scale.toFloat(), scale.toFloat())
            PosterBackground(context, HEADER, FOOTER).draw(canvas, WIDTH, height)
            text(canvas, "ARKNIGHTS // GACHA RECORD", 48f, 60f, 22f, GOLD)
            val icon = ContextCompat.getDrawable(context,
                if (poster.official) R.drawable.hg_icon_80x80 else R.drawable.ic_account_bilibili)?.toBitmap(80, 80)
            icon?.let { canvas.drawBitmap(it, null, RectF(48f, 94f, 128f, 174f), paint) }
            text(canvas, poster.nickname, 150f, 125f, 32f, WHITE, true, 480f)
            text(canvas, context.getString(R.string.gacha_statistics), 150f, 166f, 24f, MUTED)
            val range = "${getTimeStr(poster.firstTimestamp, "yyyy-MM-dd")} — ${getTimeStr(poster.lastTimestamp, "yyyy-MM-dd")}"
            text(canvas, range, 48f, 220f, 23f, MUTED)
            val stats = listOf(R.string.total_gacha_count to poster.total.toString(),
                R.string.six_star_count to poster.sixStars.toString(),
                R.string.average_six_star to (poster.average?.let { context.getString(R.string.gacha_average_times, it) } ?: "—"))
            stats.forEachIndexed { index, (label, value) ->
                val x = 48f + index * 270
                text(canvas, context.getString(label), x, 278f, 21f, MUTED)
                text(canvas, value, x, 324f, 34f, WHITE, true)
            }
            var top = HEADER.toFloat()
            val maximum = poster.pools.flatMap { it.hitRecords }.maxOfOrNull { it.count }?.coerceAtLeast(100) ?: 100
            poster.visiblePools.forEach { pool ->
                ensureActive()
                val blockHeight = 112 + pool.hitRecords.size * ROW
                paint.color = Color.argb(165, 35, 38, 41)
                canvas.drawRoundRect(RectF(32f, top, WIDTH - 32f, top + blockHeight), 24f, 24f, paint)
                text(canvas, pool.poolName, 56f, top + 49, 28f, WHITE, true, 590f)
                text(canvas, context.getString(R.string.gacha_count, pool.totalCount), 686f, top + 49, 26f, GOLD, maxWidth = 140f)
                if (pool.isFes) text(canvas, context.getString(R.string.limited_pool), 56f, top + 82, 18f, GOLD)
                pool.hitRecords.forEachIndexed { index, record ->
                    val y = top + 100 + index * ROW
                    paint.color = Color.rgb(56, 60, 64)
                    val rect = RectF(56f, y, 144f, y + 88)
                    canvas.drawRoundRect(rect, 12f, 12f, paint)
                    images[record.charId]?.let {
                        val save = canvas.save()
                        canvas.clipPath(Path().apply { addRoundRect(rect, 12f, 12f, Path.Direction.CW) })
                        canvas.drawBitmap(it, null, rect, paint)
                        canvas.restoreToCount(save)
                    }
                    text(canvas, record.name, 168f, y + 28, 25f, WHITE, true, 440f)
                    if (record.isNew) {
                        textPaint.textSize = 25f * FONT_SCALE
                        textPaint.typeface = Typeface.DEFAULT_BOLD
                        val name = TextUtils.ellipsize(record.name, textPaint, 440f, TextUtils.TruncateAt.END).toString()
                        text(canvas, "New", 180f + textPaint.measureText(name), y + 28, 18f, Color.rgb(235, 100, 92), true)
                    }
                    text(canvas, context.getString(R.string.gacha_count, record.count), 700f, y + 28, 23f, WHITE, maxWidth = 120f)
                    paint.color = ContextCompat.getColor(context, gachaPullColor(record.count))
                    canvas.drawRoundRect(RectF(168f, y + 45, 168f + 640f * record.count / maximum, y + 57), 6f, 6f, paint)
                    text(canvas, getTimeStr(record.ts, "yyyy-MM-dd"), 168f, y + 85, 18f, MUTED)
                }
                top += blockHeight + 24
            }
            text(canvas, context.getString(R.string.gacha_poster_generated, getTimeStr(System.currentTimeMillis(), "yyyy-MM-dd HH:mm")), 48f, top + 30, 18f, MUTED)
            text(canvas, context.getString(R.string.operator_poster_signature, BuildConfig.VERSION_NAME), 48f, top + 75, 19f, GOLD, rightAligned = true)
            bitmap
        } catch (error: Throwable) {
            bitmap.recycle()
            throw error
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private fun text(canvas: Canvas, value: String, x: Float, y: Float, size: Float, color: Int,
                     bold: Boolean = false, maxWidth: Float = WIDTH - x - 48, rightAligned: Boolean = false) {
        textPaint.color = color
        textPaint.textSize = size * FONT_SCALE
        textPaint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        val label = TextUtils.ellipsize(value, textPaint, maxWidth, TextUtils.TruncateAt.END).toString()
        val left = if (rightAligned) x + maxWidth - textPaint.measureText(label) else x
        canvas.drawText(label, left, y, textPaint)
    }
    private companion object {
        const val FONT_SCALE = 1.18f
        const val WIDTH = 880
        const val HEADER = 370
        const val FOOTER = 110
        const val ROW = 120
        val WHITE = Color.rgb(240, 241, 242)
        val MUTED = Color.rgb(172, 179, 185)
        val GOLD = Color.rgb(229, 184, 74)
    }
}
