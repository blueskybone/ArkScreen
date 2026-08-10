package com.blueskybone.arkscreen.data.resource

import com.blueskybone.arkscreen.domain.repository.GameResourceRepository
import com.blueskybone.arkscreen.domain.service.TextTranslator
import timber.log.Timber

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
class TextTranslatorImpl(
    private val gameResourceRepository: GameResourceRepository,
) : TextTranslator {

    override suspend fun translate(key: String, fallback: String): String {
        val normalizedKey = key.trim()
        val map = gameResourceRepository.getI18nMap().getOrElse { throwable ->
            Timber.tag("RecruitOCR").e(throwable, "Load i18n map failed")
            return fallback
        }

        return map[normalizedKey] ?: fallback.also {
            Timber.tag("RecruitOCR").w(
                "Missing translation: key=%s fallback=%s",
                normalizedKey,
                fallback,
            )
        }
    }
}
