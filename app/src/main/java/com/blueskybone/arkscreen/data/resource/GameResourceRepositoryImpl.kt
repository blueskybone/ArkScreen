package com.blueskybone.arkscreen.data.resource


import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.model.I18nTranslations
import com.blueskybone.arkscreen.data.resource.model.RecruitDatabaseDto
import com.blueskybone.arkscreen.domain.model.ResourceSyncStatus
import com.blueskybone.arkscreen.domain.model.operator.OperatorBasicInfo
import com.blueskybone.arkscreen.domain.model.recruit.RecruitDatabase
import com.blueskybone.arkscreen.domain.repository.GameResourceRepository
import kotlinx.coroutines.flow.Flow
import java.io.FileNotFoundException

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */

class GameResourceRepositoryImpl(
    private val gameResourceStore: GameResourceStore,
    private val jsonReader: ResourceJsonReader,
) : GameResourceRepository {

    override fun syncResource(type: ConfigType): Flow<ResourceSyncStatus> {
        return gameResourceStore.sync(type)
    }

    override suspend fun getRecruitDb(): Result<RecruitDatabase> {
        return gameResourceStore.load(ConfigType.RECRUIT_DB) { file ->
            jsonReader.readEntity(
                file = file,
                clazz = RecruitDatabaseDto::class.java,
            ).toDomain()
        }
    }

    override suspend fun getI18nTranslations(): Result<I18nTranslations> {
        return gameResourceStore.load(ConfigType.I18N_DB) { file ->
            val root = jsonReader.readNode(file)
            I18nTranslations(
                recruit = root.readTranslationSection("recruit"),
                profession = root.readTranslationSection("profession"),
                subProfession = root.readTranslationSection("sub_profession"),
            )
        }
    }

    override suspend fun getOperatorBasicInfoMap(): Result<Map<String, OperatorBasicInfo>> {
        return gameResourceStore.load(ConfigType.CHAR_MAP) { file ->
            val root = jsonReader.readNode(file)

            val charInfoMapNode = root["charInfoMap"]
                ?: throw FileNotFoundException("char_info_map file missing charInfoMap")

            charInfoMapNode.fields().asSequence()
                .associate { (charId, charInfo) ->
                    charId to OperatorBasicInfo(
                        charId = charId,
                        skinId = "$charId#1",
                        name = charInfo["name"]?.asText().orEmpty(),
                        rarity = charInfo["rarity"]?.asInt() ?: 0,
                        profession = charInfo["profession"]?.asText().orEmpty(),
                    )
                }
        }
    }

    override suspend fun getResourceDate(type: ConfigType): Result<String> {
        return Result.success(
            gameResourceStore.getResourceDate(type)
        )
    }

    override fun clearResourceCache(type: ConfigType) {
        gameResourceStore.clearCache(type)
    }

    private fun com.fasterxml.jackson.databind.JsonNode.readTranslationSection(
        name: String,
    ): Map<String, String> {
        val section = this[name] ?: throw FileNotFoundException("I18n file missing $name")
        return section.fields().asSequence().associate { (key, value) ->
            key.trim() to value.asText()
        }
    }
}
