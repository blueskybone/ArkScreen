package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.data.local.room.EndfieldGachaRecord
import com.blueskybone.arkscreen.data.local.room.EndfieldGachaPool
import com.blueskybone.arkscreen.data.local.room.dao.AccountEfDao
import com.blueskybone.arkscreen.data.local.room.dao.EndfieldGachaDao
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/** 终末地寻访接口与本地记录。凭证仅用于请求，不写入寻访记录。 */
class EndfieldGachaRepository(
    private val dao: EndfieldGachaDao,
    private val accountDao: AccountEfDao,
    private val mapper: ObjectMapper,
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun observe(roleId: String): Flow<List<EndfieldGachaRecord>> = dao.observe(roleId)
    fun observePools(roleId: String): Flow<List<EndfieldGachaPool>> = dao.observePools(roleId)

    suspend fun sync(account: AccountEf, progress: (String) -> Unit): Int = withContext(Dispatchers.IO) {
        if (!account.official) error("目前仅支持鹰角官服账号的寻访记录")
        val oauth = post("https://as.hypergryph.com/user/oauth2/v2/grant",
            mapper.createObjectNode().put("type", 1)
                .put("appCode", "be36d44aa36bfb5b").put("token", account.token))
            .path("data").path("token").asText()
        if (oauth.isBlank()) error("账号授权失败，请重新导入账号")
        val bindingUid = account.bindingUid?.takeIf(String::isNotBlank)
            ?: resolveBindingUid(account, oauth).also { accountDao.updateBindingUid(account.roleId, it) }
        val u8 = post(
            "https://binding-api-account-prod.hypergryph.com/account/binding/v1/u8_token_by_uid",
            mapper.createObjectNode().put("uid", bindingUid).put("token", oauth),
        ).path("data").path("token").asText()
        if (u8.isBlank()) error("寻访授权失败，请重新导入账号")

        val base = "https://ef-webview.hypergryph.com/api/record"
        // 鹰角国服寻访接口使用固定 server_id=1，与森空岛角色 serverId 不同。
        val query = mapOf("lang" to "zh-cn", "token" to u8, "server_id" to "1")
        val charMeta = get("$base/char/meta", query).path("data").path("tabs")
        val charTypes = charMeta.mapNotNull { it.path("poolType").asText().takeIf(String::isNotBlank) }
            .distinct().ifEmpty { error("没有取得角色卡池列表") }
        val weapons = get("$base/weapon/pool", query).path("data")
        if (!weapons.isArray) error("没有取得武器卡池列表")
        val weaponPools = weapons.mapNotNull { node ->
            node.path("poolId").asText().takeIf(String::isNotBlank)?.let {
                Triple(it, node.path("poolName").asText(), node.path("poolType").asText())
            }
        }

        val existing = dao.getAll(account.roleId)
        fun latest(type: String, matches: (EndfieldGachaRecord) -> Boolean): String = existing
            .asSequence().filter { it.type == type && matches(it) }.map { it.seqId }
            .maxWithOrNull(::compareSeqId).orEmpty()
        val all = mutableListOf<EndfieldGachaRecord>()
        for (poolType in charTypes) {
            progress("角色寻访 · ${charPoolName(poolType)}")
            all += fetchPages("$base/char", query + ("pool_type" to poolType), account.roleId,
                "char", poolType, stopSeqId = latest("char") { it.poolType == poolType })
        }
        val (rerunPools, normalPools) = weaponPools.partition { (poolId, _, poolType) ->
            poolType == "rerun" || poolId.startsWith("rerun", ignoreCase = true)
        }
        for ((poolId, poolName, _) in normalPools) {
            progress("武器申领 · ${poolName.ifBlank { poolId }}")
            all += fetchPages("$base/weapon", query + ("pool_id" to poolId), account.roleId,
                "weapon", "", poolName, latest("weapon") { it.poolId == poolId })
        }
        if (rerunPools.isNotEmpty()) {
            progress("武器申领 · 重构申领")
            all += fetchPages(
                "$base/weapon",
                query + ("pool_ids" to rerunPools.joinToString(",") { it.first }),
                account.roleId, "weapon", "", "重构申领",
                latest("weapon") { record -> rerunPools.any { it.first == record.poolId } },
            )
        }
        dao.upsert(all.distinctBy { Triple(it.roleId, it.type, it.seqId) })
        progress("正在读取卡池保底信息…")
        syncPoolInfo(account.roleId, query, (existing + all).filterNot(::isGiftRecord))
        all.size
    }

    private suspend fun resolveBindingUid(account: AccountEf, oauth: String): String {
        val json = get(
            "https://binding-api-account-prod.hypergryph.com/account/binding/v1/binding_list",
            mapOf("token" to oauth, "appCode" to "endfield"),
        )
        val app = json.path("data").path("list")
            .firstOrNull { it.path("appCode").asText() == "endfield" }
            ?: error("未找到终末地绑定账号，请重新导入账号")
        val bindings = app.path("bindingList")
        return bindings.firstOrNull { binding ->
            binding.path("roles").any { it.path("roleId").asText() == account.roleId }
        }?.path("uid")?.asText()?.takeIf(String::isNotBlank)
            ?: error("未找到当前角色的绑定账号，请重新导入账号")
    }

    private suspend fun fetchPages(
        url: String,
        query: Map<String, String>,
        roleId: String,
        type: String,
        poolType: String,
        fallbackPoolName: String = "",
        stopSeqId: String = "",
    ): List<EndfieldGachaRecord> {
        val result = mutableListOf<EndfieldGachaRecord>()
        var cursor = ""
        repeat(500) { page ->
            val body = get(url, query + (if (cursor.isBlank()) emptyMap() else mapOf("seq_id" to cursor)))
                .path("data")
            val list = body.path("list")
            if (!list.isArray) error("寻访记录格式不正确")
            if (list.isEmpty) return result
            var reachedCached = false
            list.forEach { item ->
                val seqId = item.path("seqId").asText()
                if (seqId.isBlank()) error("寻访记录缺少序号")
                if (stopSeqId.isNotBlank() && compareSeqId(seqId, stopSeqId) <= 0) {
                    reachedCached = true
                    return@forEach
                }
                result += EndfieldGachaRecord(
                    roleId = roleId,
                    type = type,
                    seqId = seqId,
                    poolId = item.path("poolId").asText(),
                    poolName = item.path("poolName").asText().ifBlank { fallbackPoolName },
                    poolType = if (type == "char") poolType else item.path("poolType").asText(),
                    itemId = item.path(if (type == "char") "charId" else "weaponId").asText(),
                    itemName = item.path("nameText").asText().ifBlank {
                        item.path(if (type == "char") "charName" else "weaponName").asText()
                    },
                    rarity = item.path("rarity").asInt(),
                    gachaTs = item.path("gachaTs").asText(),
                    kind = item.path("kind").asText(),
                    isNew = item.path("isNew").asBoolean(),
                    isFree = item.path("isFree").asBoolean(),
                    poolVersion = item.path("poolVersion").takeIf { it.isNumber }?.asInt(),
                )
            }
            if (reachedCached) return result
            if (!body.path("hasMore").asBoolean()) return result
            val next = list.last().path("seqId").asText()
            if (next.isBlank() || next == cursor) error("寻访分页游标异常")
            cursor = next
            if (page < 499) delay(350)
        }
        error("寻访记录页数超出限制")
    }

    private fun get(url: String, query: Map<String, String>): JsonNode {
        val builder = url.toHttpUrl().newBuilder()
        query.forEach { (key, value) -> builder.addQueryParameter(key, value) }
        return execute(Request.Builder().url(builder.build()).get().build())
    }

    private fun post(url: String, body: JsonNode): JsonNode {
        val request = Request.Builder().url(url)
            .post(mapper.writeValueAsString(body).toRequestBody("application/json".toMediaType()))
            .build()
        return execute(request)
    }

    private fun execute(request: Request): JsonNode {
        // 不把带 token 的 URL 或响应原文放进错误信息。
        val response = try { client.newCall(request).execute() } catch (_: Exception) {
            error("连接寻访服务器失败")
        }
        response.use {
            if (!it.isSuccessful) error("寻访服务器返回 HTTP ${it.code}")
            val node = try { mapper.readTree(it.body?.charStream()) } catch (_: Exception) {
                error("寻访服务器响应无法解析")
            }
            if (node == null) error("寻访服务器响应为空")
            if (node.has("code") && node.path("code").asInt(-1) != 0) {
                error("寻访接口返回错误代码 ${node.path("code").asInt()}")
            }
            if (node.has("status") && node.path("status").asInt(-1) != 0) {
                error("寻访授权返回错误代码 ${node.path("status").asInt()}")
            }
            return node
        }
    }

    private fun charPoolName(type: String): String = when (type) {
        "E_CharacterGachaPoolType_Special" -> "特许寻访"
        "E_CharacterGachaPoolType_Rerun" -> "重构寻访"
        "E_CharacterGachaPoolType_Joint" -> "辉光庆典"
        "E_CharacterGachaPoolType_Standard" -> "基础寻访"
        "E_CharacterGachaPoolType_Beginner" -> "启程寻访"
        else -> type
    }

    private fun compareSeqId(a: String, b: String): Int =
        a.length.compareTo(b.length).takeIf { it != 0 } ?: a.compareTo(b)

    private suspend fun syncPoolInfo(
        roleId: String,
        query: Map<String, String>,
        records: List<EndfieldGachaRecord>,
    ) {
        val previous = dao.getPools(roleId).associateBy { it.type to it.poolId }
        val counts = mapOf("char" to fetchOfficialCounts("char", query),
            "weapon" to fetchOfficialCounts("weapon", query))
        val pools = records.filter { it.poolId.isNotBlank() }
            .distinctBy { it.type to it.poolId }
            .map { record ->
                val key = record.type to record.poolId
                val old = previous[key]
                val info = runCatching {
                    get("https://ef-webview.hypergryph.com/api/content",
                        mapOf("lang" to "zh-cn", "server_id" to "1", "pool_id" to record.poolId))
                        .path("data").path("pool")
                }.getOrNull()
                val upIds = info?.let(::readUp6Ids).orEmpty()
                EndfieldGachaPool(
                    roleId = roleId,
                    type = record.type,
                    poolId = record.poolId,
                    poolName = info?.path("pool_name")?.asText()
                        ?.takeIf(String::isNotBlank) ?: record.poolName,
                    up6Ids = upIds.takeIf(String::isNotBlank) ?: old?.up6Ids.orEmpty(),
                    officialCount = counts[record.type]?.get(record.poolId) ?: old?.officialCount,
                )
            }
        dao.upsertPools(pools)
    }

    private fun fetchOfficialCounts(type: String, query: Map<String, String>): Map<String, Int> =
        runCatching {
            val entries = get("https://ef-webview.hypergryph.com/api/record/$type/rerun-counts", query)
                .path("data")
            if (!entries.isArray) return@runCatching emptyMap()
            entries.mapNotNull { node ->
                node.path("poolId").asText().takeIf(String::isNotBlank)?.let { id ->
                    id to node.path("totalPullCount").asInt().coerceAtLeast(0)
                }
            }.toMap()
        }.getOrDefault(emptyMap())

    private fun readUp6Ids(pool: JsonNode): String {
        val all = pool.path("all")
        val names = if (pool.path("pool_type").asText() == "extra") {
            pool.path("rotate_reward_item_list").map { item ->
                item.asText().removeSuffix("的信物").trim()
            }
        } else listOf(pool.path("up6_name").asText())
        return names.filter(String::isNotBlank).mapNotNull { name ->
            all.firstOrNull { it.path("name").asText() == name && it.path("rarity").asInt() == 6 }
                ?.path("id")?.asText()?.takeIf(String::isNotBlank)
                ?: all.firstOrNull { it.path("name").asText() == name }
                    ?.path("id")?.asText()?.takeIf(String::isNotBlank)
        }.distinct().joinToString(",")
    }

    private fun isGiftRecord(record: EndfieldGachaRecord): Boolean =
        record.kind.startsWith("gift", ignoreCase = true) ||
            record.itemName.contains("寻访情报书") || record.itemId.contains("寻访情报书")
}
