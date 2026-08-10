package com.blueskybone.arkscreen.ui.recruit.ocr

import android.content.Context
import android.graphics.Bitmap
import com.blueskybone.arkscreen.util.getRealScreenSize


//TODO: 注意context的传递
class RecruitTagRecognizer(
    private val context: Context,
    private val imageProcessor: ImageProcessor
) {

    fun recognize(bitmap: Bitmap): Result<List<String>> {
        return runCatching {

            val point = getRealScreenSize(context)
            val screenWidth = point.x
            val screenHeight = point.y

            val data = imageProcessor.getRecruitTags(bitmap, screenWidth, screenHeight)
            data.tags
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
        }
    }
}