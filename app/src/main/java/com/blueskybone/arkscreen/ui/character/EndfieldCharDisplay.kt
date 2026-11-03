package com.blueskybone.arkscreen.ui.character

internal object EndfieldCharDisplay {
    fun stars(rarity: String?, key: String? = null): String =
        (rarity?.toIntOrNull() ?: key?.substringAfterLast('_')?.toIntOrNull())
        ?.takeIf { it in 1..6 }?.let { "★".repeat(it) }.orEmpty()

    fun gemQuality(templateId: String?): String = stars(
        templateId?.substringAfterLast("rarity_", missingDelimiterValue = ""),
    )
}
