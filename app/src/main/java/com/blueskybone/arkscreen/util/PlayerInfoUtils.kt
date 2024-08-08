package com.blueskybone.arkscreen.util

import com.blueskybone.arkscreen.I18n
import com.blueskybone.arkscreen.base.data.Operator
import com.blueskybone.arkscreen.base.data.PlayerData
import com.blueskybone.arkscreen.base.data.PlayerInfo
import com.blueskybone.arkscreen.util.TimeUtils.getRemainTimeStr
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStr
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.koin.java.KoinJavaComponent.getKoin
import java.text.Collator
import java.util.zip.GZIPInputStream

/**
 *   Created by blueskybone
 *   Date: 2024/8/6
 */

// @Deprecated
// 反序列化：charinfomap的每一个干员单独为一个子类，不能用这种方法
// 最后也要单独解析每个数据，还是得手动古法解析json
fun readPlayerInfoData(gzip: GZIPInputStream): PlayerInfo {
    val objectMapper =
        ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
        )
    return objectMapper.readValue(gzip, PlayerInfo::class.java)
}

fun getPlayerData(tree: JsonNode): PlayerData {
    val currentTs = tree.at("/data/currentTs").asLong()
    val playerData = PlayerData()
    //status
    run {
        playerData.playerStatus.uid = tree.at("/data/status/uid").asText()
        playerData.playerStatus.nickname = tree.at("/data/status/name").asText()
        playerData.playerStatus.level = tree.at("/data/status/level").asInt()
        playerData.playerStatus.registerTs = tree.at("/data/status/registerTs").asLong()
        playerData.playerStatus.lastOnlineTs = tree.at("/data/status/lastOnlineTs").asLong()
    }
    //ap
    run {
        val current: Int = tree.at("/data/status/ap/current").asInt()
        val max: Int = tree.at("/data/status/ap/max").asInt()
        val lastApAddTime: Long = tree.at("/data/status/ap/lastApAddTime").asLong()
        val recoverTime: Long = tree.at("/data/status/ap/completeRecoveryTime").asLong()
        playerData.apInfo.max = max
        if (current >= max) {
            playerData.apInfo.current = current
            playerData.apInfo.remainSecs = -1L
            playerData.apInfo.recoverTime = -1L
        } else if (recoverTime < currentTs) {
            playerData.apInfo.current = max
            playerData.apInfo.remainSecs = -1L
            playerData.apInfo.recoverTime = -1L
        } else {
            playerData.apInfo.current = (currentTs - lastApAddTime).toInt() / (60 * 6) + current
            playerData.apInfo.remainSecs = recoverTime - currentTs
            playerData.apInfo.recoverTime = recoverTime
        }
        if (playerData.apInfo.current >= max) {
            playerData.apInfo.remainSecsStr = "已恢复"
            playerData.apInfo.recoverTimeStr = "已恢复"
        } else {
            playerData.apInfo.recoverTimeStr = getTimeStr(recoverTime * 1000)
            playerData.apInfo.remainSecsStr =
                getRemainTimeStr(playerData.apInfo.remainSecs)
        }
    }

    //training
    run {
        val nodeTrain = tree.at("/data/building/training")
        val nodeChar = tree.at("/data/charInfoMap")

        playerData.train.isNull = nodeTrain.isNull
        if (playerData.train.isNull) return@run

        val trainee: JsonNode = nodeTrain.get("trainee")
        playerData.train.traineeIsNull = trainee.isNull
        val trainer: JsonNode = nodeTrain.get("trainer")
        playerData.train.trainerIsNull = trainer.isNull

        val lastUpdateTime = nodeTrain["lastUpdateTime"].asLong()

        if (!playerData.train.traineeIsNull) {
            val traineeCode: String = trainee.get("charId").asText()
            playerData.train.trainee = nodeChar.get(traineeCode).get("name").asText()
            playerData.train.profession =
                nodeChar.get(traineeCode).get("profession").asText()
            playerData.train.targetSkill = trainee.get("targetSkill").asInt() + 1
        }
        if (!playerData.train.trainerIsNull) {
            val trainerCode: String = trainer.get("charId").asText()
            playerData.train.trainer = nodeChar.get(trainerCode).get("name").asText()
        }
        val remainSecs = nodeTrain.get("remainSecs").asLong()
        playerData.train.remainSecs = remainSecs
        playerData.train.completeTime = playerData.train.remainSecs + currentTs


        when (remainSecs) {
            //专精完成
            0L -> {
                playerData.train.totalPoint = 1L
                playerData.train.remainPoint = 0L
            }

            //空闲中
            -1L -> {
                playerData.train.totalPoint = 1L
                playerData.train.remainPoint = 1L
            }

            else -> {
                playerData.train.remainPoint =
                    (nodeTrain["remainSecs"].asDouble() * nodeTrain["speed"]
                        .asDouble()).toLong()

                val totalPoint =
                    ((currentTs - lastUpdateTime).toDouble() * nodeTrain["speed"]
                        .asDouble()).toLong() + playerData.train.remainPoint
                playerData.train.totalPoint = getTotalPoint(totalPoint)
                //playerData.train.totalPoint = totalPoint
                var targetPoint = 18900L
                var targetPoint2 = 18900L
                if (playerData.train.profession == "SNIPER" ||
                    playerData.train.profession == "WARRIOR"
                ) targetPoint = 24300L
                if (playerData.train.profession == "CASTER" ||
                    playerData.train.profession == "SUPPORT"
                ) targetPoint2 = 24300L
                val remainPoint = playerData.train.remainPoint
                if (remainPoint > targetPoint) {
                    val secs =
                        (remainPoint - targetPoint) / nodeTrain.get("speed").asDouble()
                    playerData.train.changeRemainSecs = secs.toLong()
                    playerData.train.changeTime = currentTs + secs.toLong()
                }
                if (remainPoint > targetPoint2) {
                    val secs =
                        (remainPoint - targetPoint2) / nodeTrain.get("speed").asDouble()
                    println("secs = $secs")
                    playerData.train.changeRemainSecsLogos = secs.toLong()
                    playerData.train.changeTimeLogos = currentTs + secs.toLong()
                }
            }
        }

    }

    //recruit
    run {
        val recruitNode = tree.at("/data/recruit")
        playerData.recruits.isNull = recruitNode.isNull
        if (recruitNode.isNull) return@run
        var unable = 0
        var complete = 0
        var finishTs = -1L
        for (i in 0..3) {
            val node = recruitNode.get(i)
            when (node["state"].asInt()) {
                0 -> {
                    unable++
                }

                3 -> {
                    complete++
                }

                2 -> {
                    val finish = node["finishTs"].asLong()
                    if (finish < currentTs) complete++
                    finishTs = finish.coerceAtLeast(finishTs)
                }
            }
            if (finishTs == -1L || finishTs < currentTs) {
                playerData.recruits.remainSecs = -1
            } else {
                playerData.recruits.remainSecs = (finishTs - currentTs)
                playerData.recruits.completeTime = finishTs
            }
            playerData.recruits.max = 4 - unable
            playerData.recruits.complete = complete
        }
    }

    //hire
    run {
        val hireNode = tree.at("/data/building/hire")
        playerData.hire.isNull = hireNode.isNull
        if (hireNode.isNull) return@run
        val count = hireNode["refreshCount"].asInt()
        val remainSecs = hireNode["completeWorkTime"].asLong() - currentTs
        val completeTime = hireNode["completeWorkTime"].asLong()
        if (remainSecs < 0) {
            playerData.hire.completeTime = -1L
            playerData.hire.remainSecs = -1L
            playerData.hire.count = (count + 1).coerceAtMost(3)
        } else {
            playerData.hire.completeTime = completeTime
            playerData.hire.remainSecs = remainSecs
            playerData.hire.count = count
        }
    }

    //meeting
    run {
        val meetingNode = tree.at("/data/building/meeting")
        playerData.meeting.isNull = meetingNode.isNull
        if (meetingNode.isNull) return@run
        val sharing = meetingNode.at("/clue/sharing").asBoolean()
        val shareCompleteTime = meetingNode.at("/clue/shareCompleteTime").asLong()
        if (!sharing) {
            playerData.meeting.status = 0
        } else {
            if (shareCompleteTime > currentTs) {
                playerData.meeting.status = 1
                playerData.meeting.completeTime = shareCompleteTime
                playerData.meeting.remainSecs = shareCompleteTime - currentTs
            } else {
                playerData.meeting.status = 2
            }
        }
        playerData.meeting.current = meetingNode.at("/clue/board").size()
    }

    //tradings
    run {
        val tradingsNode = tree.at("/data/building/tradings")
        playerData.tradings.isNull = tradingsNode.isNull
        if (tradingsNode.isNull) return@run
        var stockSum = 0
        var stockLimitSum = 0
        var completeTimeAll = -1L
        var remainSecsAll = -1L

        for (node in tradingsNode) {
            val trade = PlayerData.Trade()
            val completeTime = node["completeWorkTime"].asLong()
            val lastUpdateTime = node["lastUpdateTime"].asLong()
            val stockLimit = node["stockLimit"].asInt()
            val rawStock = node["stock"].size()
            val strategy = node["strategy"].asText()    //O_GOLD    O_DIAMOND
            var stock = rawStock
            /*
            * 由于森空岛接口返回的信息缺少基建技能的效率加成数据，
            * 无法准确计算贸易站订单数，只能采用估计speed的方法。
            * targetPoint是效率为190%的假设下单个订单所需时间。
            * 所以最后的估计订单数绝大部分情况下比真实值要高一些。
            * 获取订单的正常时长：赤金3.5h,源石碎片2h。
            * */
            val speed = 1.9F
            val targetPoint = when (strategy) {
                "O_GOLD" -> 6632L  //3.5 * 3600 / speed
                else -> 3789L      //2 * 3600 / speed
            }

            val geneStock = (completeTime - lastUpdateTime) / targetPoint
            stock += geneStock.toInt()
            if (geneStock.toInt() > 0 && currentTs < completeTime) stock--
            else {
                val newStock = (currentTs - completeTime) / targetPoint
                stock += newStock.toInt() + 1
            }
            if (stock > stockLimit) {
                stock = stockLimit
                trade.completeTime = -1L
                trade.remainSecs = -1L
            } else {
                val restStock = stockLimit - stock
                if (currentTs < completeTime) {
                    trade.remainSecs = restStock * targetPoint + completeTime - currentTs
                    trade.completeTime = currentTs + trade.remainSecs
                } else {
                    trade.completeTime =
                        (stockLimit - (rawStock + geneStock)) * targetPoint + completeTime
                    trade.remainSecs = trade.completeTime - currentTs
                }
            }
            trade.max = stockLimit
            trade.strategy = strategy
            playerData.tradings.tradings.add(trade)
            stockSum += stock
            stockLimitSum += stockLimit
            completeTimeAll = completeTimeAll.coerceAtLeast(trade.completeTime)
            remainSecsAll = remainSecsAll.coerceAtLeast(trade.remainSecs)
        }
        playerData.tradings.current = stockSum
        playerData.tradings.max = stockLimitSum
        playerData.tradings.completeTime = completeTimeAll
        playerData.tradings.remainSecs = remainSecsAll
    }

    //manufacture
    run {
        val manufacturesNode = tree.at("/data/building/manufactures")
        val manufactureFormulaInfoMap = tree.at("/data/manufactureFormulaInfoMap")
        playerData.tradings.isNull = manufacturesNode.isNull
        if (manufacturesNode.isNull) return@run
        var stockSum = 0
        var stockLimitSum = 0
        var completeTimeAll = -1L
        var remainSecsAll = -1L
        for (node in manufacturesNode) {
            val manufacture = PlayerData.Manufacture()
            val formulaId = node["formulaId"].asText()
            val weight = manufactureFormulaInfoMap.get(formulaId).get("weight").asInt()
            val stockLimit = node["capacity"].asInt() / weight
            val completeTime = node["completeWorkTime"].asLong()
            val lastUpdateTime = node["lastUpdateTime"].asLong()
            var stock = node["complete"].asInt()
            //严格来说不是一个正经计算当前完成的方法，和森空岛数据可能有1~2左右的出入
            if (currentTs >= completeTime) {
                stock = stockLimit
                manufacture.completeTime = -1L
                manufacture.remainSecs = -1L
            } else {
                stock += ((currentTs - lastUpdateTime) /
                        ((completeTime - lastUpdateTime) / (stockLimit - stock))).toInt()
                manufacture.completeTime = completeTime
                manufacture.remainSecs = completeTime - currentTs
            }
            manufacture.formula = formulaId
            playerData.manufactures.manufactures.add(manufacture)
            stockLimitSum += stockLimit
            stockSum += stock
            completeTimeAll = completeTimeAll.coerceAtLeast(manufacture.completeTime)
            remainSecsAll = remainSecsAll.coerceAtLeast(manufacture.remainSecs)
        }
        playerData.manufactures.current = stockSum
        playerData.manufactures.max = stockLimitSum
        playerData.manufactures.completeTime = completeTimeAll
        playerData.manufactures.remainSecs = remainSecsAll
    }

    //labor
    run {
        val laborNode = tree.at("/data/building/labor")
        val laborValue = laborNode["value"].asInt()
        val laborMax = laborNode["maxValue"].asInt()
        var laborCurrent = if (laborNode["remainSecs"].asLong() == 0L) {
            laborValue
        } else {
            ((currentTs - laborNode["lastUpdateTime"].asLong()) * (laborMax - laborValue)
                    / laborNode["remainSecs"].asLong() + laborValue).toInt()
        }
        var laborRemain =
            laborNode["remainSecs"].asInt() - (currentTs - laborNode["lastUpdateTime"].asInt())
        if (laborCurrent > laborMax) {
            laborCurrent = laborMax
        }
        val recoverTime = laborNode["remainSecs"].asInt() + laborNode["lastUpdateTime"].asInt()
        playerData.labor.current = laborCurrent
        playerData.labor.max = laborMax
        if (laborRemain < 0) {
            laborRemain = 0
        }

        playerData.labor.remainSecs = laborRemain
        playerData.labor.recoverTime = recoverTime.toLong()
    }

    //dormitories
    run {
        val dormitoriesNode = tree.at("/data/building/dormitories")
        playerData.dormitories.isNull = dormitoriesNode.isNull
        if (dormitoriesNode.isNull) return@run
        var max = 0
        var value = 0
        for (node in dormitoriesNode) {
            val chars = node["chars"]
            var speed = node["level"].asDouble() * 0.1 + 1.5 + node["comfort"].asDouble() / 2500
            speed *= 100.0
            max += chars.size()
            for (j in 0 until chars.size()) {
                val chr = chars[j]
                val currentAp = chr.at("/ap").asInt()
                val lastApAddTime = chr.at("/lastApAddTime").asInt()
                if (currentAp == 8640000) value++
                else {
                    val ap = ((currentTs - lastApAddTime) * speed.toInt() + currentAp).toInt()
                    if (ap >= 8640000) value++
                }
            }
        }
        playerData.dormitories.max = max
        playerData.dormitories.current = value
        //TODO: recoverTime
    }

    //tired
    run {
        val buildingNode = tree.at("/data/building")
        val tiredNode = buildingNode.at("/tiredChars")
        val nodeChar = tree.at("/data/charInfoMap")
        var value = tiredNode.size()        //current value
        var remainSecs = Long.MAX_VALUE
        //遍历基建群
        val meetingNode = buildingNode["meeting"]
        val controlNode = buildingNode["control"]
        val hireNode = buildingNode["hire"]
        val tradingsNode = buildingNode["tradings"]
        val manufacturesNode = buildingNode["manufactures"]
        val powersNode = buildingNode["powers"]

        //干员心情消耗受到基建buff影响，但skd返回的数据同样缺少这部分，
        //计算基于满心情开始工作的假设，不满足假设的情况会导致结果不准确
        //尤其是放入设施时，干员心情越低，计算出的speed越高。

        val charLists = mutableListOf<JsonNode>()
        if (!meetingNode.isNull) {
            charLists.add(meetingNode["chars"])
        }
        if (!controlNode.isNull) {
            charLists.add(controlNode["chars"])
        }
        if (!hireNode.isNull) {
            charLists.add(hireNode["chars"])
        }
        if (!tradingsNode.isNull) {
            for (tradingNode in tradingsNode) {
                charLists.add(tradingNode["chars"])
            }
        }
        if (!manufacturesNode.isNull) {
            for (manufactureNode in manufacturesNode) {
                charLists.add(manufactureNode["chars"])
            }
        }
        if (!powersNode.isNull) {
            for (powerNode in powersNode) {
                charLists.add(powerNode["chars"])
            }
        }
        if (charLists.isNotEmpty()) {
            for (charList in charLists) {
                for (char in charList) {
                    val ap = char["ap"].asLong()
                    val lastApAddTime = char["lastApAddTime"].asLong()
                    val workTime = char["workTime"].asLong()
                    if (workTime == 0L) continue
                    val speed = (8640000L - ap).toFloat() / workTime.toFloat()
                    val restTime = ap.toFloat() / speed
                    if ((currentTs - lastApAddTime) > restTime) {
                        val name = nodeChar.get(char["charId"].asText()).get("name").asText()
                        println("tired: $name")
                        value++
                    } else {
                        remainSecs = remainSecs.coerceAtMost(restTime.toLong())
                    }
                }
            }
        }

        playerData.tired.current = value
        playerData.tired.remainSecs = remainSecs
    }

    //routine
    run {
        val routineNode = tree.at("/data/routine")
        val campaignNode = tree.at("/data/campaign")
        val towerNode = tree.at("/data/tower")
        playerData.routine.dailyCurrent = routineNode["daily"]["current"].asInt()
        playerData.routine.dailyTotal = routineNode["daily"]["total"].asInt()
        playerData.routine.weeklyCurrent = routineNode["weekly"]["current"].asInt()
        playerData.routine.weeklyTotal = routineNode["weekly"]["total"].asInt()
        playerData.routine.campaignCurrent = campaignNode["reward"]["current"].asInt()
        playerData.routine.campaignTotal = campaignNode["reward"]["total"].asInt()
        playerData.routine.towerHigherCurrent =
            towerNode["reward"]["higherItem"]["current"].asInt()
        playerData.routine.towerHigherTotal = towerNode["reward"]["higherItem"]["total"].asInt()
        playerData.routine.towerLowerCurrent =
            towerNode["reward"]["lowerItem"]["current"].asInt()
        playerData.routine.towerLowerTotal = towerNode["reward"]["lowerItem"]["total"].asInt()
    }
    return playerData
}

private fun getTotalPoint(computePoint: Long): Long {
    if (computePoint > 86400L) return 86400L
    if (computePoint > 57600L) return 86400L
    if (computePoint > 43200L) return 57600L
    if (computePoint > 28800L) return 43200L
    return 28800L
}

fun getOpeData(tree: JsonNode): ArrayList<Operator> {
    val i18n: I18n by getKoin().inject()
    var charList = ArrayList<Operator>()
    val charsNode = tree.at("/data/chars")
    val charInfoMap = tree.at("/data/charInfoMap")
    val equipInfoMap = tree.at("/data/equipmentInfoMap")
    for (char in charsNode) {
        val operator = Operator()
        val charId = char["charId"].asText()
        operator.charId = charId
        val skinId = char["skinId"].asText()
        val skinIdTemp = if ('@' in skinId) {
            skinId.replace('@', '_')
        } else if ("#1" in skinId) {
            skinId.replace("#1", "")
        } else {
            skinId.replace('#', '_')
        }
        operator.skinId = skinIdTemp
        operator.level = char["level"].asInt()
        operator.evolvePhase = char["evolvePhase"].asInt()
        operator.potentialRank = char["potentialRank"].asInt()
        operator.mainSkillLvl = char["mainSkillLvl"].asInt()
        operator.favorPercent = char["favorPercent"].asInt()
        operator.defaultSkillId = char["defaultSkillId"].asText()
        operator.gainTime = char["gainTime"].asLong()
        operator.defaultEquipId = char["defaultEquipId"].asText()

        for ((idx, skill) in char["skills"].withIndex()) {
            operator.skills.add(Operator.Skill(idx, skill["specializeLevel"].asInt()))
        }
        for (equip in char["equip"]) {
            val equipId = equip["id"].asText()
            val equipInfo = equipInfoMap[equipId]
            val level = equip["level"].asInt()
            val locked = equip["locked"].asBoolean()
            if (equipInfo.has("typeName2") && !locked) {
                operator.equips.add(
                    Operator.Equip(
                        equipId,
                        equipInfo["typeIcon"].asText(),
                        equipInfo["typeName2"].asText(),
                        level
                    )
                )
            }

        }
        val charInfo = charInfoMap.get(operator.charId)
        operator.name = charInfo["name"].asText()
        operator.nationId = charInfo["nationId"].asText()
        operator.groupId = charInfo["groupId"].asText()
        operator.displayNumber = charInfo["displayNumber"].asText()
        operator.rarity = charInfo["rarity"].asInt()
        operator.profession = charInfo["profession"].asText()
        operator.subProfessionId = charInfo["subProfessionId"].asText()
        operator.professionName = i18n.convert(operator.profession, I18n.ConvertType.Profession)
        operator.subProfessionName =
            i18n.convert(operator.subProfessionId, I18n.ConvertType.SubProfession)

        val stringBuilder = StringBuilder()
        for (equip in operator.equips) {
            stringBuilder.append(equip.typeName2).append("-").append(equip.level)
                .append(" ")
        }
        operator.equipString = stringBuilder.toString()
        charList.add(operator)
    }

    val customComparator = Comparator<Operator> { u1, u2 ->
        if (u1.rarity != u2.rarity) {
            u2.rarity - u1.rarity
        } else {
            if (u1.evolvePhase != u2.evolvePhase) {
                u2.evolvePhase - u1.evolvePhase
            } else {
                u2.level - u1.level
            }
        }
    }
    //排序：职业
    val customOrder = listOf(
        "PIONEER", "WARRIOR", "TANK", "SNIPER",
        "CASTER", "MEDIC", "SUPPORT", "SPECIAL"
    )
    val collator = Collator.getInstance(java.util.Locale.CHINA)
    val charsTempList = charList.sortedWith(compareBy(collator) { it.name })
    val sortedList = charsTempList.sortedBy { customOrder.indexOf(it.profession) }
    charList = sortedList.sortedWith(customComparator).toMutableList() as ArrayList<Operator>
    return charList
}
