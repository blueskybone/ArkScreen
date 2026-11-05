package com.blueskybone.arkscreen.ui.character

import org.junit.Assert.assertEquals
import org.junit.Test

class EndfieldCharDisplayTest {
    @Test fun formatsCharacterAndWeaponRarityAsStars() {
        assertEquals("★★★★★★", EndfieldCharDisplay.stars("6", "rarity_6"))
        assertEquals("★★★★★", EndfieldCharDisplay.stars("五星", "rarity_5"))
        assertEquals("", EndfieldCharDisplay.stars(null, null))
    }

    @Test fun parsesGemQualityFromTemplate() {
        assertEquals("★★★★★", EndfieldCharDisplay.gemQuality("item_gem_rarity_5"))
        assertEquals("", EndfieldCharDisplay.gemQuality(null))
    }
}
