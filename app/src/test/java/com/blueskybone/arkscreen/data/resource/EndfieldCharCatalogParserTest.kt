package com.blueskybone.arkscreen.data.resource

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class EndfieldCharCatalogParserTest {
    private fun resource() = ObjectMapper().readTree(File("src/main/assets/endfield_char_catalog.json"))

    @Test fun bundledCatalogHasSeparateAdministratorMappingsAndNullablePreviewId() {
        val catalog = EndfieldCharCatalogParser.parse(resource())
        assertTrue(catalog.isNotEmpty())
        assertEquals("93e76fbbc07f7b480cfe0870c6414494", catalog.single { it.wikiId == "156" }.charId)
        assertNotEquals(catalog.single { it.wikiId == "89" }.charId, catalog.single { it.wikiId == "156" }.charId)
        val preview = resource()
        (preview["operators"][0] as ObjectNode).putNull("charId").put("marker", "label_type_preview")
        val entry = EndfieldCharCatalogParser.parse(preview).first()
        assertTrue(entry.isPreview)
        assertNull(entry.charId)
    }

    @Test fun invalidDownloadCannotReplaceValidCatalog() {
        val invalidRarity = resource()
        (invalidRarity["operators"][0] as ObjectNode).put("rarity", 0)
        assertThrows(IllegalArgumentException::class.java) { EndfieldCharCatalogParser.parse(invalidRarity) }
        val duplicate = resource()
        (duplicate["operators"][1] as ObjectNode).put("wikiId", duplicate["operators"][0]["wikiId"].asText())
        assertThrows(IllegalArgumentException::class.java) { EndfieldCharCatalogParser.parse(duplicate) }
        val missingId = resource()
        (missingId["operators"][0] as ObjectNode).remove("charId")
        assertThrows(IllegalArgumentException::class.java) { EndfieldCharCatalogParser.parse(missingId) }
    }
}
