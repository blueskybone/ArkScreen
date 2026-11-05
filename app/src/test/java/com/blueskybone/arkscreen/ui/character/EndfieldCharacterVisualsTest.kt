package com.blueskybone.arkscreen.ui.character

import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.network.model.EndfieldTalentState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EndfieldCharacterVisualsTest {
    @Test fun mapsProvidedProfessionAndPropertyIcons() {
        assertEquals(R.drawable.ef_profession_caster, EndfieldCharacterVisuals.professionIcon("caster", "术师"))
        assertEquals(R.drawable.ef_profession_tank, EndfieldCharacterVisuals.professionIcon(null, "重装"))
        assertEquals(R.drawable.ef_profession_vanguard, EndfieldCharacterVisuals.professionIcon(null, "先锋"))
        assertEquals(R.drawable.ef_profession_guard, EndfieldCharacterVisuals.professionIcon("guard", "近卫"))
        assertEquals(R.drawable.ef_profession_support, EndfieldCharacterVisuals.professionIcon(null, "辅助"))
        assertEquals(R.drawable.ef_profession_striker, EndfieldCharacterVisuals.professionIcon(null, "突击"))
        assertEquals(R.drawable.ef_property_cold, EndfieldCharacterVisuals.propertyIcon("char_property_cryst", "寒冷"))
        assertEquals(R.drawable.ef_property_pulse, EndfieldCharacterVisuals.propertyIcon("char_property_pulse", "电磁"))
        assertEquals(R.drawable.ef_property_physical, EndfieldCharacterVisuals.propertyIcon(null, "物理"))
        assertEquals(R.drawable.ef_property_fire, EndfieldCharacterVisuals.propertyIcon("char_property_fire", "灼热"))
        assertEquals(R.drawable.ef_property_natural, EndfieldCharacterVisuals.propertyIcon(null, "腐蚀"))
        assertEquals(null, EndfieldCharacterVisuals.professionIcon("caster", "术士"))
        assertEquals(null, EndfieldCharacterVisuals.propertyIcon("char_property_fire", "灼燃"))
        assertEquals(null, EndfieldCharacterVisuals.propertyIcon(null, null))
    }

    @Test fun ordersFourSkillsAndMapsFiveProperties() {
        assertEquals(0, EndfieldCharacterVisuals.skillOrder("skill_type_normal_attack"))
        assertEquals(1, EndfieldCharacterVisuals.skillOrder("skill_type_normal_skill"))
        assertEquals(2, EndfieldCharacterVisuals.skillOrder("skill_type_combo_skill"))
        assertEquals(3, EndfieldCharacterVisuals.skillOrder("skill_type_ultimate_skill"))
        assertEquals(R.color.ef_skill_physical, EndfieldCharacterVisuals.propertyColor("char_property_physical"))
        assertEquals(R.color.ef_skill_pulse, EndfieldCharacterVisuals.propertyColor("skill_property_pulse"))
        assertEquals(R.color.ef_skill_natural, EndfieldCharacterVisuals.propertyColor("char_property_natural"))
        assertEquals(R.color.ef_skill_fire, EndfieldCharacterVisuals.propertyColor("skill_property_fire"))
        assertEquals(R.color.ef_skill_cryst, EndfieldCharacterVisuals.propertyColor("char_property_cryst"))
    }

    @Test fun talentUnlocksUseActualNodesAndInheritedTiers() {
        val state = EndfieldTalentState(
            attrNodes = listOf("ability_1"),
            latestPassiveSkillNodes = listOf("passive_0_2"),
            latestSpaceshipSkillNodes = listOf("spaceship_1_2"),
        )
        assertTrue(EndfieldCharacterVisuals.isTalentUnlocked("ability_1", state, "ability"))
        assertFalse(EndfieldCharacterVisuals.isTalentUnlocked("ability_3", state, "ability"))
        assertTrue(EndfieldCharacterVisuals.isTalentUnlocked("passive_0_1", state, "combat"))
        assertTrue(EndfieldCharacterVisuals.isTalentUnlocked("passive_0_2", state, "combat"))
        assertFalse(EndfieldCharacterVisuals.isTalentUnlocked("passive_1_1", state, "combat"))
        assertTrue(EndfieldCharacterVisuals.isTalentUnlocked("spaceship_1_1", state, "cultivation"))
    }
}
