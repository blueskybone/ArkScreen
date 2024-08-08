package com.blueskybone.arkscreen.task.screenshot

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.widget.Toast
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.I18n
import com.blueskybone.arkscreen.base.PrefManager
import com.blueskybone.arkscreen.task.recruit.RecruitManager
import com.blueskybone.arkscreen.util.convertImageToBitmap
import com.blueskybone.arkscreen.util.getDensityDpi
import com.blueskybone.arkscreen.util.getEleCombination
import com.blueskybone.arkscreen.util.getRealScreenSize
import com.hjq.toast.Toaster
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.getKoin

/**
 *   Created by blueskybone
 *   Date: 2024/7/26
 */

class ScreenshotActivity : Activity() {

    private val prefManager: PrefManager by getKoin().inject()

    companion object {
        private const val TAG = "ScreenshotActivity"

        private var mediaProjection: MediaProjection? = null
        private var imageReader: ImageReader? = null
        private var surface: Surface? = null
        private var virtualDisplay: VirtualDisplay? = null

        var screenWidth = 0
        var screenHeight = 0
        var screenDensityDpi = 0
        private const val delay: Long = 300

        var debug: Boolean = false
    }

    private fun setScreenSize() {
        screenDensityDpi = getDensityDpi(this)
        val point = getRealScreenSize(this)
        screenWidth = point.x
        screenHeight = point.y

//        if (debug)
//            loggerRun.write("screenWidth: $screenWidth  screenHeight: $screenHeight")
    }


    override fun onStart() {
        super.onStart()
        debug = prefManager.enableDebug.get()
        try {
            setScreenSize()
            if (prepareForScreenshot()) {
                val bitmap = takeOneScreenshot()
                if (bitmap != null) {
                    Thread {
                        processBitmap(bitmap)
                    }.start()
                }
            }
            stop()
        } catch (e: Exception) {
//            loggerTrace.write("$TAG: $e \n${exceptionStackTrace(e)}")
            stop()
        }
    }

    private fun takeOneScreenshot(): Bitmap? {
        Thread.sleep(delay)
        var bitmap: Bitmap? = null
//        if (debug)
//            loggerRun.write("takeOneScreenshot")
        try {
            imageReader!!.acquireLatestImage().use { image ->
                if (image != null) {
                    bitmap = convertImageToBitmap(image, Bitmap.Config.ARGB_8888)
//                    if (debug) {
//                        loggerRun.write("convertImageToBitmap")
//                        saveBitmap(bitmap!!, "raw_bitmap.jpg")
//                        loggerRun.write("save raw screenshot")
//                    }
                } else {
//                    loggerTrace.write("$TAG: takeOneScreenshotTest(): image==null")
                    Toaster.show("error: image null 检查日志")
                }
            }
        } catch (e: Exception) {
//            loggerTrace.write("$TAG: takeOneScreenshot: $e \n${exceptionStackTrace(e)}")
            Toaster.show("error: acquireLatestImage() 检查日志")
            return null
        }
        return bitmap
    }

    private fun processBitmap(bitmap: Bitmap) {
        val imageProcessor: ImageProcessor by getKoin().inject()
        val recruitManager: RecruitManager by getKoin().inject()
        val floatingWindow: FloatingWindow by getKoin().inject()

        val result = imageProcessor.getRecruitTags(
            bitmap,
            screenWidth,
            screenHeight
        )
        bitmap.recycle()

        val tags: List<String>
        when (result.status) {
            ImageProcessor.NONE_INFO, ImageProcessor.ERROR_REC, ImageProcessor.UK_RESULT -> {
                Toaster.show(result.msg)
//                if (debug)
//                    loggerRun.write("${result.msg}\n--------end-------")
                return
            }

            else -> {
                tags = result.tags
            }
        }
//        if (debug) {
//            loggerRun.write("tags ${tags[0]} ${tags[1]} ${tags[2]} ${tags[3]} ${tags[4]}")
//            loggerRun.write("--------end----------")
//        }
        Log.e("processBitmap tag size", tags.size.toString())
        val tagsList = getEleCombination(tags)
        val recruitResultList = mutableListOf<RecruitManager.RecruitResult>()
        for (tagsCom in tagsList) {
            val recruitResult = recruitManager.getRecruitResult(tagsCom, true)
            if (recruitResult.operators.isNotEmpty()) {
                recruitResult.sort()
                recruitResultList.add(recruitResult)
            }
        }

        val finalList: List<RecruitManager.RecruitResult> = recruitResultList.toList().sorted()
        Log.e("processBitmap finalList size", finalList.size.toString())
        Log.e("processBitmap finalList size", prefManager.recruitShowMode.get().toString())
        when (prefManager.recruitShowMode.get()) {
            PrefManager.ShowMode.TOAST -> {
                showToastResult(finalList)
            }

            null, PrefManager.ShowMode.FLOAT -> {
                Handler(Looper.getMainLooper()).post {
                    floatingWindow.openAndUpdateWindow(this, tags, finalList)
                }
            }

            PrefManager.ShowMode.AUTO -> {
                if (finalList.isNotEmpty()) {
                    when (finalList[0].rare) {
                        1, 5, 6 -> {
                            Handler(Looper.getMainLooper()).post {
                                floatingWindow.openAndUpdateWindow(this, tags, finalList)
                            }
                        }

                        else -> {
                            showToastResult(finalList)
                        }
                    }
                } else {
                    showToastResult(finalList)
                }
            }
        }
    }

    private fun showToastResult(result: List<RecruitManager.RecruitResult>) {
        Log.e("showToastResult", result.size.toString())
        if (result.isEmpty()) {
            Toaster.show("无四星以上组合")
            return
        } else {
            val item = result[0]
            val str = StringBuilder()

            val rare = item.rare
            val tags = item.tags
            for (tag in tags) {
                str.append(tag).append(" ")
            }
            str.append("-> ").append(rare).append("★").append(" ")
            for (ope in item.operators) {
                if (ope.rare == rare) {
                    str.append(ope.name)
                    break
                }
            }
            Toaster.show(str)
        }

    }

    private fun prepareForScreenshot(): Boolean {
        try {
            imageReader = ImageReader.newInstance(
                screenWidth,
                screenHeight, PixelFormat.RGBA_8888, 2
            )
            surface = imageReader!!.surface
//            if (debug)
//                loggerRun.write("set ImageReader and surface done.")

            mediaProjection = APP.createMediaProjection()
//            if (debug)
//                loggerRun.write("create MediaProjection done")

            startVirtualDisplay()
//            if (debug)
//                loggerRun.write("start VirtualDisplay ")
        } catch (e: Exception) {
            //loggerTrace.write("$TAG: prepareForScreenshot(): $e \n${exceptionStackTrace(e)}")
            Toaster.show("error: prepareForScreenshot 检查日志")
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun startVirtualDisplay() {
        virtualDisplay?.release()
        virtualDisplay = mediaProjection!!.createVirtualDisplay(
            "ScreenShot",
            screenWidth,
            screenHeight,
            screenDensityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                    or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            surface,
            null,
            null
        )
    }

    private fun stop() {
        stopVirtualDisplay()
        stopSurface()
        imageReader = null
        mediaProjection?.stop()
        mediaProjection = null
        APP.stopScreenTaskService()
        finish()
    }

    private fun stopVirtualDisplay() {
        virtualDisplay?.release()
        virtualDisplay = null
    }

    private fun stopSurface() {
        surface?.release()
        surface = null
    }

    private fun releaseMutex() {
        APP.releaseMutexActivity()
    }

    override fun onDestroy() {
        stop()
        releaseMutex()
        super.onDestroy()
    }

}