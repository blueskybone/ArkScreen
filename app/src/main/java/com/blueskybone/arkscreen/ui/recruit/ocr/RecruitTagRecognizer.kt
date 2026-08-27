package com.blueskybone.arkscreen.ui.recruit.ocr

import android.graphics.Bitmap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber


class RecruitTagRecognizer(
    private val imageProcessor: ImageProcessor
) {

    suspend fun recognize(bitmap: Bitmap): Result<List<String>> {
        return try {
            val tags = withContext(Dispatchers.Default) {
                val data = imageProcessor.getRecruitTags(bitmap)
                val tags = data.tags
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                Timber.tag("RecruitOCR").i(
                    "Recognition result: bitmap=%dx%d status=%s message=%s tagCount=%d",
                    bitmap.width,
                    bitmap.height,
                    data.status,
                    data.msg,
                    tags.size,
                )
                Timber.tag("RecruitOCR").d("Recognized tags=%s", tags)
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
