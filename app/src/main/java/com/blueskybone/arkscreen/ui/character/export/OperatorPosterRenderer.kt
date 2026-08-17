package com.blueskybone.arkscreen.ui.character.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.blueskybone.arkscreen.BuildConfig
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.network.avatarUrl
import com.blueskybone.arkscreen.data.network.equipUrl
import com.blueskybone.arkscreen.data.network.skillUrl
import com.blueskybone.arkscreen.domain.model.operator.Operator
import com.blueskybone.arkscreen.domain.usecase.operator.OperatorPoster
import com.blueskybone.arkscreen.ui.character.evolveIconMap
import com.blueskybone.arkscreen.ui.character.potentialIconMap
import com.blueskybone.arkscreen.ui.character.profIconMap
import kotlinx.coroutines.async
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.net.URLEncoder
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

sealed interface OperatorPosterProgress {
    data class LoadingResources(val completed: Int, val total: Int) : OperatorPosterProgress
    data object Drawing : OperatorPosterProgress
}

class OperatorPosterRenderer(private val context: Context) {

    suspend fun render(
        poster: OperatorPoster,
        onProgress: suspend (OperatorPosterProgress) -> Unit = {},
    ): Bitmap {
        val images = loadImages(poster, onProgress)
        onProgress(OperatorPosterProgress.Drawing)
        val lines = poster.rows.flatMap { row ->
            row.operators.chunked(OPERATORS_PER_LINE)
                .ifEmpty { listOf(emptyList()) }
                .mapIndexed { index, operators ->
                    PosterLine(
                        profession = row.profession,
                        operators = operators,
                        showProfession = index == 0,
                    )
                }
        }
        val maxOperators = lines.maxOfOrNull { it.operators.size }?.coerceAtLeast(1) ?: 1
        val logicalWidth = OUTER_MARGIN * 2 + LABEL_WIDTH +
            maxOperators * TILE_SIZE + (maxOperators - 1).coerceAtLeast(0) * TILE_GAP
        val logicalHeight = HEADER_HEIGHT + lines.size * ROW_HEIGHT + FOOTER_HEIGHT
        val scale = minOf(1f, MAX_POSTER_WIDTH.toFloat() / logicalWidth)
        val outputWidth = (logicalWidth * scale).toInt()
        val outputHeight = (logicalHeight * scale).toInt()
        return Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
            val canvas = Canvas(bitmap)
            canvas.scale(scale, scale)
            drawPoster(canvas, poster, lines, images, logicalWidth, logicalHeight)
        }
    }

    private suspend fun loadImages(
        poster: OperatorPoster,
        onProgress: suspend (OperatorPosterProgress) -> Unit,
    ): Map<String, Bitmap> = coroutineScope {
        val urls = buildSet {
            poster.accountAvatarUrl?.takeIf(String::isNotBlank)?.let(::add)
            poster.rows.flatMap { it.operators }.forEach { operator ->
                add(operatorAvatarUrl(operator.skinId))
                operator.skills.filter { it.specializeLevel > 0 }.forEach {
                    add("$skillUrl${it.id}.png")
                }
                operator.equips.filterNot { it.locked }.forEach {
                    add("$equipUrl${it.typeIcon.uppercase()}_icon.png")
                }
            }
        }
        onProgress(OperatorPosterProgress.LoadingResources(0, urls.size))
        val completed = AtomicInteger(0)
        val semaphore = Semaphore(IMAGE_CONCURRENCY)
        urls.associateWith { url ->
            async {
                val bitmap = semaphore.withPermit {
                    try {
                        val request = ImageRequest.Builder(context)
                            .data(url)
                            .allowHardware(false)
                            .size(IMAGE_REQUEST_SIZE)
                            .build()
                        context.imageLoader.execute(request).drawable
                            ?.toBitmap(
                                width = IMAGE_REQUEST_SIZE,
                                height = IMAGE_REQUEST_SIZE,
                                config = Bitmap.Config.ARGB_8888,
                            )
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        null
                    }
                }
                onProgress(
                    OperatorPosterProgress.LoadingResources(
                        completed = completed.incrementAndGet(),
                        total = urls.size,
                    )
                )
                bitmap
            }
        }.mapValues { it.value.await() }.mapNotNullValues()
    }

    private fun drawPoster(
        canvas: Canvas,
        poster: OperatorPoster,
        lines: List<PosterLine>,
        images: Map<String, Bitmap>,
        width: Int,
        height: Int,
    ) {
        drawBackground(canvas, width, height)
        drawHeader(canvas, poster, images, width)
        lines.forEachIndexed { index, line ->
            drawRow(canvas, line, images, index, width)
        }
        drawFooter(canvas, width, height)
    }

    private fun drawBackground(canvas: Canvas, width: Int, height: Int) {
        canvas.drawColor(BACKGROUND_COLOR)

        val contentTop = HEADER_HEIGHT.toFloat()
        val contentHeight = (height - HEADER_HEIGHT - FOOTER_HEIGHT).toFloat()
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
        val contentHeight = (height - HEADER_HEIGHT - FOOTER_HEIGHT).toFloat()
        val logoSize = minOf(width * 0.94f, contentHeight * 0.72f)
        val centerX = width * 0.86f
        val centerY = HEADER_HEIGHT + contentHeight * 0.57f
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

    private fun drawHeader(
        canvas: Canvas,
        poster: OperatorPoster,
        images: Map<String, Bitmap>,
        width: Int,
    ) {
        val left = OUTER_MARGIN.toFloat()
        val right = (width - OUTER_MARGIN).toFloat()
        val serverLabel = poster.serverName

        textPaint.apply {
            color = HEADER_ACCENT_COLOR
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.12f
        }
        fitTextSize(
            text = HEADER_EYEBROW,
            maxWidth = right - left,
            preferredSize = 22f,
            minimumSize = 15f,
        )
        canvas.drawText(HEADER_EYEBROW, left, 58f, textPaint)

        val headerContentWidth = right - left
        val accountGroupWidth = minOf(520f, headerContentWidth * 0.42f)
        val accountGroupLeft = right - accountGroupWidth
        val titleMaxWidth = (accountGroupLeft - left - 48f).coerceAtLeast(120f)

        textPaint.apply {
            color = Color.WHITE
            textSize = 74f
            typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.04f
        }
        fitTextSize(
            text = "OPERATOR",
            maxWidth = titleMaxWidth,
            preferredSize = 74f,
            minimumSize = 30f,
        )
        canvas.drawText("OPERATOR", left, 170f, textPaint)
        textPaint.apply {
            color = Color.rgb(218, 218, 218)
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            letterSpacing = 0.09f
        }
        fitTextSize(
            text = "ARCHIVE",
            maxWidth = titleMaxWidth,
            preferredSize = 64f,
            minimumSize = 28f,
        )
        canvas.drawText("ARCHIVE", left, 238f, textPaint)

        val avatarBounds = RectF(right - 68f, 174f, right, 242f)
        drawAccountAvatar(
            canvas = canvas,
            bitmap = poster.accountAvatarUrl?.let(images::get),
            bounds = avatarBounds,
        )
        val accountTextRight = avatarBounds.left - 18f
        val accountTextWidth = (accountTextRight - accountGroupLeft).coerceAtLeast(120f)

        textPaint.apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.RIGHT
            letterSpacing = 0f
        }
        fitTextSize(
            text = poster.accountName,
            maxWidth = accountTextWidth,
            preferredSize = 34f,
            minimumSize = 24f,
        )
        canvas.drawText(poster.accountName, accountTextRight, 199f, textPaint)

        textPaint.apply {
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val serverBadgeWidth = textPaint.measureText(serverLabel) + 30f
        val badge = RectF(
            accountTextRight - serverBadgeWidth,
            210f,
            accountTextRight,
            242f,
        )
        headerBadgePaint.color = Color.rgb(35, 37, 40)
        canvas.drawRoundRect(badge, 16f, 16f, headerBadgePaint)
        headerBadgeStrokePaint.color = Color.rgb(76, 79, 83)
        canvas.drawRoundRect(badge, 16f, 16f, headerBadgeStrokePaint)
        textPaint.color = Color.rgb(220, 220, 220)
        canvas.drawText(serverLabel, badge.centerX(), badge.centerY() + 7f, textPaint)

        val summary = "六星 / 精二 / ${poster.operatorCount} 名"
        textPaint.apply {
            color = Color.rgb(180, 180, 180)
            textSize = 20f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.RIGHT
        }
        fitTextSize(
            text = summary,
            maxWidth = (badge.left - accountGroupLeft - 14f).coerceAtLeast(80f),
            preferredSize = 20f,
            minimumSize = 14f,
        )
        canvas.drawText(
            summary,
            badge.left - 14f,
            235f,
            textPaint,
        )
        dividerPaint.color = Color.rgb(55, 55, 55)
        canvas.drawRect(
            left,
            (HEADER_HEIGHT - 2).toFloat(),
            right,
            HEADER_HEIGHT.toFloat(),
            dividerPaint,
        )
    }

    private fun drawAccountAvatar(canvas: Canvas, bitmap: Bitmap?, bounds: RectF) {
        headerBadgePaint.color = Color.rgb(43, 45, 49)
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), bounds.width() / 2f, headerBadgePaint)
        if (bitmap != null) {
            val saveCount = canvas.save()
            val clip = Path().apply {
                addCircle(
                    bounds.centerX(),
                    bounds.centerY(),
                    bounds.width() / 2f,
                    Path.Direction.CW,
                )
            }
            canvas.clipPath(clip)
            drawCenterCrop(canvas, bitmap, bounds)
            canvas.restoreToCount(saveCount)
        }
        headerBadgeStrokePaint.color = Color.rgb(76, 79, 83)
        canvas.drawCircle(
            bounds.centerX(),
            bounds.centerY(),
            bounds.width() / 2f,
            headerBadgeStrokePaint,
        )
    }

    private fun fitTextSize(
        text: String,
        maxWidth: Float,
        preferredSize: Float,
        minimumSize: Float,
    ) {
        textPaint.textSize = preferredSize
        while (textPaint.textSize > minimumSize && textPaint.measureText(text) > maxWidth) {
            textPaint.textSize -= 1f
        }
    }

    private fun drawRow(
        canvas: Canvas,
        line: PosterLine,
        images: Map<String, Bitmap>,
        rowIndex: Int,
        posterWidth: Int,
    ) {
        val top = HEADER_HEIGHT + rowIndex * ROW_HEIGHT
        val centerY = top + TILE_SIZE / 2f
        if (line.showProfession) {
            drawResource(
                canvas,
                profIconMap[line.profession] ?: R.drawable.skill_icon_default,
                RectF(
                    OUTER_MARGIN.toFloat(),
                    centerY - 28f,
                    OUTER_MARGIN + 56f,
                    centerY + 28f,
                ),
            )
        }

        line.operators.forEachIndexed { index, operator ->
            val left = OUTER_MARGIN + LABEL_WIDTH + index * (TILE_SIZE + TILE_GAP)
            drawOperator(canvas, operator, images, left.toFloat(), (top + ROW_PADDING).toFloat())
        }

        dividerPaint.color = Color.rgb(35, 35, 35)
        canvas.drawRect(
            OUTER_MARGIN.toFloat(),
            (top + ROW_HEIGHT - 1).toFloat(),
            (posterWidth - OUTER_MARGIN).toFloat(),
            (top + ROW_HEIGHT).toFloat(),
            dividerPaint,
        )
    }

    private fun drawOperator(
        canvas: Canvas,
        operator: Operator,
        images: Map<String, Bitmap>,
        left: Float,
        top: Float,
    ) {
        val tile = RectF(left, top, left + TILE_SIZE, top + TILE_SIZE)
        tilePaint.color = Color.rgb(43, 45, 49)
        canvas.drawRoundRect(tile, TILE_CORNER_RADIUS, TILE_CORNER_RADIUS, tilePaint)

        val saveCount = canvas.save()
        val tileClip = Path().apply {
            addRoundRect(tile, TILE_CORNER_RADIUS, TILE_CORNER_RADIUS, Path.Direction.CW)
        }
        canvas.clipPath(tileClip)

        val avatarRect = RectF(tile.left + 4f, tile.top + 4f, tile.right - 4f, tile.bottom - 4f)
        val avatar = images[operatorAvatarUrl(operator.skinId)]
        if (avatar != null) {
            drawCenterCrop(canvas, avatar, avatarRect)
        } else {
            placeholderPaint.color = Color.rgb(45, 45, 45)
            canvas.drawRect(avatarRect, placeholderPaint)
        }

        overlayPaint.color = Color.argb(175, 0, 0, 0)
        canvas.drawRect(
            tile.left + 4f,
            tile.bottom - STATUS_BAR_HEIGHT,
            tile.left + 4f + LEVEL_BAR_WIDTH,
            tile.bottom - 4f,
            overlayPaint,
        )
        drawLevel(canvas, operator, tile)
        drawPotential(canvas, operator, tile)
        drawSkills(canvas, operator, images, tile)
        drawModules(canvas, operator, images, tile)
        canvas.restoreToCount(saveCount)
    }

    private fun drawLevel(canvas: Canvas, operator: Operator, tile: RectF) {
        drawResource(
            canvas,
            evolveIconMap[operator.evolvePhase] ?: R.drawable.skill_icon_default,
            RectF(tile.left + 8f, tile.bottom - 31f, tile.left + 30f, tile.bottom - 9f),
        )
        textPaint.apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("Lv.${operator.level}", tile.left + 34f, tile.bottom - 11f, textPaint)
    }

    private fun drawPotential(canvas: Canvas, operator: Operator, tile: RectF) {
        overlayPaint.color = Color.argb(175, 0, 0, 0)
        canvas.drawCircle(tile.right - 20f, tile.top + 20f, 17f, overlayPaint)
        drawResource(
            canvas,
            potentialIconMap[operator.potentialRank] ?: R.drawable.skill_icon_default,
            RectF(tile.right - 34f, tile.top + 6f, tile.right - 6f, tile.top + 34f),
        )
    }

    private fun drawSkills(
        canvas: Canvas,
        operator: Operator,
        images: Map<String, Bitmap>,
        tile: RectF,
    ) {
        operator.skills.filter { it.specializeLevel > 0 }.take(3).forEachIndexed { index, skill ->
            val left = tile.left + 7f + index * 31f
            val iconRect = RectF(left, tile.top + 7f, left + 27f, tile.top + 34f)
            overlayPaint.color = Color.argb(190, 20, 20, 20)
            canvas.drawRect(iconRect, overlayPaint)
            images["$skillUrl${skill.id}.png"]?.let {
                canvas.drawBitmap(it, null, iconRect, imagePaint)
            }
            drawBadge(canvas, skill.specializeLevel.toString(), iconRect.right - 2f, iconRect.bottom)
        }
    }

    private fun drawModules(
        canvas: Canvas,
        operator: Operator,
        images: Map<String, Bitmap>,
        tile: RectF,
    ) {
        operator.equips.filterNot { it.locked }.take(3).forEachIndexed { index, equip ->
            val top = tile.top + 42f + index * 31f
            val iconRect = RectF(tile.right - 34f, top, tile.right - 7f, top + 27f)
            overlayPaint.color = Color.argb(190, 20, 20, 20)
            canvas.drawRect(iconRect, overlayPaint)
            images["$equipUrl${equip.typeIcon.uppercase()}_icon.png"]?.let {
                canvas.drawBitmap(it, null, iconRect, imagePaint)
            }
            drawBadge(canvas, equip.stage.toString(), iconRect.right - 2f, iconRect.bottom)
        }
    }

    private fun drawBadge(canvas: Canvas, value: String, right: Float, bottom: Float) {
        overlayPaint.color = Color.argb(220, 0, 0, 0)
        canvas.drawCircle(right - 6f, bottom - 6f, 8f, overlayPaint)
        textPaint.apply {
            color = Color.WHITE
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(value, right - 6f, bottom - 2f, textPaint)
    }

    private fun drawFooter(canvas: Canvas, width: Int, height: Int) {
        textPaint.apply {
            color = Color.rgb(125, 125, 125)
            textSize = 20f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(
            "Generated by 公招助手 · v${BuildConfig.VERSION_NAME}",
            (width - OUTER_MARGIN).toFloat(),
            (height - 32).toFloat(),
            textPaint,
        )
    }

    private fun drawResource(canvas: Canvas, @DrawableRes resource: Int, destination: RectF) {
        val drawable = ContextCompat.getDrawable(context, resource) ?: return
        drawable.bounds = Rect(
            destination.left.toInt(),
            destination.top.toInt(),
            destination.right.toInt(),
            destination.bottom.toInt(),
        )
        drawable.draw(canvas)
    }

    private fun drawCenterCrop(canvas: Canvas, bitmap: Bitmap, destination: RectF) {
        val destinationRatio = destination.width() / destination.height()
        val sourceRatio = bitmap.width.toFloat() / bitmap.height
        val source = if (sourceRatio > destinationRatio) {
            val width = (bitmap.height * destinationRatio).toInt()
            val left = (bitmap.width - width) / 2
            Rect(left, 0, left + width, bitmap.height)
        } else {
            val height = (bitmap.width / destinationRatio).toInt()
            val top = (bitmap.height - height) / 2
            Rect(0, top, bitmap.width, top + height)
        }
        canvas.drawBitmap(bitmap, source, destination, imagePaint)
    }

    private fun operatorAvatarUrl(skinId: String): String {
        val encoded = URLEncoder.encode(skinId, Charsets.UTF_8.name())
        return "$avatarUrl$encoded.png"
    }

    private fun <K, V : Any> Map<K, V?>.mapNotNullValues(): Map<K, V> =
        mapNotNull { (key, value) -> value?.let { key to it } }.toMap()

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val placeholderPaint = Paint()
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dividerPaint = Paint()
    private val backgroundLightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val noisePaint = Paint()
    private val headerBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val headerBadgeStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val noiseTexture by lazy(::createNoiseTexture)

    private companion object {
        const val MAX_POSTER_WIDTH = 4096
        const val OUTER_MARGIN = 96
        const val LABEL_WIDTH = 82
        const val HEADER_HEIGHT = 278
        const val FOOTER_HEIGHT = 84
        const val TILE_SIZE = 144
        const val TILE_GAP = 14
        const val ROW_PADDING = 18
        const val ROW_HEIGHT = TILE_SIZE + ROW_PADDING * 2
        const val TILE_CORNER_RADIUS = 14f
        const val STATUS_BAR_HEIGHT = 38f
        const val LEVEL_BAR_WIDTH = 88f
        const val OPERATORS_PER_LINE = 12
        const val IMAGE_REQUEST_SIZE = 256
        const val IMAGE_CONCURRENCY = 6
        const val NOISE_TILE_SIZE = 96
        const val NOISE_SEED = 0x41524B
        const val WATERMARK_ALPHA = 18
        const val HEADER_EYEBROW = "ARKNIGHTS // PERSONAL DATA"
        val BACKGROUND_COLOR: Int = Color.rgb(8, 9, 10)
        val HEADER_ACCENT_COLOR: Int = Color.rgb(229, 184, 74)
    }

    private data class PosterLine(
        val profession: String,
        val operators: List<Operator>,
        val showProfession: Boolean,
    )
}
