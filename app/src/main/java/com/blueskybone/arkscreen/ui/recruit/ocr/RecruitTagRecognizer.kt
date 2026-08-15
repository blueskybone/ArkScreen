package com.blueskybone.arkscreen.ui.recruit.ocr

import android.content.Context
import android.graphics.Bitmap
import com.blueskybone.arkscreen.util.getRealScreenSize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber


class RecruitTagRecognizer(
    private val context: Context,
    private val imageProcessor: ImageProcessor
) {

    suspend fun recognize(bitmap: Bitmap): Result<List<String>> {
        return try {
            val tags = withContext(Dispatchers.Default) {
                val point = getRealScreenSize(context)
                val screenWidth = point.x
                val screenHeight = point.y

                val data = imageProcessor.getRecruitTags(bitmap, screenWidth, screenHeight)
                val tags = data.tags
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                Timber.tag("RecruitOCR").d(
                    "Recognition result: status=%s message=%s tags=%s",
                    data.status,
                    data.msg,
                    tags,
                )
                tags
            }
            Result.success(tags)
        } catch (error: CancellationException) {
            throw error
        } catch (throwable: Throwable) {
            Timber.tag("RecruitOCR").e(throwable, "Recruit tag recognition failed")
            Result.failure(throwable)
        }
    }
}
