package com.blueskybone.arkscreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.DataUiState
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.getGameInfoConnectionTask
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.getGameInfoConnectionTaskTest
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.playerinfo.ApCache
import com.blueskybone.arkscreen.playerinfo.RealTimeData
import com.blueskybone.arkscreen.playerinfo.RealTimeUi
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.TimeUtils.getDayNum
import com.blueskybone.arkscreen.util.TimeUtils.getRemainTimeStr
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStr
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.getKoin
import java.util.zip.GZIPInputStream

/**
 *   Created by blueskybone
 *   Date: 2025/1/17
 */


class RealTimeModel : ViewModel() {
    private val prefManager: PrefManager by getKoin().inject()

    private val _uiState = MutableLiveData<DataUiState>()
    val uiState: LiveData<DataUiState> get() = _uiState
    var realTimeUi: RealTimeUi? = null


    init {
        viewModelScope.launch {
            _uiState.value = DataUiState.Loading("LOADING...")
            withContext(Dispatchers.IO) {
                loadRealTimeData()
            }
        }
    }

    private suspend fun loadRealTimeData() {
        val accountSk = prefManager.baseAccountSk.get()
        //not a good practice. for null/empty situation, use a simple flag,
        //add 'isnull' to AccountSk
        if (accountSk.uid == "") {
            _uiState.postValue(DataUiState.Error("未登录"))
            return
        }
        try {
            val realTimeData = getRealTimeData(accountSk)
            //store ap to cache
            val apCache = ApCache(
                realTimeData.currentTs,
                realTimeData.currentTs,
                realTimeData.apInfo.remainSecs,
                realTimeData.apInfo.recoverTime,
                realTimeData.apInfo.max,
                realTimeData.apInfo.current,
                false
            )
            val laborCache = ApCache(
                realTimeData.currentTs,
                realTimeData.currentTs,
                realTimeData.labor.remainSecs,
                realTimeData.labor.recoverTime,
                realTimeData.labor.max,
                realTimeData.labor.current,
                false
            )
            prefManager.apCache.set(apCache)
            prefManager.laborCache.set(laborCache)

            realTimeUi = processData(realTimeData, accountSk.official)
            _uiState.postValue(DataUiState.Success(""))
        } catch (e: Exception) {
            _uiState.postValue(DataUiState.Error(e.message ?: "LOADING FAILED"))
        }
    }

    private fun processData(data: RealTimeData, official: Boolean): RealTimeUi {
        val realTimeUi = RealTimeUi()
        realTimeUi.official = official
        realTimeUi.apNow = data.apInfo.current.toString()
        realTimeUi.apMax = "/" + data.apInfo.max
        realTimeUi.apResTime = data.apInfo.remainSecsStr
        realTimeUi.nickName = data.playerStatus.nickname
        realTimeUi.lastLogin = "上次登录 " +
                when (getDayNum(getCurrentTs()) - getDayNum(data.playerStatus.lastOnlineTs)) {
                    0L -> "今天"
                    1L -> "昨天"
                    else -> getTimeStr(data.playerStatus.lastOnlineTs * 1000, "yyyy-MM-dd")
                }

        //recruit
        realTimeUi.recruit.value = "${data.recruits.complete}/${data.recruits.max}"
        realTimeUi.recruit.time = if (data.recruits.remainSecs == -1L) {
            "已完成招募"
        } else {
            getRemainTimeStr(data.recruits.remainSecs)
        }

        //recruitRefresh
        if (data.hire.isNull) {
            realTimeUi.recruitRefresh.value = "暂无数据"
            realTimeUi.recruitRefresh.time = ""
        } else {
            realTimeUi.recruitRefresh.value = "${data.hire.count}/3"
            realTimeUi.recruitRefresh.time = if (data.hire.remainSecs == -1L) {
                "已完成刷新"
            } else {
                getRemainTimeStr(data.hire.remainSecs)
            }
        }

        //labor
        realTimeUi.labor.value = "${data.labor.current}/${data.labor.max}"
        realTimeUi.labor.time = if (data.labor.remainSecs == -1L) {
            ""
        } else {
            getRemainTimeStr(data.labor.remainSecs)
        }
        //meeting
        if (data.meeting.isNull) {
            realTimeUi.meeting.value = "暂无数据"
        } else {
            realTimeUi.meeting.value = "${data.meeting.current}/7"
            realTimeUi.meeting.time = if (data.meeting.remainSecs == -1L) {
                "收集完成"
            } else {
                getRemainTimeStr(data.meeting.remainSecs)
            }
        }

        //base
        realTimeUi.manufacture.value = "${data.manufactures.current}/${data.manufactures.max}"
        realTimeUi.trading.value = "${data.tradings.current}/${data.tradings.max}"
        realTimeUi.dormitories.value = "${data.dormitories.current}/${data.dormitories.max}"
        realTimeUi.tired.value = "${data.tired.current}"

        //train
        if (data.train.isNull) {
            realTimeUi.train.value = "暂无数据"
        } else {
            realTimeUi.train.value = if (data.train.traineeIsNull) {
                "空闲中"
            } else {
                data.train.trainee
            }
            realTimeUi.train.time = when (data.train.remainSecs) {
                -1L -> "空闲中"
                0L -> "专精完成"
                else -> getRemainTimeStr(data.train.remainSecs)
            }
        }
        realTimeUi.campaign.value = "${data.routine.campaignCurrent}/${data.routine.campaignTotal}"
        //show change
        if (data.train.changeTimeLogos != -1L) {
            realTimeUi.displayChange = true
            realTimeUi.logosChange.text =
                "逻各斯换班时间：${getTimeStr(data.train.changeTimeLogos * 1000)}"
            realTimeUi.logosChange.display = true
        }

        if (data.train.changeTimeIrene != -1L) {
            realTimeUi.displayChange = true
            realTimeUi.ireneChange.text =
                "艾丽妮换班时间：${getTimeStr(data.train.changeTimeIrene * 1000)}"
            realTimeUi.ireneChange.display = true
        }
        return realTimeUi
    }

    private suspend fun  getRealTimeData(account:AccountSk): RealTimeData {

//        try{
//            val playerInfoRespTest = getGameInfoConnectionTaskTest(account)
//            println("playerInfoRespTest success")
////            println(playerInfoRespTest.body()?.data?.building?.manufactures?.get(0)?.chars?.get(0)?.charId)
//            val train = playerInfoRespTest.body()?.data?.building?.training
//            if(train == null){
//                println("training is null SUCE")
//            }else{
//                println("training not null fail")
//
//            }
//        }catch (e: Exception){
//            e.printStackTrace()
//            println("playerInfoRespTest failed" )
//        }

        val response = getGameInfoConnectionTask(account)
        response.body()?.use{ body ->
            val gzip = GZIPInputStream(body.byteStream())
            val result = readRealTimeFromGzip(ObjectMapper().readTree(gzip))
            return result
        }?: throw Exception("response body is empty")
    }

    private fun readRealTimeFromGzip(tree: JsonNode): RealTimeData {
        val currentTs = tree.at("/data/currentTs").asLong()
        val playerData = RealTimeData()
        playerData.currentTs = currentTs

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
//                playerData.apInfo.current =
//                    (currentTs - lastApAddTime).toInt() / (60 * 6) + current
                playerData.apInfo.current = max - ((recoverTime - currentTs).toInt() / (60 * 6) + 1)
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
                        playerData.train.changeRemainSecsIrene = secs.toLong()
                        playerData.train.changeTimeIrene = currentTs + secs.toLong()
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
                val trade = RealTimeData.Trade()
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
//                val speed = 1.8F
                val targetPoint = when (strategy) {
                    "O_GOLD" -> 7000L  //3.5 * 3600 / speed
                    else -> 4000L      //2 * 3600 / speed
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
                val manufacture = RealTimeData.Manufacture()
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
            val recoverTime =
                laborNode["remainSecs"].asInt() + laborNode["lastUpdateTime"].asInt()
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
                var speed =
                    node["level"].asDouble() * 0.1 + 1.5 + node["comfort"].asDouble() / 2500
                speed *= 100.0
                max += chars.size()
                for (j in 0 until chars.size()) {
                    val chr = chars[j]
                    val currentAp = chr.at("/ap").asInt()
                    val lastApAddTime = chr.at("/lastApAddTime").asInt()
                    if (currentAp == 8640000) value++
                    else {
                        val ap =
                            ((currentTs - lastApAddTime) * speed.toInt() + currentAp).toInt()
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
                            val name =
                                nodeChar.get(char["charId"].asText()).get("name").asText()
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
            playerData.routine.towerHigherTotal =
                towerNode["reward"]["higherItem"]["total"].asInt()
            playerData.routine.towerLowerCurrent =
                towerNode["reward"]["lowerItem"]["current"].asInt()
            playerData.routine.towerLowerTotal =
                towerNode["reward"]["lowerItem"]["total"].asInt()
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
}