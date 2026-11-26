package com.blueskybone.arkscreen.domain.usecase.widget

import com.blueskybone.arkscreen.domain.usecase.widget.WidgetAccountResolver
import com.blueskybone.arkscreen.domain.model.widget.WidgetConfig
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import org.junit.Assert.*
import org.junit.Test

class WidgetAccountResolverTest {
    private val official = AccountSk("same-uid", "A", "", true, "", "1")
    private val bilibili = AccountSk("same-uid", "B", "", false, "", "2")
    private val accounts = listOf(official, bilibili)

    @Test fun twoWidgetsStayBoundToTheirOwnServerWhenCurrentChanges() {
        val a = WidgetConfig(1, WidgetConfig.FIXED, "arknights", "1", "same-uid")
        val b = WidgetConfig(2, WidgetConfig.FIXED, "arknights", "2", "same-uid")
        for (current in accounts) {
            assertEquals(official, WidgetAccountResolver.resolve(a, accounts, current))
            assertEquals(bilibili, WidgetAccountResolver.resolve(b, accounts, current))
        }
    }

    @Test fun defaultWidgetFollowsCurrentAccount() {
        assertEquals(bilibili, WidgetAccountResolver.resolve(WidgetConfig(1), accounts, bilibili))
        assertNull(WidgetAccountResolver.resolve(WidgetConfig(1), accounts, null))
    }

    @Test fun removedFixedAccountNeverFallsBackToCurrent() {
        val config = WidgetConfig(1, WidgetConfig.FIXED, "arknights", "1", "same-uid")
        assertNull(WidgetAccountResolver.resolve(config, listOf(bilibili), bilibili))
    }

    @Test fun unknownModesAndOtherGamesDoNotLeakArknightsData() {
        assertNull(WidgetAccountResolver.resolve(WidgetConfig(1, game = "endfield"), accounts, official))
        assertNull(WidgetAccountResolver.resolve(WidgetConfig(1, accountMode = "unknown"), accounts, official))
    }
}
