package com.blueskybone.arkscreen.domain.model.link

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkUrlTest {

    @Test
    fun normalize_acceptsRegularWebUrls() {
        assertEquals(
            "https://example.com/path",
            LinkUrl.normalize("  https://example.com/path  ").getOrThrow(),
        )
    }

    @Test
    fun normalize_rejectsNonWebSchemesAndIncompleteUrls() {
        assertTrue(LinkUrl.normalize("file:///data/local/file").isFailure)
        assertTrue(LinkUrl.normalize("intent://example.com").isFailure)
        assertTrue(LinkUrl.normalize("example.com").isFailure)
    }
}
