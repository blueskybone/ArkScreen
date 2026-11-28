package com.blueskybone.arkscreen.ui.character

import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldOperatorInfo
import com.blueskybone.arkscreen.ui.UiStatus
import org.junit.Assert.*
import org.junit.Test

class EndfieldMissingStateTest {
    private val catalog = listOf(
        EndfieldOperatorInfo("1", "a", "A", "https://example.com/a.png", 6, "profession_guard", "char_property_fire", false),
        EndfieldOperatorInfo("2", "b", "B", "https://example.com/b.png", 5, "profession_caster", "char_property_cryst", false),
    )

    @Test fun failedOrPendingAssetFetchDoesNotReportAllOperatorsAsMissing() {
        val state = EndfieldCharUiState(catalog = catalog)
        assertTrue(state.missingCharacters.isEmpty())
        assertTrue(state.copy(status = UiStatus.Loading()).missingCharacters.isEmpty())
        assertTrue(state.copy(status = UiStatus.Error("offline")).missingCharacters.isEmpty())
        assertEquals(2, state.copy(status = UiStatus.Success()).missingCharacters.size)
    }

    @Test fun filtersUseSameKeysForMissingCharactersAndPreserveFullDirectory() {
        val state = EndfieldCharUiState(catalog = catalog, status = UiStatus.Success(), showMissing = true,
            filters = EndfieldCharFilters("★★★★★★", "profession_guard", "char_property_fire"))
        assertEquals(listOf("A"), state.visibleMissing.map { it.name })
        assertEquals(2, state.catalog.size)
        assertEquals(setOf("profession_guard", "profession_caster"), state.filterOptions[1].toSet())
        assertTrue(state.copy(filters = EndfieldCharFilters(property = "char_property_natural")).visibleMissing.isEmpty())
    }
}
