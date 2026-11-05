package com.blueskybone.arkscreen.ui.gacha

import com.blueskybone.arkscreen.data.local.room.EndfieldGachaPool
import com.blueskybone.arkscreen.data.local.room.EndfieldGachaRecord

data class EndfieldPoolProgress(
    val key: String,
    val name: String,
    val total: Int,
    val free: Int,
    val six: Int,
    val five: Int,
    val smallCount: Int,
    val smallMax: Int?,
    val bigCount: Int?,
    val bigMax: Int?,
    val bigSatisfied: Boolean,
    val bigUsesOfficialCount: Boolean,
    val hits: List<EndfieldSixHit>,
)

data class EndfieldSixHit(
    val seqId: String,
    val name: String,
    val pity: Int,
    val isFree: Boolean,
    val isNew: Boolean,
    val gachaTs: String,
)

object EndfieldPityCalculator {
    private const val SPECIAL = "E_CharacterGachaPoolType_Special"
    private const val RERUN = "E_CharacterGachaPoolType_Rerun"
    private const val JOINT = "E_CharacterGachaPoolType_Joint"
    private const val STANDARD = "E_CharacterGachaPoolType_Standard"
    private const val BEGINNER = "E_CharacterGachaPoolType_Beginner"

    fun isGift(record: EndfieldGachaRecord): Boolean =
        record.kind.startsWith("gift", ignoreCase = true) ||
            record.itemName.contains("寻访情报书") || record.itemId.contains("寻访情报书")

    fun calculate(
        records: List<EndfieldGachaRecord>,
        pools: List<EndfieldGachaPool>,
        type: String,
    ): List<EndfieldPoolProgress> {
        val metadata = pools.filter { it.type == type }.associateBy { it.poolId }
        val chronological = records.asSequence().filter { it.type == type && !isGift(it) }
            .sortedWith(compareBy<EndfieldGachaRecord> { it.gachaTs.toLongOrNull() ?: 0L }
                .thenBy { it.seqId.length }.thenBy { it.seqId }).toList()
        val stats = linkedMapOf<String, MutableProgress>()
        val sharedSmall = mutableMapOf<String, Int>()
        for (record in chronological) {
            val key = key(record)
            val stat = stats.getOrPut(key) {
                MutableProgress(key, record.poolName.ifBlank { key }, record.poolType,
                    record.type, isWeaponRerun(record))
            }
            if (record.poolName.isNotBlank()) stat.name = record.poolName
            stat.poolIds += record.poolId
            val smallKey = when {
                record.type == "char" && record.poolType in setOf(SPECIAL, RERUN) -> record.poolType
                else -> key
            }
            // 重构申领的 4 次保底只在当前版本生效。
            if (record.type == "weapon" && isWeaponRerun(record)) {
                if (stat.lastVersion != null && record.poolVersion != null &&
                    stat.lastVersion != record.poolVersion
                ) sharedSmall[smallKey] = 0
                if (record.poolVersion != null) stat.lastVersion = record.poolVersion
            }
            stat.total++
            if (record.isFree) stat.free++ else {
                stat.paid++
                sharedSmall[smallKey] = (sharedSmall[smallKey] ?: 0) + 1
            }
            if (record.rarity == 6) {
                stat.six++
                stat.hits += EndfieldSixHit(
                    seqId = record.seqId,
                    name = record.itemName.ifBlank { record.itemId },
                    pity = if (record.isFree) 0 else sharedSmall[smallKey] ?: 0,
                    isFree = record.isFree,
                    isNew = record.isNew,
                    gachaTs = record.gachaTs,
                )
                if (!record.isFree) sharedSmall[smallKey] = 0
            } else if (record.rarity == 5) stat.five++
            stat.smallCount = sharedSmall[smallKey] ?: 0
            stat.latestSequence = record.seqId
            if (record.rarity == 6) stat.sixIds += record.itemId
        }
        return stats.values.reversed().map { stat ->
            val related = stat.poolIds.mapNotNull(metadata::get)
            val upIds = related.flatMap { it.up6Ids.split(',') }.filter(String::isNotBlank).toSet()
            val gotUp = stat.sixIds.any(upIds::contains)
            val bigMax = when {
                upIds.isEmpty() -> null
                type == "weapon" -> 80
                stat.poolType == SPECIAL || stat.poolType == RERUN -> 120
                else -> null
            }
            val official = if (stat.poolType == RERUN || stat.isWeaponRerun) {
                related.mapNotNull { it.officialCount }.maxOrNull()
            } else null
            val bigCount = if (bigMax == null) null else if (gotUp) 0
                else (official ?: stat.paid).coerceAtMost(bigMax)
            EndfieldPoolProgress(
                key = stat.key,
                name = stat.name,
                total = stat.total,
                free = stat.free,
                six = stat.six,
                five = stat.five,
                smallCount = stat.smallCount,
                smallMax = when {
                    type == "weapon" -> 40
                    stat.poolType == BEGINNER -> 40
                    stat.poolType in setOf(SPECIAL, RERUN, JOINT, STANDARD) -> 80
                    else -> null
                },
                bigCount = bigCount,
                bigMax = bigMax,
                bigSatisfied = gotUp,
                bigUsesOfficialCount = official != null,
                hits = stat.hits.reversed(),
            )
        }
    }

    private fun key(record: EndfieldGachaRecord): String = when {
        record.type == "weapon" -> record.poolId.ifBlank { record.poolName }
        record.poolType == RERUN -> record.poolName.ifBlank { record.poolId }
        record.poolType == STANDARD || record.poolType == BEGINNER -> record.poolType
        else -> record.poolId.ifBlank { record.poolType }
    }

    private fun isWeaponRerun(record: EndfieldGachaRecord): Boolean =
        record.poolType == "rerun" || record.poolId.startsWith("rerun", ignoreCase = true)

    private class MutableProgress(
        val key: String,
        var name: String,
        val poolType: String,
        val type: String,
        val isWeaponRerun: Boolean,
    ) {
        var total = 0
        var paid = 0
        var free = 0
        var six = 0
        var five = 0
        var smallCount = 0
        var latestSequence = ""
        var lastVersion: Int? = null
        val poolIds = mutableSetOf<String>()
        val sixIds = mutableSetOf<String>()
        val hits = mutableListOf<EndfieldSixHit>()
    }
}
