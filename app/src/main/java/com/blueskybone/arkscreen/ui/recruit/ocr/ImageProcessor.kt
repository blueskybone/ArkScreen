package com.blueskybone.arkscreen.ui.recruit.ocr

import android.content.Context
import android.graphics.Bitmap
import com.blueskybone.arkscreen.domain.service.TextTranslator
import com.blueskybone.arkscreen.util.cacheAssetFile
import timber.log.Timber

/**
 *   Created by blueskybone
 *   Date: 2024/2/23
 */


/*
* From bitmap to tags
* */
class ImageProcessor(
    private val context: Context,
    private val textTranslator: TextTranslator,
) {

    companion object {
        const val NONE_INFO = "none_info"
        const val ERROR_REC = "err_in_recognize"
        const val OK = "ok"
        const val UK_RESULT = "uk_result"

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

    suspend fun getRecruitTags(bitmap: Bitmap, screenWidth: Int, screenHeight: Int): ImageRecruitData {
        val roiBitmap = getRoiBitmap(bitmap, screenWidth, screenHeight)
        val scale: Int = getScale(screenWidth)
        val stdTagFilepath = cacheAssetFile(context, "target_std.dat")
            .getOrElse { error ->
                Timber.tag("RecruitOCR").e(error, "Failed to prepare OCR asset")
                return ImageRecruitData(ERROR_REC, "识别资源准备失败", emptyList())
            }
            .absolutePath

        val rawOutput = getTagText(roiBitmap, stdTagFilepath, scale)
        Timber.tag("RecruitOCR").d(
            "JNI output: screen=%dx%d roi=%dx%d scale=%d output=%s",
            screenWidth,
            screenHeight,
            roiBitmap.width,
            roiBitmap.height,
            scale,
            rawOutput,
        )
        val text = rawOutput.split(",".toRegex())
        val result = text
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()

        if (result.isEmpty()) {
            Timber.tag("RecruitOCR").w("JNI returned an empty result")
            return ImageRecruitData(UK_RESULT, "empty JNI result", emptyList())
        }

        return when (result[0]) {
            "NONE" -> {
                ImageRecruitData(NONE_INFO, "未获取有效信息", listOf())
            }

            "WRONG" -> {
                ImageRecruitData(ERROR_REC, result.getOrElse(1) { "unknown error" }, listOf())
            }

            "RECRUIT" -> {
                val tags = getTagsList(result.getOrElse(1) { "" })
                ImageRecruitData(OK, "ok", tags)
            }

            else -> {
                ImageRecruitData(UK_RESULT, result.getOrElse(1) { rawOutput }, listOf())
            }
        }
    }

    private suspend fun getTagsList(raw: String): List<String> {
        val rawTags = raw.split("_")
            .map(String::trim)
            .filter(String::isNotBlank)
        val translations = textTranslator.translateAll(rawTags)
        return rawTags.map { rawTag ->
            val translated = translations[rawTag] ?: rawTag
            Timber.tag("RecruitOCR").d(
                "Translate tag: raw=%s translated=%s",
                rawTag,
                translated,
            )
            translated
        }
    }

    private fun getRoiBitmap(source: Bitmap, width: Int, height: Int): Bitmap {
        val (x, y, roiWidth, roiHeight) = if (width > 2 * height) {
            val targetHeight = (height / 5.143).toInt()
            listOf(
                (width / 2 - height / 2.572).toInt(),
                (height / 2.06).toInt(),
                (targetHeight * 4.0).toInt(),
                targetHeight,
            )
        } else {
            val targetWidth = (width / 2.5).toInt()
            listOf(
                (width / 3.636).toInt(),
                (height / 2 - width / 111.111).toInt(),
                targetWidth,
                (targetWidth * 0.281).toInt(),
            )
        }
        return Bitmap.createBitmap(source, x, y, roiWidth, roiHeight)
    }

    private fun getScale(width: Int): Int = width / 640
}
