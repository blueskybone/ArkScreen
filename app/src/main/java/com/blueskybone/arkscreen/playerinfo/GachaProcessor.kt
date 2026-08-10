package com.blueskybone.arkscreen.playerinfo

import android.net.Uri
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.room.Gacha
import com.blueskybone.arkscreen.room.GachaWithNum
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStrYMD
import com.fasterxml.jackson.databind.ObjectMapper
import com.hjq.toast.Toaster
import timber.log.Timber

/**
 * Created by Godot
 * Date: 2026/1/29
 */
//class GachaProcessor {
//
//    fun processGachaCount(recordsDb: List<Gacha>): List<GachaWithNum> {
//        val gachaList = mutableListOf<GachaWithNum>()
//        // 先按照Gacha.poolId分类，然后在每一个list中倒序排序List<Gacha>.sortByTsAndPosDescending().reversed()。
//        // 然后遍历，维护两个值countSum和countNum。具体规则：countSum每次+1；countNum每次+1,遇到rarity == 6时归零。然后创建GachaWithNum
//        // 最后把所有的list再次收集起来,倒序排序返回
//        recordsDb.groupBy { it.poolId }.map { (_, list) ->
//            val newList = list.sortByTsAndPosDescending().reversed()
//            var countSum = 0
//            var countNum = 0
//            for (record in newList) {
//                countSum++
//                countNum++
//                gachaList.add(GachaWithNum(record, countNum, countSum))
//                if (record.rarity == 5) countNum = 0
//            }
//        }
//        return gachaList.sortDescending()
//    }
//
//    fun convertRecordsToList(recordsDb: List<Gacha>): List<Gachas> {
//        val data = recordsDb.sortByTsAndPosDescending()
//        if (data.isEmpty()) return listOf()
//        val dateRange =
//            getTimeStrYMD(data.last().ts / 1000) + "-" + getTimeStrYMD(data.first().ts / 1000)
//
//        //分类
//        val groupByCate = data.groupBy { it.poolCate }
//        val limited = groupByCate["LIMITED"] ?: listOf()
//        val normal = groupByCate["NORMAL"] ?: listOf()
//        val classic = groupByCate["CLASSIC"] ?: listOf()
//        if (groupByCate["UN"] != null) Toaster.show("存在未知卡池，请注意修正")
//        val finalCountSum = data.size
//
//        //处理标准池
//        val normalIdx = normal.withIndex()
//            .filter { it.value.rarity == 5 }
//            .map { it.index }
//        //generate <pool, records>Map.
//        val mapNormal = normal.distinctBy { it.poolId }
//            .associateBy(
//                keySelector = { it.poolId },
//                valueTransform = { Gachas(pool = it.pool) }
//            ).toMutableMap()
//        val rarity6Count += normalIdx.size
//        processCateGachaRecords(normal, normalIdx, mapNormal)
//        poolCountNormal = normalIdx.firstOrNull() ?: normal.size
//
//        //处理中坚池
//        val classicIdx = classic.withIndex()
//            .filter { it.value.rarity == 5 }
//            .map { it.index }
//        rarity6Count += classicIdx.size
//        val mapClassic = classic.distinctBy { it.poolId }
//            .associateBy(
//                keySelector = { it.poolId },
//                valueTransform = { Gachas(pool = it.pool) }
//            ).toMutableMap()
//
//        processCateGachaRecords(classic, classicIdx, mapClassic)
//        poolCountCore = classicIdx.firstOrNull() ?: classic.size
//        //单独处理限定池
//
//        val mapLimited = limited.distinctBy { it.poolId }
//            .associateBy(
//                keySelector = { it.poolId },
//                valueTransform = { Gachas(pool = it.pool, isFes = true) }
//            ).toMutableMap()
//        val oneLimitGacha = limited.groupBy { it.poolId }
//        oneLimitGacha.forEach { (poolId, gacha) ->
//            val gachaIdx = gacha.withIndex()
//                .filter { it.value.rarity == 5 }
//                .map { it.index }
//            rarity6Count += gachaIdx.size
//
//            processCateGachaRecords(gacha, gachaIdx, mapLimited)
//            if (poolId == limited.first().poolId) {
//                poolCountFes = gachaIdx.firstOrNull() ?: gacha.size
//            }
//        }
//
//        val mapAll = mapLimited + mapClassic + mapNormal
//        //给卡池TS赋值用于最终结果排序
//        val allPoolGacha = data.groupBy { it.poolId }
//        allPoolGacha.forEach { (poolId, list) ->
//            list.sortedByDescending { it.ts }
//            mapAll[poolId]?.ts = list.first().ts
//            mapAll[poolId]?.count = list.size
//        }
//
//
//        //mapAll转List
//        return mapAll.values
//            .toList()
//            .sortedByDescending { it.ts }
//    }
//    fun parseJson(json: String): List<Gacha> {  }
//
//
//
//    //处理中坚池和标准池
//    private fun processCateGachaRecords(
//        gachaList: List<Gacha>,         //待处理的全部records
//        filteredIndices: List<Int>,     //出货的record在records的顺序坐标
//        targetMap: MutableMap<String, Gachas>   //对应的卡池Map
//    ) {
//        // 处理中间记录
//        filteredIndices.windowed(2, 1).forEach { (currentIdx, nextIdx) ->
//            val count = nextIdx - currentIdx
//            val record = gachaList[currentIdx]
//            targetMap[record.poolId]?.data?.add(
//                Records(id++, record.charName, record.charId, record.isNew, count, record.ts)
//            )
//        }
//
//        // 处理最后一条记录
//        if (filteredIndices.isNotEmpty()) {
//            val lastIdx = filteredIndices.last()
//            val record = gachaList[lastIdx]
//            val count = gachaList.size - lastIdx
//            targetMap[record.poolId]?.data?.add(
//                Records(id++, record.charName, record.charId, record.isNew, count, record.ts)
//            )
//        }
//    }
//
//    private fun generateCustomJson(dataList: List<Gacha>, uid: String): String {
//        val mapper = ObjectMapper()
//        val root = mapper.createObjectNode()
//
//        val info = root.putObject("info")
//        info.put("uid", uid)
//        info.put("export_timestamp", System.currentTimeMillis())
//        info.put("export_app", "arkscreen")
//
//        val dataArray = root.putArray("data")
//        dataList.forEach { gacha ->
//            val gachaNode = dataArray.addObject()
//            gachaNode.put("poolId", gacha.poolId)
//            gachaNode.put("poolCate", gacha.poolCate)
//            gachaNode.put("ts", gacha.ts)
//            gachaNode.put("pool", gacha.pool)
//            gachaNode.put("charName", gacha.charName)
//            gachaNode.put("charId", gacha.charId)
//            gachaNode.put("rarity", gacha.rarity)
//            gachaNode.put("isNew", gacha.isNew)
//            gachaNode.put("pos", gacha.pos)
//        }
//        return mapper.writeValueAsString(root)
//    }
//
//
//    private fun readJsonFile(uri: Uri): List<Gacha> {
//        val gachaList = mutableListOf<Gacha>()
//        try {
//            APP.contentResolver.openInputStream(uri)?.use { inputStream ->
//                var dataNode = ObjectMapper().readTree(inputStream)
//                val infoNode = dataNode["info"]
//                val uid = infoNode.get("uid").asText()
//                dataNode = dataNode["data"]
//                dataNode.toList().forEach { record ->
//                    gachaList.add(
//                        Gacha(
//                            uid = uid,
//                            poolId = record.get("poolId").asText(),
//                            poolCate = record.get("poolCate").asText(),
//                            ts = record.get("ts").asLong(),
//                            pool = record.get("pool").asText(),
//                            charName = record.get("charName").asText(),
//                            charId = record.get("charId").asText(),
//                            rarity = record.get("rarity").asInt(),
//                            isNew = record.get("isNew").asBoolean(),
//                            pos = record.get("pos").asInt(),
//                        )
//                    )
//                }
//            }
//        } catch (e: Exception) {
//            Timber.e(e.message)
//            throw Exception(e.message)
//        }
//        return gachaList
//    }
//
//    private fun List<Gacha>.sortByTsAndPosDescending(): List<Gacha> {
//        return sortedWith(
//            compareByDescending<Gacha> { it.ts }
//                .thenByDescending { it.pos }
//        )
//    }
//    private fun List<GachaWithNum>.sortDescending(): List<GachaWithNum> {
//        return sortedWith(
//            compareByDescending<GachaWithNum> { it.gacha.ts }
//                .thenByDescending { it.gacha.pos }
//        )
//    }
//}