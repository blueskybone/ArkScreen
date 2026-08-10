package com.blueskybone.arkscreen.data.resource

import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.repository.GameResourceRepository
import com.blueskybone.arkscreen.domain.service.TextTranslator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
class TextTranslatorImpl(
    private val gameResourceRepository: GameResourceRepository,
) : TextTranslator {
    private val syncStarted = AtomicBoolean(false)
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun translate(key: String, fallback: String): String {
        val normalizedKey = key.trim()
        val map = gameResourceRepository.getI18nMap().getOrElse { throwable ->
            Timber.tag("TextTranslator").e(throwable, "Load i18n map failed")
            startResourceSync()
            return fallback
        }

        val translated = map[normalizedKey] ?: fallback.also {
            Timber.tag("TextTranslator").w(
                "Missing translation: key=%s fallback=%s",
                normalizedKey,
                fallback,
            )
        }
        startResourceSync()
        return translated
    }

    override suspend fun translateAll(keys: Collection<String>): Map<String, String> {
        val normalizedKeys = keys.asSequence().map(String::trim).distinct().toList()
        val map = gameResourceRepository.getI18nMap().getOrElse { throwable ->
            Timber.tag("TextTranslator").e(throwable, "Load i18n map failed")
            startResourceSync()
            return normalizedKeys.associateWith { it }
        }
        val translated = normalizedKeys.associateWith { key ->
            map[key] ?: key.also {
                Timber.tag("TextTranslator").w("Missing translation: key=%s", key)
            }
        }
        startResourceSync()
        return translated
    }

    private fun startResourceSync() {
        if (!syncStarted.compareAndSet(false, true)) return
        syncScope.launch {
            gameResourceRepository.syncResource(ConfigType.I18N_DB).collect()
        }
    }
}
