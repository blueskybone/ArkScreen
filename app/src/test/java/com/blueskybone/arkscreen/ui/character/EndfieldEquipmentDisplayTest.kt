package com.blueskybone.arkscreen.ui.character

import com.blueskybone.arkscreen.R
import org.junit.Assert.assertEquals
import org.junit.Test

class EndfieldEquipmentDisplayTest {
    @Test
    fun refineProgressUsesAllThreeTerms() {
        assertEquals(0, EndfieldEquipmentDisplay.refineProgress(null))
        assertEquals(0, EndfieldEquipmentDisplay.refineProgress(emptyMap()))
        assertEquals(50, EndfieldEquipmentDisplay.refineProgress(mapOf("1" to 3)))
        assertEquals(50, EndfieldEquipmentDisplay.refineProgress(mapOf("1" to 3, "2" to 2, "3" to 3)))
        assertEquals(100, EndfieldEquipmentDisplay.refineProgress(mapOf("1" to 3, "2" to 3, "3" to 3)))
    }

    @Test
    fun equipmentRaritySelectsFrameColor() {
        assertEquals(R.color.ef_equip_green, EndfieldEquipmentDisplay.qualityColor("equip_rarity_2"))
        assertEquals(R.color.ef_equip_purple, EndfieldEquipmentDisplay.qualityColor("equip_rarity_4"))
        assertEquals(R.color.ef_equip_gold, EndfieldEquipmentDisplay.qualityColor("equip_rarity_5"))
    }
}
