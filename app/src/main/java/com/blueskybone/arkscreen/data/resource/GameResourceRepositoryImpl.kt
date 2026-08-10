package com.blueskybone.arkscreen.data.resource


import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.model.ResourceSyncStatus
import com.blueskybone.arkscreen.domain.model.operator.OperatorBasicInfo
import com.blueskybone.arkscreen.domain.model.recruit.RecruitDatabase
import com.blueskybone.arkscreen.domain.repository.GameResourceRepository
import com.fasterxml.jackson.databind.JsonNode
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
                clazz = RecruitDatabase::class.java,
            )
        }
    }

    override suspend fun getI18nDb(): Result<Map<String, String>> {
        return gameResourceStore.load(ConfigType.I18N_DB) { file ->
            val root = jsonReader.readNode(file)

            val mapInfoNode = root["mapInfo"]
                ?: throw FileNotFoundException("I18n file missing mapInfo")

            mapInfoNode.fields().asSequence()
                .associate { (key, value) ->
                    key.trim() to value.asText()
                }
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

//    override suspend fun getCharInfoMap(): Result<JsonNode> {
//        return gameResourceStore.load(ConfigType.CHAR_MAP) { file ->
//            val root = jsonReader.readNode(file)
//
//            root["charInfoMap"]
//                ?: throw FileNotFoundException("char_info_map file missing charInfoMap")
//        }
//    }

    override suspend fun getResourceDate(type: ConfigType): Result<String> {
        return Result.success(
            gameResourceStore.getResourceDate(type)
        )
    }

    override fun clearResourceCache(type: ConfigType) {
        gameResourceStore.clearCache(type)
    }
}