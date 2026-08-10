package com.blueskybone.arkscreen.data.resource

import com.blueskybone.arkscreen.domain.repository.GameResourceRepository
import com.blueskybone.arkscreen.domain.service.TextTranslator

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
class TextTranslatorImpl(
    private val gameResourceRepository: GameResourceRepository,
) : TextTranslator {

    override suspend fun translate(key: String, fallback: String): String {
        val map = gameResourceRepository.getI18nMap()
            .getOrElse { emptyMap() }

        return map[key.trim()] ?: fallback
    }
}