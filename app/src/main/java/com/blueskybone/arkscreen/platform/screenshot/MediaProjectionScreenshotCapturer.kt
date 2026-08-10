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

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            releaseSession(stopProjection = false)
        }
    }

    override suspend fun captureOnce(): ScreenshotResult = captureMutex.withLock {
        withContext(Dispatchers.Default) {
            try {
                val reader = getOrCreateSession()
                    ?: return@withContext ScreenshotResult.Failure(
                        ScreenshotError.PermissionMissing
                    )

                // Let the persistent virtual display publish a fresh frame.
                delay(150)

                val image = reader.acquireLatestImage()
                    ?: return@withContext ScreenshotResult.Failure(
                        ScreenshotError.CaptureFailed
                    )

                val metrics = getDisplayMetrics()
                val bitmap = image.use {
                    val plane = it.planes[0]
                    val buffer = plane.buffer
                    val pixelStride = plane.pixelStride
                    val rowStride = plane.rowStride
                    val rowPadding = rowStride - pixelStride * metrics.widthPixels

                    val rawBitmap = Bitmap.createBitmap(
                        metrics.widthPixels + rowPadding / pixelStride,
                        metrics.heightPixels,
                        Bitmap.Config.ARGB_8888
                    )
                    rawBitmap.copyPixelsFromBuffer(buffer)

                    Bitmap.createBitmap(
                        rawBitmap,
                        0,
                        0,
                        metrics.widthPixels,
                        metrics.heightPixels
                    ).also { rawBitmap.recycle() }
                }

                ScreenshotResult.Success(bitmap)
            } catch (e: CancellationException) {
                releaseSession(stopProjection = true)
                throw e
            } catch (e: Throwable) {
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
            imageReader?.let { return it }

            val resultCode = screenshotSession.getResultCode() ?: return null
            val data = screenshotSession.getData() ?: return null
            val metrics = getDisplayMetrics()

            val projection = mediaProjectionManager.getMediaProjection(resultCode, data)
                ?: return null
            projection.registerCallback(
                projectionCallback,
                Handler(Looper.getMainLooper())
            )
            mediaProjection = projection

            val reader = ImageReader.newInstance(
                metrics.widthPixels,
                metrics.heightPixels,
                PixelFormat.RGBA_8888,
                2
            )
            imageReader = reader
            val display = projection.createVirtualDisplay(
                "screen_capture",
                metrics.widthPixels,
                metrics.heightPixels,
                metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.surface,
                null,
                null
            )

            virtualDisplay = display
            return reader
        }
    }

    private fun releaseSession(stopProjection: Boolean) {
        val projection: MediaProjection?
        synchronized(resourceLock) {
            projection = mediaProjection
            mediaProjection = null
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader?.close()
            imageReader = null
            screenshotSession.clear()
        }

        projection?.unregisterCallback(projectionCallback)
        if (stopProjection) projection?.stop()
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
}
