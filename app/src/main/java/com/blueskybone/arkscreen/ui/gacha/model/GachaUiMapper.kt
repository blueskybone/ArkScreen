package com.blueskybone.arkscreen.ui.gacha.model

import com.blueskybone.arkscreen.platform.time.TimeUtils.getTimeStrYMD
import com.blueskybone.arkscreen.domain.model.gacha.Record as DomainRecord



object GachaUiMapper {

    fun map(records: List<DomainRecord>): GachaUiSnapshot {
        if (records.isEmpty()) {
            return GachaUiSnapshot(
                records = emptyList(),
                gachaPoolStats = emptyList(),
                gachaSummary = GachaSummary(),
                gachaPools = emptyList(),
                gachaOverview = GachaOverview()
            )
        }

        val sortedRecords = records.sortByTsAndPosDescending()
        val dateRange = buildDateRange(sortedRecords)

        val recordsByPoolId = sortedRecords.groupBy { it.poolId }
        val recordsByPoolCate = sortedRecords.groupBy { it.poolCate }

        val displayRecords = buildDisplayRecords(recordsByPoolId)
        val gachaPools = buildGachaPools(recordsByPoolId)
        val gachaPoolStats = buildGachaPoolStats(recordsByPoolId)
        val gachaSummary = buildGachaSummary(
            gachaPools = gachaPools,
            sortedRecords = sortedRecords,
            dateRange = dateRange
        )
        val gachaOverview = buildGachaOverview(
            dateRange = dateRange,
            sortedRecords = sortedRecords,
            recordsByPoolCate = recordsByPoolCate
        )

        return GachaUiSnapshot(
            records = displayRecords,
            gachaPoolStats = gachaPoolStats,
            gachaSummary = gachaSummary,
            gachaPools = gachaPools,
            gachaOverview = gachaOverview
        )
    }

    /*
    * 先统计每一次寻访记录对应的水位抽数
    * */
    private fun buildDisplayRecords(
        recordsByPoolId: Map<String, List<DomainRecord>>
    ): List<Record> {
        val result = mutableListOf<Record>()

        recordsByPoolId.forEach { (_, poolRecords) ->
            val ascending = poolRecords
                .sortByTsAndPosDescending()
                .reversed()

            var pityCount = 0
            var totalCount = 0

            ascending.forEach { record ->
                pityCount++
                totalCount++
                result += Record(
                    id = record.id,
                    name = record.charName,
                    charId = record.charId,
                    isNew = record.isNew,
                    count = pityCount,
                    ts = record.ts,
                    pos = record.pos,
                    rare = record.rarity,
                    poolId = record.poolId,
                    gachaPool = record.pool,
                    gachaCount = totalCount
                )
                if (record.rarity == 5) {
                    pityCount = 0
                }
            }
        }

        return result.sortedWith(
            compareByDescending<Record> { it.ts }
                .thenByDescending { it.pos }
        )
    }

    private fun buildGachaPools(
        recordsByPoolId: Map<String, List<DomainRecord>>
    ): List<GachaPool> {

        return recordsByPoolId.map { (poolId, poolRecords) ->
            val sorted = poolRecords.sortByTsAndPosDescending()

            val sixStarIndices = sorted.withIndex()
                .filter { it.value.rarity == 5 }
                .map { it.index }

            val segmentedRecords = mutableListOf<Record>()

            sixStarIndices.windowed(2, 1).forEach { (currentIdx, nextIdx) ->
                val hitRecord = sorted[currentIdx]
                val count = nextIdx - currentIdx

                segmentedRecords += Record(
                    id = hitRecord.id,
                    name = hitRecord.charName,
                    charId = hitRecord.charId,
                    isNew = hitRecord.isNew,
                    count = count,
                    ts = hitRecord.ts,
                    pos = hitRecord.pos,
                    rare = hitRecord.rarity,
                    poolId = hitRecord.poolId,
                    gachaPool = hitRecord.pool,
                    gachaCount = count
                )
            }

            if (sixStarIndices.isNotEmpty()) {
                val lastIdx = sixStarIndices.last()
                val hitRecord = sorted[lastIdx]
                val count = sorted.size - lastIdx

                segmentedRecords += Record(
                    id = hitRecord.id,
                    name = hitRecord.charName,
                    charId = hitRecord.charId,
                    isNew = hitRecord.isNew,
                    count = count,
                    ts = hitRecord.ts,
                    pos = hitRecord.pos,
                    rare = hitRecord.rarity,
                    poolId = hitRecord.poolId,
                    gachaPool = hitRecord.pool,
                    gachaCount = count
                )
            }

            GachaPool(
                poolName = sorted.firstOrNull()?.pool.orEmpty(),
                poolId = poolId,
                isFes = sorted.firstOrNull()?.poolCate.toGachaType() == GachaType.LIMITED,
                hitRecords = segmentedRecords,
                totalCount = sorted.size,
                ts = sorted.firstOrNull()?.ts ?: 0L
            )
        }.sortedByDescending { it.ts }
    }

    private fun buildGachaPoolStats(
        recordsByPoolId: Map<String, List<DomainRecord>>
    ): List<GachaPoolStats> {
        val stats = recordsByPoolId.map { (poolId, poolRecords) ->
            GachaPoolStats(
                poolName = poolRecords.firstOrNull()?.pool.orEmpty(),
                poolId = poolId,
                isFes = poolRecords.firstOrNull()?.poolCate.toGachaType() == GachaType.LIMITED,
                rare6 = poolRecords.count { it.rarity == 5 },
                rare5 = poolRecords.count { it.rarity == 4 },
                rare4 = poolRecords.count { it.rarity == 3 },
                rare3 = poolRecords.count { it.rarity == 2 }
            )
        }

        val allStats = GachaPoolStats(
            poolName = "全部卡池",
            poolId = "ALL",
            isFes = false,
            rare6 = recordsByPoolId.values.flatten().count { it.rarity == 5 },
            rare5 = recordsByPoolId.values.flatten().count { it.rarity == 4 },
            rare4 = recordsByPoolId.values.flatten().count { it.rarity == 3 },
            rare3 = recordsByPoolId.values.flatten().count { it.rarity == 2 }
        )

        return listOf(allStats) + stats.reversed()
    }

    private fun buildGachaSummary(
        gachaPools: List<GachaPool>,
        sortedRecords: List<DomainRecord>,
        dateRange: String
    ): GachaSummary {
        return GachaSummary(
            allPools = gachaPools,
            dateRange = dateRange,
            totalSum = sortedRecords.size,
            rare6Count = sortedRecords.count { it.rarity == 5 }
        )
    }

    private fun buildGachaOverview(
        dateRange: String,
        sortedRecords: List<DomainRecord>,
        recordsByPoolCate: Map<String, List<DomainRecord>>
    ): GachaOverview {
        val normal = recordsByPoolCate["NORMAL"].orEmpty()
        val classic = recordsByPoolCate["CLASSIC"].orEmpty()
        val limited = recordsByPoolCate["LIMITED"].orEmpty()

        return GachaOverview(
            poolCountNormal = currentPity(normal),
            poolCountCore = currentPity(classic),
            poolCountFes = currentPityForLimited(limited),
            totalCount = sortedRecords.size,
            rare6Count = sortedRecords.count { it.rarity == 5 },
            dateRange = dateRange
        )
    }

    private fun currentPity(records: List<DomainRecord>): Int {
        if (records.isEmpty()) return 0
        val index = records.indexOfFirst { it.rarity == 5 }
        return if (index == -1) records.size else index
    }

    private fun currentPityForLimited(records: List<DomainRecord>): Int {
        if (records.isEmpty()) return 0

        val latestPoolId = records.first().poolId
        val latestPoolRecords = records.filter { it.poolId == latestPoolId }

        val index = latestPoolRecords.indexOfFirst { it.rarity == 5 }
        return if (index == -1) latestPoolRecords.size else index
    }

    private fun String?.toGachaType(): GachaType {
        return when (this) {
            "NORMAL" -> GachaType.NORMAL
            "LIMITED" -> GachaType.LIMITED
            "CLASSIC" -> GachaType.CLASSIC
            "LINKAGE" -> GachaType.LIMITED
            else -> GachaType.UNKNOWN
        }
    }

    private fun List<DomainRecord>.sortByTsAndPosDescending(): List<DomainRecord> {
        return sortedWith(
            compareByDescending<DomainRecord> { it.ts }
                .thenByDescending { it.pos }
        )
    }

    private fun buildDateRange(records: List<DomainRecord>): String {
        if (records.isEmpty()) return "-"
        return getTimeStrYMD(records.last().ts / 1000) +
                "-" +
                getTimeStrYMD(records.first().ts / 1000)
    }
}
