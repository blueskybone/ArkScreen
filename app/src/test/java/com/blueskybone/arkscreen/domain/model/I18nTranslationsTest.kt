package com.blueskybone.arkscreen.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class I18nTranslationsTest {
    private val translations = I18nTranslations(
        recruit = mapOf("robot" to "支援机械"),
        profession = mapOf("WARRIOR" to "近卫干员"),
        subProfession = mapOf("charger" to "冲锋手"),
    )

    @Test
    fun `按分类查询翻译`() {
        assertEquals("支援机械", translations.translate("robot"))
        assertEquals("近卫干员", translations.translate("WARRIOR"))
        assertEquals("冲锋手", translations.translate("charger"))
    }

    @Test
    fun `不存在的键返回空值`() {
        assertNull(translations.translate("unknown"))
    }

    @Test
    fun `分类键冲突时使用稳定的优先级`() {
        val duplicated = I18nTranslations(
            recruit = mapOf("same" to "公招"),
            profession = mapOf("same" to "职业"),
            subProfession = mapOf("same" to "子职业"),
        )

        assertEquals("公招", duplicated.translate("same"))
    }
}
