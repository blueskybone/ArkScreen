package com.blueskybone.arkscreen.domain.usecase.operator

import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldCharacter
import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldCharacterData
import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldOperatorInfo
import org.junit.Assert.*
import org.junit.Test

class EndfieldOperatorCatalogTest {
    private fun info(id: String, rarity: Int = 6) = EndfieldOperatorInfo(
        id, "char_$id", id, "https://example.com/portrait.png", rarity,
        "profession_guard", "char_property_physical", false,
    )

    @Test fun missingExcludesOwnedBothAdministratorsPreviewsAndUnlinkedEntries() {
        val catalog = listOf(info("89"), info("156"), info("1"), info("2", 4), info("3"),
            info("4").copy(isPreview = true), info("5").copy(charId = null))
        val owned = listOf(EndfieldCharacter(id = "char_1", charData = EndfieldCharacterData(id = "different")))
        assertEquals(listOf("3", "2"), EndfieldOperatorCatalog.missing(catalog, owned).map { it.wikiId })
    }

    @Test fun femaleAdministratorWikiUsesCharacterDataIdBeforeSharedAssetId() {
        val catalog = listOf(info("89"), info("156"))
        val character = EndfieldCharacter(id = "char_89", charData = EndfieldCharacterData(id = "char_156"))
        assertEquals("156", EndfieldOperatorCatalog.wikiEntry(catalog, character)?.wikiId)
        assertEquals("89", EndfieldOperatorCatalog.wikiEntry(catalog, character.copy(charData = null))?.wikiId)
        assertNull(EndfieldOperatorCatalog.wikiEntry(catalog, EndfieldCharacter()))
    }
}
