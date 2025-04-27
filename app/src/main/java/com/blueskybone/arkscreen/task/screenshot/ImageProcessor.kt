package com.blueskybone.arkscreen.task.screenshot

import android.graphics.Bitmap
import com.blueskybone.arkscreen.task.recruit.I18nManager
import com.blueskybone.arkscreen.util.getAssetsFilepath
import com.blueskybone.arkscreen.util.getRoiBitmap
import com.blueskybone.arkscreen.util.getScale

/**
 *   Created by blueskybone
 *   Date: 2024/2/23
 */


/*
* From bitmap to tags
* */
class ImageProcessor {
    private var i18nManager: I18nManager = I18nManager.instance

    companion object {
        const val NONE_INFO = "none_info"
        const val ERROR_REC = "err_in_recognize"
        const val OK = "ok"
        const val UK_RESULT = "uk_result"

        init {
            System.loadLibrary("arkscreen")
        }

        val instance: ImageProcessor by lazy { Holder.INSTANCE }
    }

    private object Holder {
        val INSTANCE = ImageProcessor()
    }

    data class ImageRecruitData(
        val status: String,
        val msg: String = "OK",
        val tags: List<String>
    )

    private external fun getTagText(bitmap: Bitmap, dataPath: String, num: Int): String

    fun getRecruitTags(bitmap: Bitmap, screenWidth: Int, screenHeight: Int): ImageRecruitData {
        val roiBitmap = getRoiBitmap(bitmap, screenWidth, screenHeight)
        val scale: Int = getScale(screenWidth)
        val stdTagFilepath = getAssetsFilepath("target_std.dat")

        val result = getTagText(roiBitmap, stdTagFilepath, scale).split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()


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
            tags.add(i18nManager.convert(rawTag, I18nManager.ConvertType.Recruit))
        }
        return tags
    }
}