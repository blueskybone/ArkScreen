package com.blueskybone.arkscreen.platform.screenshot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber

class MediaProjectionScreenshotCapturer(
    private val context: Context,
    private val screenshotSession: ScreenshotSession,
) : ScreenshotCapturer {

    private val mediaProjectionManager: MediaProjectionManager by lazy {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private val captureMutex = Mutex()
    private val resourceLock = Any()
    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var captureWidth = 0
    private var captureHeight = 0
    private var captureDensityDpi = 0

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            releaseSession(stopProjection = false)
        }
    }

    override suspend fun captureOnce(delayMillis: Long): ScreenshotResult = captureMutex.withLock {
        withContext(Dispatchers.Default) {
            try {
                val safeDelayMillis = delayMillis.coerceIn(
                    MIN_CAPTURE_DELAY_MS,
                    MAX_CAPTURE_DELAY_MS,
                )
                val reader = getOrCreateSession()
                    ?: return@withContext ScreenshotResult.Failure(
                        ScreenshotError.PermissionMissing
                    )

                Timber.tag(LOG_TAG).i("Capture requested: delayMs=%d", safeDelayMillis)
                // 来源配置的延迟同时承担画面恢复和 ImageReader 等待首帧的职责。
                delay(safeDelayMillis)

                val image = reader.acquireLatestImage()
                    ?: return@withContext ScreenshotResult.Failure(
                        ScreenshotError.CaptureFailed
                    )

                val metrics = getDisplayMetrics()
                val bitmap = image.use {
                    val imageWidth = it.width
                    val imageHeight = it.height
                    val plane = it.planes[0]
                    val buffer = plane.buffer
                    val pixelStride = plane.pixelStride
                    val rowStride = plane.rowStride
                    val rowPadding = rowStride - pixelStride * imageWidth

                    val rawBitmap = Bitmap.createBitmap(
                        imageWidth + rowPadding / pixelStride,
                        imageHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    rawBitmap.copyPixelsFromBuffer(buffer)

                    Bitmap.createBitmap(
                        rawBitmap,
                        0,
                        0,
                        imageWidth,
                        imageHeight
                    ).also { rawBitmap.recycle() }
                }

                Timber.tag(LOG_TAG).i(
                    "Capture completed: display=%dx%d@%d bitmap=%dx%d",
                    metrics.widthPixels,
                    metrics.heightPixels,
                    metrics.densityDpi,
                    bitmap.width,
                    bitmap.height,
                )

                ScreenshotResult.Success(bitmap)
            } catch (e: CancellationException) {
                releaseSession(stopProjection = true)
                throw e
            } catch (e: Throwable) {
                Timber.tag(LOG_TAG).e(e, "Capture failed")
                releaseSession(stopProjection = true)
                ScreenshotResult.Failure(ScreenshotError.SystemError(e))
            }
        }
    }

    override fun close() {
        releaseSession(stopProjection = true)
    }

    private fun getOrCreateSession(): ImageReader? {
        synchronized(resourceLock) {
            val metrics = getDisplayMetrics()
            val existingProjection = mediaProjection
            if (existingProjection != null) {
                val dimensionsUnchanged =
                    captureWidth == metrics.widthPixels &&
                        captureHeight == metrics.heightPixels &&
                        captureDensityDpi == metrics.densityDpi
                if (dimensionsUnchanged) imageReader?.let { return it }

                Timber.tag(LOG_TAG).i(
                    "Display changed, rebuild capture target: old=%dx%d@%d new=%dx%d@%d",
                    captureWidth,
                    captureHeight,
                    captureDensityDpi,
                    metrics.widthPixels,
                    metrics.heightPixels,
                    metrics.densityDpi,
                )
                return resizeCaptureTargetLocked(metrics)
            }

            val resultCode = screenshotSession.getResultCode() ?: return null
            val data = screenshotSession.getData() ?: return null

            val projection = mediaProjectionManager.getMediaProjection(resultCode, data)
                ?: return null
            projection.registerCallback(
                projectionCallback,
                Handler(Looper.getMainLooper())
            )
            mediaProjection = projection
            Timber.tag(LOG_TAG).i(
                "MediaProjection session created: display=%dx%d@%d",
                metrics.widthPixels,
                metrics.heightPixels,
                metrics.densityDpi,
            )
            return createCaptureTargetsLocked(projection, metrics)
        }
    }

    private fun createCaptureTargetsLocked(
        projection: MediaProjection,
        metrics: DisplayMetrics,
    ): ImageReader {
        val reader = ImageReader.newInstance(
            metrics.widthPixels,
            metrics.heightPixels,
            PixelFormat.RGBA_8888,
            2,
        )
        imageReader = reader
        virtualDisplay = projection.createVirtualDisplay(
            "screen_capture",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            null,
        )
        captureWidth = metrics.widthPixels
        captureHeight = metrics.heightPixels
        captureDensityDpi = metrics.densityDpi
        return reader
    }

    private fun releaseCaptureTargetsLocked() {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
        captureWidth = 0
        captureHeight = 0
        captureDensityDpi = 0
    }

    /**
     * Android 14 起一次授权只能创建一个 VirtualDisplay。方向变化时必须复用原实例，
     * 通过 resize 和替换 Surface 调整采集尺寸，否则系统会拒绝第二次创建。
     */
    private fun resizeCaptureTargetLocked(metrics: DisplayMetrics): ImageReader {
        val display = checkNotNull(virtualDisplay) { "VirtualDisplay is missing" }
        val oldReader = imageReader
        val newReader = ImageReader.newInstance(
            metrics.widthPixels,
            metrics.heightPixels,
            PixelFormat.RGBA_8888,
            2,
        )
        display.resize(
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
        )
        display.surface = newReader.surface
        imageReader = newReader
        captureWidth = metrics.widthPixels
        captureHeight = metrics.heightPixels
        captureDensityDpi = metrics.densityDpi
        oldReader?.close()
        return newReader
    }

    private fun releaseSession(stopProjection: Boolean) {
        val projection: MediaProjection?
        synchronized(resourceLock) {
            projection = mediaProjection
            mediaProjection = null
            releaseCaptureTargetsLocked()
            screenshotSession.clear()
        }

        projection?.unregisterCallback(projectionCallback)
        if (stopProjection) projection?.stop()
        Timber.tag(LOG_TAG).i("MediaProjection session released: requested=%s", stopProjection)
    }

    private fun getDisplayMetrics(): DisplayMetrics {
        val metrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            metrics.widthPixels = bounds.width()
            metrics.heightPixels = bounds.height()
            metrics.densityDpi = context.resources.displayMetrics.densityDpi
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)
        }

        return metrics
    }

    private companion object {
        const val LOG_TAG = "RecruitCapture"
        const val MIN_CAPTURE_DELAY_MS = 0L
        const val MAX_CAPTURE_DELAY_MS = 5_000L
    }
}
