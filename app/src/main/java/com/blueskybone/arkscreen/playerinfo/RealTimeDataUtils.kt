package com.blueskybone.arkscreen.playerinfo

import com.blueskybone.arkscreen.network.model.Ap
import com.blueskybone.arkscreen.network.model.Building
import com.blueskybone.arkscreen.network.model.Campaign
import com.blueskybone.arkscreen.network.model.CharInfo
import com.blueskybone.arkscreen.network.model.Dormitories
import com.blueskybone.arkscreen.network.model.Hire
import com.blueskybone.arkscreen.network.model.Labor
import com.blueskybone.arkscreen.network.model.MChars
import com.blueskybone.arkscreen.network.model.ManufactureFormulaInfo
import com.blueskybone.arkscreen.network.model.Manufactures
import com.blueskybone.arkscreen.network.model.Meeting
import com.blueskybone.arkscreen.network.model.PlayerInfoData
import com.blueskybone.arkscreen.network.model.PlayerInfoResp
import com.blueskybone.arkscreen.network.model.Recruit
import com.blueskybone.arkscreen.network.model.Tradings
import com.blueskybone.arkscreen.network.model.Trainee
import com.blueskybone.arkscreen.network.model.Training
import com.blueskybone.arkscreen.util.TimeUtils.getRemainTimeStr
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStr
import com.fasterxml.jackson.databind.JsonNode


//fun geneRealTimeData(playerInfoResp: PlayerInfoResp): RealTimeData {
//
//    val dataTree = playerInfoResp.data!!
//
//    val currentTs = dataTree.currentTs
//    val playerData = RealTimeData()
//    playerData.currentTs = currentTs
//
//    val status = dataTree.status
//    playerData.playerStatus.uid = status.uid
//    playerData.playerStatus.nickname = status.name
//    playerData.playerStatus.level = status.level
//    playerData.playerStatus.registerTs = status.registerTs
//    playerData.playerStatus.lastOnlineTs = status.lastOnlineTs
//
//    //ap
//    run {
//        val ap = status.ap!!
//        val current: Int = ap.current
//        val max: Int = ap.max
//        val recoverTime: Long = ap.completeRecoveryTime
//        playerData.apInfo.max = max
//        if (current >= max) {
//            playerData.apInfo.current = current
//            playerData.apInfo.remainSecs = -1L
//            playerData.apInfo.recoverTime = -1L
//        } else if (recoverTime < currentTs) {
//            playerData.apInfo.current = max
//            playerData.apInfo.remainSecs = -1L
//            playerData.apInfo.recoverTime = -1L
//        } else {
////                playerData.apInfo.current =
////                    (currentTs - lastApAddTime).toInt() / (60 * 6) + current
//            playerData.apInfo.current = max - ((recoverTime - currentTs).toInt() / (60 * 6) + 1)
//            playerData.apInfo.remainSecs = recoverTime - currentTs
//            playerData.apInfo.recoverTime = recoverTime
//        }
////    if (playerData.apInfo.current >= max) {
////        playerData.apInfo.remainSecsStr = "已恢复"
////        playerData.apInfo.recoverTimeStr = "已恢复"
////    } else {
////        playerData.apInfo.recoverTimeStr = getTimeStr(recoverTime * 1000)
////        playerData.apInfo.remainSecsStr =
////            getRemainTimeStr(playerData.apInfo.remainSecs)
////    }
//    }
////
//    //training
//    run {
//        val train = dataTree.building.training
//        val nodeChar = dataTree.charInfoMap
//
//        playerData.train.isNull = train == null
//        if (train == null) return@run
//
//        playerData.train.traineeIsNull = dataTree.building.training!!.trainee == null
//        playerData.train.trainerIsNull = dataTree.building.training!!.trainer == null
//
//        val lastUpdateTime = train.lastUpdateTime!!
//
//        if (!playerData.train.traineeIsNull) {
//            val trainee = dataTree.building.training!!.trainee!!
//            val traineeCode: String = trainee.charId
//            playerData.train.trainee = nodeChar[traineeCode]!!.name
//            playerData.train.profession = nodeChar[traineeCode]!!.profession
//            playerData.train.targetSkill = trainee.targetSkill + 1
//        }
//        if (!playerData.train.trainerIsNull) {
//            val trainer = dataTree.building.training!!.trainer!!
//
//            val trainerCode: String = trainer.charId
//            playerData.train.trainer = nodeChar[trainerCode]!!.name
//        }
//        val remainSecs = train.remainSecs!!
//        playerData.train.remainSecs = remainSecs
//        playerData.train.completeTime = playerData.train.remainSecs + currentTs
//
//        when (remainSecs) {
//            //专精完成
//            0L -> {
//                playerData.train.totalPoint = 1L
//                playerData.train.remainPoint = 0L
//            }
//
//            //空闲中
//            -1L -> {
//                playerData.train.totalPoint = 1L
//                playerData.train.remainPoint = 1L
//            }
//
//            else -> {
//                playerData.train.remainPoint =
//                    (train.remainSecs!!.toDouble() * train.speed!!.toDouble()).toLong()
//
//                val totalPoint =
//                    ((currentTs - lastUpdateTime).toDouble() * train.speed!!.toDouble()).toLong() + playerData.train.remainPoint
//                playerData.train.totalPoint = getTotalPoint(totalPoint)
//                //playerData.train.totalPoint = totalPoint
//                var targetPoint = 18900L
//                var targetPoint2 = 18900L
//                if (playerData.train.profession == "SNIPER" || playerData.train.profession == "WARRIOR") targetPoint =
//                    24300L
//                if (playerData.train.profession == "CASTER" || playerData.train.profession == "SUPPORT") targetPoint2 =
//                    24300L
//                val remainPoint = playerData.train.remainPoint
//                if (remainPoint > targetPoint) {
//                    val secs = (remainPoint - targetPoint) / train.speed!!.toDouble()
//                    playerData.train.changeRemainSecsIrene = secs.toLong()
//                    playerData.train.changeTimeIrene = currentTs + secs.toLong()
//                }
//                if (remainPoint > targetPoint2) {
//                    val secs = (remainPoint - targetPoint2) / train.speed!!.toDouble()
//                    playerData.train.changeRemainSecsLogos = secs.toLong()
//                    playerData.train.changeTimeLogos = currentTs + secs.toLong()
//                }
//            }
//        }
//    }
//
//    //recruit
//    run {
//        val recruitNode = dataTree.recruit
//        playerData.recruits.isNull = recruitNode == null
//        if (recruitNode == null) return@run
//        var unable = 0
//        var complete = 0
//        var finishTs = -1L
//        for (i in 0..3) {
//            val node = recruitNode[i]
//            when (node.state) {
//                0 -> {
//                    unable++
//                }
//
//                3 -> {
//                    complete++
//                }
//
//                2 -> {
//                    val finish = node.finishTs!!
//                    if (finish < currentTs) complete++
//                    finishTs = finish.coerceAtLeast(finishTs)
//                }
//            }
//            if (finishTs == -1L || finishTs < currentTs) {
//                playerData.recruits.remainSecs = -1
//            } else {
//                playerData.recruits.remainSecs = (finishTs - currentTs)
//                playerData.recruits.completeTime = finishTs
//            }
//            playerData.recruits.max = 4 - unable
//            playerData.recruits.complete = complete
//        }
//    }
////
//    //hire
//    run {
//        val hireNode = dataTree.building.hire
//        playerData.hire.isNull = hireNode == null
//        if (hireNode == null) return@run
//        val count = hireNode.refreshCount
//        val remainSecs = hireNode.completeWorkTime - currentTs
//        val completeTime = hireNode.completeWorkTime
//        if (remainSecs < 0) {
//            playerData.hire.completeTime = -1L
//            playerData.hire.remainSecs = -1L
//            playerData.hire.count = (count + 1).coerceAtMost(3)
//        } else {
//            playerData.hire.completeTime = completeTime
//            playerData.hire.remainSecs = remainSecs
//            playerData.hire.count = count
//        }
//    }
////
//    //meeting
//    run {
//        val meetingNode = dataTree.building.meeting
//        playerData.meeting.isNull = meetingNode == null
//        if (meetingNode == null) return@run
//        val sharing = meetingNode.clue!!.sharing
//        val shareCompleteTime = meetingNode.clue!!.shareCompleteTime
//        if (!sharing) {
//            playerData.meeting.status = 0
//        } else {
//            if (shareCompleteTime > currentTs) {
//                playerData.meeting.status = 1
//                playerData.meeting.completeTime = shareCompleteTime
//                playerData.meeting.remainSecs = shareCompleteTime - currentTs
//            } else {
//                playerData.meeting.status = 2
//            }
//        }
//        playerData.meeting.current = meetingNode.clue!!.board.size
//    }
////
//    //tradings
//    run {
//        val tradingsNode = dataTree.building.tradings
//        playerData.tradings.isNull = tradingsNode == null
//        if (tradingsNode == null) return@run
//        var stockSum = 0
//        var stockLimitSum = 0
//        var completeTimeAll = -1L
//        var remainSecsAll = -1L
//
//        for (node in tradingsNode) {
//            val trade = RealTimeData.Trade()
//            val completeTime = node.completeWorkTime
//            val lastUpdateTime = node.lastUpdateTime
//            val stockLimit = node.stockLimit
//            val rawStock = node.stock.size
//            val strategy = node.strategy   //O_GOLD    O_DIAMOND
//            var stock = rawStock
//            /*
//            * 由于森空岛接口返回的信息缺少基建技能的效率加成数据，
//            * 无法准确计算贸易站订单数，只能采用估计speed的方法。
//            * targetPoint是效率为190%的假设下单个订单所需时间。
//            * 所以最后的估计订单数绝大部分情况下比真实值要高一些。
//            * 获取订单的正常时长：赤金3.5h,源石碎片2h。
//            * */
////                val speed = 1.8F
//            val targetPoint = when (strategy) {
//                "O_GOLD" -> 7000L  //3.5 * 3600 / speed
//                else -> 4000L      //2 * 3600 / speed
//            }
//
//            val geneStock = (completeTime - lastUpdateTime) / targetPoint
//            stock += geneStock.toInt()
//            if (geneStock.toInt() > 0 && currentTs < completeTime) stock--
//            else {
//                val newStock = (currentTs - completeTime) / targetPoint
//                stock += newStock.toInt() + 1
//            }
//            if (stock > stockLimit) {
//                stock = stockLimit
//                trade.completeTime = -1L
//                trade.remainSecs = -1L
//            } else {
//                val restStock = stockLimit - stock
//                if (currentTs < completeTime) {
//                    trade.remainSecs = restStock * targetPoint + completeTime - currentTs
//                    trade.completeTime = currentTs + trade.remainSecs
//                } else {
//                    trade.completeTime =
//                        (stockLimit - (rawStock + geneStock)) * targetPoint + completeTime
//                    trade.remainSecs = trade.completeTime - currentTs
//                }
//            }
//            trade.max = stockLimit
//            trade.strategy = strategy
//            playerData.tradings.tradings.add(trade)
//            stockSum += stock
//            stockLimitSum += stockLimit
//            completeTimeAll = completeTimeAll.coerceAtLeast(trade.completeTime)
//            remainSecsAll = remainSecsAll.coerceAtLeast(trade.remainSecs)
//        }
//        playerData.tradings.current = stockSum
//        playerData.tradings.max = stockLimitSum
//        playerData.tradings.completeTime = completeTimeAll
//        playerData.tradings.remainSecs = remainSecsAll
//    }
////
//    //manufacture
//    run {
//        val manufacturesNode = dataTree.building.manufactures
//        val manufactureFormulaInfoMap = dataTree.manufactureFormulaInfoMap
//        playerData.manufactures.isNull = manufacturesNode == null
//        if (manufacturesNode == null) return@run
//        var stockSum = 0
//        var stockLimitSum = 0
//        var completeTimeAll = -1L
//        var remainSecsAll = -1L
//        for (node in manufacturesNode) {
//            val manufacture = RealTimeData.Manufacture()
//            val formulaId = node.formulaId
//            val weight = manufactureFormulaInfoMap[formulaId]!!.weight
//            val stockLimit = node.capacity / weight
//            val completeTime = node.completeWorkTime
//            val lastUpdateTime = node.lastUpdateTime
//            var stock = node.complete
//            //严格来说不是一个正经计算当前完成的方法，和森空岛数据可能有1~2左右的出入
//            if (currentTs >= completeTime) {
//                stock = stockLimit
//                manufacture.completeTime = -1L
//                manufacture.remainSecs = -1L
//            } else {
//                stock += ((currentTs - lastUpdateTime) /
//                        ((completeTime - lastUpdateTime) / (stockLimit - stock))).toInt()
//                manufacture.completeTime = completeTime
//                manufacture.remainSecs = completeTime - currentTs
//            }
//            manufacture.formula = formulaId
//            playerData.manufactures.manufactures.add(manufacture)
//            stockLimitSum += stockLimit
//            stockSum += stock
//            completeTimeAll = completeTimeAll.coerceAtLeast(manufacture.completeTime)
//            remainSecsAll = remainSecsAll.coerceAtLeast(manufacture.remainSecs)
//        }
//        playerData.manufactures.current = stockSum
//        playerData.manufactures.max = stockLimitSum
//        playerData.manufactures.completeTime = completeTimeAll
//        playerData.manufactures.remainSecs = remainSecsAll
//    }
////
//    //labor
//    run {
//        val labor = dataTree.building.labor
//        val laborValue = labor.value
//        val laborMax = labor.maxValue
//        var laborCurrent = if (labor.remainSecs == 0L) {
//            laborValue
//        } else {
//            ((currentTs - labor.lastUpdateTime) * (laborMax - laborValue)
//                    / labor.remainSecs + laborValue).toInt()
//        }
//        var laborRemain = labor.remainSecs - (currentTs - labor.lastUpdateTime)
//        if (laborCurrent > laborMax) {
//            laborCurrent = laborMax
//        }
//        val recoverTime = labor.remainSecs + labor.lastUpdateTime
//        playerData.labor.current = laborCurrent
//        playerData.labor.max = laborMax
//        if (laborRemain < 0) {
//            laborRemain = 0
//        }
//
//        playerData.labor.remainSecs = laborRemain
//        playerData.labor.recoverTime = recoverTime
//    }
////
//    //dormitories
//    run {
//        val dormitoriesNode = dataTree.building.dormitories
//        playerData.dormitories.isNull = dormitoriesNode == null
//        if (dormitoriesNode == null) return@run
//        var max = 0
//        var value = 0
//        for (node in dormitoriesNode) {
//            val chars = node.chars
//            var speed =
//                node.level.toDouble() * 0.1 + 1.5 + node.comfort.toDouble() / 2500
//            speed *= 100.0
//            max += chars.size
//            for (j in 0 until chars.size) {
//                val chr = chars[j]
//                val currentAp = chr.ap
//                val lastApAddTime = chr.lastApAddTime.toInt()
//                if (currentAp == 8640000) value++
//                else {
//                    val ap =
//                        ((currentTs - lastApAddTime) * speed.toInt() + currentAp).toInt()
//                    if (ap >= 8640000) value++
//                }
//            }
//        }
//        playerData.dormitories.max = max
//        playerData.dormitories.current = value
//        //TODO: recoverTime
//    }
////
//    //tired
//    run {
//        val buildingNode = dataTree.building
//        val tiredNode = buildingNode.tiredChars
//        var value = tiredNode.size        //current value
//        var remainSecs = Long.MAX_VALUE
//        //遍历基建群
//        val meetingNode = buildingNode.meeting
//        val controlNode = buildingNode.control
//        val hireNode = buildingNode.hire
//        val tradingsNode = buildingNode.tradings
//        val manufacturesNode = buildingNode.manufactures
//        val powersNode = buildingNode.powers
//
//        val charList = arrayListOf<MChars>()
//        if (meetingNode != null) {
//            charList.addAll(meetingNode.chars)
//        }
//        if (controlNode != null) {
//            charList.addAll(controlNode.chars)
//        }
//        if (hireNode != null) {
//            charList.addAll(hireNode.chars)
//        }
//        if (tradingsNode != null) {
//            for (tradingNode in tradingsNode) {
//                charList.addAll(tradingNode.chars)
//            }
//        }
//        if (manufacturesNode != null) {
//            for (manufactureNode in manufacturesNode) {
//                charList.addAll(manufactureNode.chars)
//            }
//        }
//        for (powerNode in powersNode) {
//            charList.addAll(powerNode.chars)
//        }
//
//        //TODO:看一下现在能不能算了
//        //干员心情消耗受到基建buff影响，但skd返回的数据同样缺少这部分，
//        //计算基于满心情开始工作的假设，不满足假设的情况会导致结果不准确
//        //尤其是放入设施时，干员心情越低，计算出的speed越高。
//        if (charList.isNotEmpty()) {
//            for (char in charList) {
//                val ap = char.ap
//                val lastApAddTime = char.lastApAddTime
//                val workTime = char.workTime
//                if (workTime == 0L) continue
//                val speed = (8640000L - ap).toFloat() / workTime.toFloat()
//                val restTime = ap.toFloat() / speed
//                if ((currentTs - lastApAddTime) > restTime) {
//                    value++
//                } else {
//                    remainSecs = remainSecs.coerceAtMost(restTime.toLong())
//                }
//
//            }
//        }
//        playerData.tired.current = value
//        playerData.tired.remainSecs = remainSecs
//    }
//    return playerData
//}
//
//private fun getTotalPoint(computePoint: Long): Long {
//    if (computePoint > 86400L) return 86400L
//    if (computePoint > 57600L) return 86400L
//    if (computePoint > 43200L) return 57600L
//    if (computePoint > 28800L) return 43200L
//    return 28800L
//}

fun geneRealTimeData(playerInfoResp: PlayerInfoResp): RealTimeData {
    val dataTree = playerInfoResp.data ?: return RealTimeData().apply {
        currentTs = System.currentTimeMillis()
    }
    val currentTs = dataTree.currentTs

    return RealTimeData().apply {
        this.currentTs = currentTs

        // Player Status
        dataTree.status.let { status ->
            playerStatus.apply {
                uid = status.uid
                nickname = status.name
                level = status.level
                registerTs = status.registerTs
                lastOnlineTs = status.lastOnlineTs
            }
        }

        // AP Info
        dataTree.status.ap?.let { ap ->
            apInfo = calculateApInfo(ap, currentTs)
        }

        // Training
        dataTree.building.training?.let { train ->
            this.train = calculateTrainInfo(train, dataTree.charInfoMap, currentTs)
        }

        // Recruit
        dataTree.recruit?.let { recruits ->
            this.recruits = calculateRecruitInfo(recruits, currentTs)
        }

        // Hire
        dataTree.building.hire?.let { hire ->
            this.hire = calculateHireInfo(hire, currentTs)
        }

        // Meeting
        dataTree.building.meeting?.let { meeting ->
            this.meeting = calculateMeetingInfo(meeting, currentTs)
        }

        // Tradings
        dataTree.building.tradings?.let { tradings ->
            this.tradings = calculateTradingsInfo(tradings, currentTs)
        }

        // Manufactures
        dataTree.building.manufactures?.let { manufactures ->
            dataTree.manufactureFormulaInfoMap.let { formulaMap ->
                this.manufactures = calculateManufacturesInfo(manufactures, formulaMap, currentTs)
            }
        }

        // Labor
        dataTree.building.labor.let { labor ->
            this.labor = calculateLaborInfo(labor, currentTs)
        }

        // Dormitories
        dataTree.building.dormitories?.let { dormitories ->
            this.dormitories = calculateDormitoriesInfo(dormitories, currentTs)
        }

        // Tired
        dataTree.building.let { building ->
            this.tired = calculateTiredInfo(building, currentTs)
        }

        //Campaign
        dataTree.campaign?.let { campaign ->
            this.routine.campaignTotal = campaign.reward.total
            this.routine.campaignCurrent = campaign.reward.current
        }
    }
}

// ========== Helper Functions ==========

private fun calculateApInfo(ap: Ap, currentTs: Long): RealTimeData.Ap {
    return RealTimeData.Ap().apply {
        max = ap.max
        when {
            ap.current >= max -> {
                current = ap.current
                remainSecs = -1L
                recoverTime = -1L
            }

            ap.completeRecoveryTime < currentTs -> {
                current = max
                remainSecs = -1L
                recoverTime = -1L
            }

            else -> {
                current = max - ((ap.completeRecoveryTime - currentTs).toInt() / (60 * 6) + 1)
                remainSecs = ap.completeRecoveryTime - currentTs
                recoverTime = ap.completeRecoveryTime
            }
        }
    }
}

private fun calculateTrainInfo(
    train: Training,
    charInfoMap: Map<String, CharInfo>?,
    currentTs: Long
): RealTimeData.Train {
    return RealTimeData.Train().apply {
        isNull = false
        traineeIsNull = train.trainee == null
        trainerIsNull = train.trainer == null

        train.trainee?.let { trainee ->
            charInfoMap?.get(trainee.charId)?.let { charInfo ->
                this.trainee = charInfo.name
                profession = charInfo.profession
                targetSkill = trainee.targetSkill + 1
            }
        }

        train.trainer?.let { trainer ->
            charInfoMap?.get(trainer.charId)?.let { charInfo ->
                this.trainer = charInfo.name
            }
        }

        train.remainSecs?.let { remainSecs ->
            this.remainSecs = remainSecs
            completeTime = remainSecs + currentTs

            when (remainSecs) {
                0L -> { // 专精完成
                    totalPoint = 1L
                    this.remainPoint = 0L
                }

                -1L -> { // 空闲中
                    totalPoint = 1L
                    this.remainPoint = 1L
                }

                else -> {
                    train.speed?.let { speed ->
                        this.remainPoint = (remainSecs.toDouble() * speed).toLong()
                        val totalPointCalc =
                            ((currentTs - train.lastUpdateTime!!).toDouble() * speed).toLong() + this.remainPoint
                        totalPoint = getTotalPoint(totalPointCalc)

                        val targetPoint = when (profession) {
                            "SNIPER", "WARRIOR" -> 24300L
                            "CASTER", "SUPPORT" -> 24300L
                            else -> 18900L
                        }

                        if (this.remainPoint > targetPoint) {
                            val secs = (this.remainPoint - targetPoint) / speed
                            changeRemainSecsIrene = secs.toLong()
                            changeTimeIrene = currentTs + secs.toLong()
                        }
                    }
                }
            }
        }
    }
}

private fun calculateRecruitInfo(
    recruitNode: List<Recruit>,
    currentTs: Long
): RealTimeData.Recruits {
    return RealTimeData.Recruits().apply {
        isNull = false
        var unable = 0
        var complete = 0
        var maxFinishTs = -1L

        recruitNode.forEach { node ->
            when (node.state) {
                0 -> unable++
                3 -> complete++
                2 -> {
                    node.finishTs?.let { finishTs ->
                        if (finishTs < currentTs) complete++
                        maxFinishTs = maxFinishTs.coerceAtLeast(finishTs)
                    }
                }
            }
        }

        max = 4 - unable
        this.complete = complete

        if (maxFinishTs == -1L || maxFinishTs < currentTs) {
            remainSecs = -1
        } else {
            remainSecs = maxFinishTs - currentTs
            completeTime = maxFinishTs
        }
    }
}

private fun calculateHireInfo(hireNode: Hire, currentTs: Long): RealTimeData.Hire {
    return RealTimeData.Hire().apply {
        isNull = false
        val remainSecs = hireNode.completeWorkTime - currentTs

        if (remainSecs < 0) {
            completeTime = -1L
            this.remainSecs = -1L
            count = (hireNode.refreshCount + 1).coerceAtMost(3)
        } else {
            completeTime = hireNode.completeWorkTime
            this.remainSecs = remainSecs
            count = hireNode.refreshCount
        }
    }
}

private fun calculateMeetingInfo(meetingNode: Meeting, currentTs: Long): RealTimeData.Meeting {
    return RealTimeData.Meeting().apply {
        isNull = false
        current = meetingNode.clue?.board?.size ?: 0

        meetingNode.clue?.let { clue ->
            if (!clue.sharing) {
                status = 0
            } else {
                clue.shareCompleteTime.let { completeTime ->
                    if (completeTime > currentTs) {
                        status = 1
                        this.completeTime = completeTime
                        remainSecs = completeTime - currentTs
                    } else {
                        status = 2
                    }
                }
            }
        }
    }
}

private fun calculateTradingsInfo(
    tradingsNode: List<Tradings>,
    currentTs: Long
): RealTimeData.Tradings {
    return RealTimeData.Tradings().apply {
        isNull = false
        var stockSum = 0
        var stockLimitSum = 0
        var completeTimeAll = -1L
        var remainSecsAll = -1L

        tradingsNode.forEach { node ->
            RealTimeData.Trade().apply {
                strategy = node.strategy
                max = node.stockLimit

                val targetPoint = when (strategy) {
                    "O_GOLD" -> 7000L
                    else -> 4000L
                }

                val geneStock = (node.completeWorkTime - node.lastUpdateTime) / targetPoint
                var stock = node.stock.size + geneStock.toInt()

                if (geneStock.toInt() > 0 && currentTs < node.completeWorkTime) {
                    stock--
                } else {
                    val newStock = (currentTs - node.completeWorkTime) / targetPoint
                    stock += newStock.toInt() + 1
                }

                if (stock > node.stockLimit) {
                    stock = node.stockLimit
                    completeTime = -1L
                    remainSecs = -1L
                } else {
                    val restStock = node.stockLimit - stock
                    if (currentTs < node.completeWorkTime) {
                        remainSecs = restStock * targetPoint + node.completeWorkTime - currentTs
                        completeTime = currentTs + remainSecs
                    } else {
                        completeTime =
                            (node.stockLimit - (node.stock.size + geneStock)) * targetPoint + node.completeWorkTime
                        remainSecs = completeTime - currentTs
                    }
                }

                tradings.add(this)
                stockSum += stock
                stockLimitSum += node.stockLimit
                completeTimeAll = completeTimeAll.coerceAtLeast(completeTime)
                remainSecsAll = remainSecsAll.coerceAtLeast(remainSecs)
            }
        }

        current = stockSum
        max = stockLimitSum
        completeTime = completeTimeAll
        remainSecs = remainSecsAll
    }
}

private fun calculateManufacturesInfo(
    manufacturesNode: List<Manufactures>,
    formulaMap: Map<String, ManufactureFormulaInfo>,
    currentTs: Long
): RealTimeData.Manufactures {
    return RealTimeData.Manufactures().apply {
        isNull = false
        var stockSum = 0
        var stockLimitSum = 0
        var completeTimeAll = -1L
        var remainSecsAll = -1L

        manufacturesNode.forEach { node ->
            RealTimeData.Manufacture().apply {
                formula = node.formulaId
                val weight = formulaMap[node.formulaId]?.weight ?: 1
                val stockLimit = node.capacity / weight
                max = stockLimit

                var stock = node.complete
                if (currentTs >= node.completeWorkTime) {
                    stock = stockLimit
                    completeTime = -1L
                    remainSecs = -1L
                } else {
                    stock += ((currentTs - node.lastUpdateTime) /
                            ((node.completeWorkTime - node.lastUpdateTime) / (stockLimit - stock))).toInt()
                    completeTime = node.completeWorkTime
                    remainSecs = node.completeWorkTime - currentTs
                }

                manufactures.add(this)
                stockLimitSum += stockLimit
                stockSum += stock
                completeTimeAll = completeTimeAll.coerceAtLeast(completeTime)
                remainSecsAll = remainSecsAll.coerceAtLeast(remainSecs)
            }
        }

        current = stockSum
        max = stockLimitSum
        completeTime = completeTimeAll
        remainSecs = remainSecsAll
    }
}

private fun calculateLaborInfo(labor: Labor, currentTs: Long): RealTimeData.Labor {
    return RealTimeData.Labor().apply {
        max = labor.maxValue
        val laborRemain = labor.remainSecs - (currentTs - labor.lastUpdateTime)

        current = if (labor.remainSecs == 0L) {
            labor.value
        } else {
            ((currentTs - labor.lastUpdateTime) * (max - labor.value) /
                    labor.remainSecs + labor.value).toInt().coerceAtMost(max)
        }

        this.remainSecs = if (laborRemain < 0) 0 else laborRemain
        recoverTime = labor.remainSecs + labor.lastUpdateTime
    }
}

private fun calculateDormitoriesInfo(
    dormitoriesNode: List<Dormitories>,
    currentTs: Long
): RealTimeData.Dormitories {
    return RealTimeData.Dormitories().apply {
        isNull = false
        var max = 0
        var value = 0

        dormitoriesNode.forEach { node ->
            val chars = node.chars
            val speed = node.level * 0.1 + 1.5 + node.comfort / 2500.0
            max += chars.size

            chars.forEach { chr ->
                if (chr.ap == 8640000) {
                    value++
                } else {
                    val ap = ((currentTs - chr.lastApAddTime) * speed * 100 + chr.ap).toInt()
                    if (ap >= 8640000) value++
                }
            }
        }

        this.max = max
        current = value
    }
}

private fun calculateTiredInfo(building: Building, currentTs: Long): RealTimeData.Tired {
    return RealTimeData.Tired().apply {
        current = building.tiredChars.size
        var remainSecs = Long.MAX_VALUE

        val charList = mutableListOf<MChars>().apply {
            building.meeting?.chars?.let { addAll(it) }
            building.control?.chars?.let { addAll(it) }
            building.hire?.chars?.let { addAll(it) }
            building.tradings?.forEach { trading -> addAll(trading.chars) }
            building.manufactures?.forEach { manufacture -> addAll(manufacture.chars) }
            building.powers.forEach { power -> addAll(power.chars) }
        }

        charList.forEach { char ->
            if (char.workTime != 0L) {
                val speed = (8640000L - char.ap).toFloat() / char.workTime.toFloat()
                val restTime = char.ap.toFloat() / speed

                if ((currentTs - char.lastApAddTime) > restTime) {
                    current++
                } else {
                    remainSecs = minOf(remainSecs, restTime.toLong())
                }
            }
        }

        this.remainSecs = if (remainSecs == Long.MAX_VALUE) -1L else remainSecs
    }
}

private fun getTotalPoint(computePoint: Long): Long {
    return when {
        computePoint > 86400L -> 86400L
        computePoint > 57600L -> 86400L
        computePoint > 43200L -> 57600L
        computePoint > 28800L -> 43200L
        else -> 28800L
    }
}

