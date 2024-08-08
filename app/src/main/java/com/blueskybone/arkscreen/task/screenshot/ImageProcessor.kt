package com.blueskybone.arkscreen.task.screenshot

import android.graphics.Bitmap
import android.util.Log
import com.blueskybone.arkscreen.base.PrefManager
import com.blueskybone.arkscreen.task.recruit.RecruitManager
import com.blueskybone.arkscreen.util.getAssetsFilepath
import com.blueskybone.arkscreen.util.getRoiBitmap
import com.blueskybone.arkscreen.util.getScale
import com.blueskybone.arkscreen.util.saveBitmap
import org.koin.java.KoinJavaComponent.getKoin

/**
 *   Created by blueskybone
 *   Date: 2024/2/23
 */


class ImageProcessor {
    private val recruitManager: RecruitManager by getKoin().inject()
    private val prefManager: PrefManager by getKoin().inject()

    companion object {
        val NONE_INFO = "none_info"
        val ERROR_REC = "err_in_recognize"
        val OK = "ok"
        val UK_RESULT = "uk_result"

        var debug: Boolean = false

        init {
            System.loadLibrary("arkscreen")

        }
    }

    data class ImageRecruitData(
        val status: String,
        val msg: String = "OK",
        val tags: List<String>
    )


    private external fun getTagText(bitmap: Bitmap, dataPath: String, num: Int): String

    fun getRecruitTags(bitmap: Bitmap, screenWidth: Int, screenHeight: Int): ImageRecruitData {
        val roiBitmap = getRoiBitmap(bitmap, screenWidth, screenHeight)

//        debug = prefManager.enableDebug.get()
//        if (debug) {
//            loggerRun.write("roiBitmap width ${roiBitmap.width} height ${roiBitmap.height} ")
//            saveBitmap(roiBitmap, "roi_bitmap.jpg")
//        }
        val scale: Int = getScale(screenWidth)
        val stdTagFilepath = getAssetsFilepath("target_std.dat")
        val result = getTagText(roiBitmap, stdTagFilepath, scale).split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        Log.e("getRecruitTags", result[0])
        Log.e("getRecruitTags", result[1])
        return when (result[0]) {
            "NONE" -> {
                ImageRecruitData(NONE_INFO, "未获取有效信息", listOf())
            }

            "WRONG" -> {
                ImageRecruitData(ERROR_REC, result[1], listOf())
            }

            "RECRUIT" -> {
                val tags = getTagsList(result[1])
                ImageRecruitData(OK, "ok", tags)
            }

            else -> {
                ImageRecruitData(UK_RESULT, result[1], listOf())
            }
        }
    }

    private fun getTagsList(raw: String): List<String> {
        val rawTags = raw.split("_")
        val tags = mutableListOf<String>()
        for (rawTag in rawTags) {
            tags.add(recruitManager.getCn(rawTag))
        }
        return tags
    }
}