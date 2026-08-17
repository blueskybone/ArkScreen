package com.blueskybone.arkscreen.domain.model

/**
 * 游戏文本翻译资源。分类与远端 JSON 保持一致，避免不同语义的键被合并后静默覆盖。
 */
data class I18nTranslations(
    val recruit: Map<String, String>,
    val profession: Map<String, String>,
    val subProfession: Map<String, String>,
) {
    fun translate(key: String): String? =
        recruit[key] ?: profession[key] ?: subProfession[key]
}
