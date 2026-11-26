package com.blueskybone.arkscreen.ui.character

import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldCharacter
import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldCharacterData
import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldLabel
import org.junit.Assert.*
import org.junit.Test

class EndfieldCharListTest {
    @Test fun levelDescendingThenRarityAndNameWithMissingLevelLast() {
        val input = listOf(char("Z", null, 6), char("C", 90, 5), char("B", 90, 6),
            char("A", 90, 6), char("D", 80, 6))
        assertEquals(listOf("A", "B", "C", "D", "Z"),
            EndfieldCharList.visible(input, EndfieldCharFilters()).map { it.id })
        assertEquals("Z", input.first().id)
    }

    @Test fun combinesAllFiltersWithoutRemovingDetailSource() {
        val input = listOf(char("A", 90, 6), char("B", 80, 5),
            char("C", 70, 6).copy(charData = EndfieldCharacterData(
                rarity = EndfieldLabel("rarity_6", "6"), profession = EndfieldLabel(value = "辅助"),
                property = EndfieldLabel(value = "火"))))
        val filters = EndfieldCharFilters("★★★★★★", "先锋", "火")
        assertEquals(listOf("A"), EndfieldCharList.visible(input, filters).map { it.id })
        assertEquals(3, input.size)
        assertEquals(3, EndfieldCharList.visible(input, EndfieldCharFilters()).size)
        assertTrue(filters.active)
        assertFalse(EndfieldCharFilters().active)
    }

    @Test fun optionsUseFullListAndIgnoreBlankOrMissingLabels() {
        val input = listOf(char("A", 90, 6), char("B", 80, 5), char("C", 70, 6),
            EndfieldCharacter(charData = EndfieldCharacterData(profession = EndfieldLabel(value = ""))))
        val options = EndfieldCharList.options(input)
        assertEquals(listOf("★★★★★★", "★★★★★"), options[0])
        assertEquals(listOf("先锋"), options[1])
        assertEquals(listOf("火"), options[2])
        assertTrue(EndfieldCharList.visible(input, EndfieldCharFilters(property = "冰")).isEmpty())
    }

    private fun char(name: String, level: Int?, rarity: Int) = EndfieldCharacter(
        id = name, level = level, charData = EndfieldCharacterData(name = name,
            rarity = EndfieldLabel("rarity_$rarity", "$rarity"),
            profession = EndfieldLabel(value = "先锋"), property = EndfieldLabel(value = "火")))
}
