package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.domain.model.AppRemoteConfig
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class AppRemoteConfigParser(
    private val objectMapper: ObjectMapper,
) {
    fun parse(json: String): AppRemoteConfig {
        val root = objectMapper.readTree(json)
        if (root.path("schemaVersion").asInt(-1) != SUPPORTED_SCHEMA_VERSION) {
            throw IllegalStateException("不支持的远端配置版本")
        }

        val links = root.path("links")
        val groupId = links.path("qqGroup").path("groupId").textOrNull()
            ?.takeIf(QQ_GROUP_PATTERN::matches)
            ?: AppRemoteConfig.DEFAULT_QQ_GROUP_ID
        val bvid = links.path("recruitDemo").path("bvid").textOrNull()
            ?.takeIf(BVID_PATTERN::matches)
            ?: AppRemoteConfig.DEFAULT_RECRUIT_DEMO_BVID
        val cvId = links.path("manual").path("cvId").textOrNull()
            ?.takeIf(CV_ID_PATTERN::matches)
            ?: AppRemoteConfig.DEFAULT_MANUAL_CV_ID

        return AppRemoteConfig(
            qqGroupId = groupId,
            recruitDemoBvid = bvid,
            manualCvId = cvId,
        )
    }

    private fun JsonNode.textOrNull(): String? =
        takeUnless { isMissingNode || isNull }?.asText()?.trim()?.takeIf { it.isNotEmpty() }

    private companion object {
        const val SUPPORTED_SCHEMA_VERSION = 1
        val QQ_GROUP_PATTERN = Regex("""\d{5,12}""")
        val BVID_PATTERN = Regex("""BV[0-9A-Za-z]{10}""")
        val CV_ID_PATTERN = Regex("""\d+""")
    }
}
