package com.blueskybone.arkscreen.data.gacha

import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.model.I18nTranslations
import com.blueskybone.arkscreen.domain.model.ResourceSyncStatus
import com.blueskybone.arkscreen.domain.model.gacha.GachaPoolCatalog
import com.blueskybone.arkscreen.domain.model.gacha.GachaPoolCatalogEntry
import com.blueskybone.arkscreen.domain.model.operator.OperatorBasicInfo
import com.blueskybone.arkscreen.domain.model.recruit.RecruitDatabase
import com.blueskybone.arkscreen.domain.repository.GameResourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GachaImportResolverTest {

    @Test
    fun resolve_mapsPoolAndOperatorFromUpdatedResources() = runBlocking {
        val resolver = GachaImportResolver(
            FakeResources(
                operators = mapOf(
                    "char_003_kalts" to OperatorBasicInfo(
                        charId = "char_003_kalts",
                        name = "凯尔希·思衡托",
                        rarity = 5,
                        profession = "MEDIC",
                    )
                ),
                catalog = GachaPoolCatalog(
                    listOf(
                        GachaPoolCatalogEntry(
                            id = "limited_test",
                            names = setOf("测试限定"),
                            category = "LIMITED",
                            poolId = null,
                            startTs = 1000,
                            endTs = 2000,
                        )
                    )
                ),
            )
        )

        val result = resolver.resolve(document("测试限定", "凯尔希・思衡托", 5, 1500))
            .getOrThrow()

        val record = result.payload.records.single()
        assertEquals("LEGACY:limited_test", record.poolId)
        assertEquals("LIMITED", record.poolCate)
        assertEquals("char_003_kalts", record.charId)
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun resolve_prefersOfficialPoolIdAndInfersItsCategory() = runBlocking {
        val resolver = GachaImportResolver(
            FakeResources(
                operators = operator("芬", "char_123_fang", 2),
                catalog = GachaPoolCatalog(emptyList()),
            )
        )
        val input = document("幽境狩人", "芬", 2, 1500).copy(
            records = document("幽境狩人", "芬", 2, 1500).records.map {
                it.copy(poolId = "LINKAGE_74_0_1")
            }
        )

        val record = resolver.resolve(input).getOrThrow().payload.records.single()

        assertEquals("LINKAGE_74_0_1", record.poolId)
        assertEquals("LINKAGE", record.poolCate)
    }

    @Test
    fun resolve_usesStablePlaceholdersAndWarningsForUnknownValues() = runBlocking {
        val resolver = GachaImportResolver(
            FakeResources(emptyMap(), GachaPoolCatalog(emptyList()))
        )

        val first = resolver.resolve(document("未知池", "未来干员", 5, 1500)).getOrThrow()
        val second = resolver.resolve(document("未知池", "未来干员", 5, 1500)).getOrThrow()
        val record = first.payload.records.single()

        assertTrue(record.poolId.startsWith("LEGACY:UNKNOWN:"))
        assertTrue(record.charId.startsWith("UNKNOWN:"))
        assertEquals(record.poolId, second.payload.records.single().poolId)
        assertEquals(record.charId, second.payload.records.single().charId)
        assertTrue(first.warnings.any { it.code == GachaImportWarning.Code.UNKNOWN_POOL })
        assertTrue(first.warnings.any { it.code == GachaImportWarning.Code.UNMATCHED_OPERATOR })
    }

    @Test
    fun resolve_prefersBaseOperatorWhenNameIsAmbiguous() = runBlocking {
        val operators = listOf(
            OperatorBasicInfo("char_1001_amiya2", "阿米娅", 4, "WARRIOR"),
            OperatorBasicInfo("char_002_amiya", "阿米娅", 4, "CASTER"),
        ).associateBy(OperatorBasicInfo::charId)
        val resolver = GachaImportResolver(FakeResources(operators, GachaPoolCatalog(emptyList())))

        val result = resolver.resolve(document("未知池", "阿米娅", 4, 1500)).getOrThrow()

        assertEquals("char_002_amiya", result.payload.records.single().charId)
        assertTrue(result.warnings.any { it.code == GachaImportWarning.Code.AMBIGUOUS_OPERATOR })
    }

    private fun document(pool: String, char: String, rarity: Int, timestamp: Long) =
        GachaImportDocument(
            format = GachaImportFormat.XIAOHEIHE,
            sourceUid = "123456",
            sourceName = "小黑盒",
            records = listOf(
                RawGachaRecord(
                    timestamp = timestamp,
                    position = 0,
                    poolName = pool,
                    charName = char,
                    rarity = rarity,
                    isNew = false,
                )
            ),
        )

    private fun operator(name: String, id: String, rarity: Int) = mapOf(
        id to OperatorBasicInfo(id, name, rarity, "PIONEER")
    )

    private class FakeResources(
        private val operators: Map<String, OperatorBasicInfo>,
        private val catalog: GachaPoolCatalog,
    ) : GameResourceRepository {
        override fun syncResource(type: ConfigType): Flow<ResourceSyncStatus> =
            flowOf(ResourceSyncStatus.UpToDate(type))

        override suspend fun getOperatorBasicInfoMap() = Result.success(operators)
        override suspend fun getGachaPoolCatalog() = Result.success(catalog)
        override suspend fun getRecruitDb(): Result<RecruitDatabase> = error("unused")
        override suspend fun getI18nTranslations(): Result<I18nTranslations> = error("unused")
        override suspend fun getResourceDate(type: ConfigType) = Result.success("0")
        override fun clearResourceCache(type: ConfigType) = Unit
    }
}
