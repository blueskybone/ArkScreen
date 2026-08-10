package com.blueskybone.arkscreen.platform.screenshot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MediaProjectionScreenshotCapturer(
    private val context: Context,
    private val screenshotSession: ScreenshotSession,
) : ScreenshotCapturer {

    private val mediaProjectionManager: MediaProjectionManager by lazy {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override suspend fun captureOnce(): ScreenshotResult = withContext(Dispatchers.Default) {
        try {
            val resultCode = screenshotSession.getResultCode()
                ?: return@withContext ScreenshotResult.Failure(ScreenshotError.PermissionMissing)

            val data = screenshotSession.getData()
                ?: return@withContext ScreenshotResult.Failure(ScreenshotError.PermissionMissing)

            val metrics = getDisplayMetrics()
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val density = metrics.densityDpi

            val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
                ?: return@withContext ScreenshotResult.Failure(ScreenshotError.CaptureFailed)

            val imageReader = ImageReader.newInstance(
                width,
                height,
                PixelFormat.RGBA_8888,
                2
            )

            val virtualDisplay = mediaProjection.createVirtualDisplay(
                "screen_capture",
                width,
                height,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface,
                null,
                null
            )

            delay(150)

            val image = imageReader.acquireLatestImage()
                ?: run {
                    virtualDisplay.release()
                    imageReader.close()
                    mediaProjection.stop()
                    return@withContext ScreenshotResult.Failure(ScreenshotError.CaptureFailed)
                }

            val bitmap = image.use {
                val plane = it.planes[0]
                val buffer = plane.buffer
                val pixelStride = plane.pixelStride
                val rowStride = plane.rowStride
                val rowPadding = rowStride - pixelStride * width

                val rawBitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Bitmap.Config.ARGB_8888
                )

                rawBitmap.copyPixelsFromBuffer(buffer)

                Bitmap.createBitmap(rawBitmap, 0, 0, width, height).also {
                    rawBitmap.recycle()
                }
            }

            virtualDisplay.release()
            imageReader.close()
            mediaProjection.stop()

            ScreenshotResult.Success(bitmap)
        } catch (e: Throwable) {
            ScreenshotResult.Failure(ScreenshotError.SystemError(e))
        }
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