package com.blueskybone.arkscreen.playerinfo

import com.blueskybone.arkscreen.network.model.Ap
import com.blueskybone.arkscreen.network.model.Building
import com.blueskybone.arkscreen.network.model.CharInfo
import com.blueskybone.arkscreen.network.model.Dormitories
import com.blueskybone.arkscreen.network.model.Hire
import com.blueskybone.arkscreen.network.model.Labor
import com.blueskybone.arkscreen.network.model.MChars
import com.blueskybone.arkscreen.network.model.ManufactureFormulaInfo
import com.blueskybone.arkscreen.network.model.Manufactures
import com.blueskybone.arkscreen.network.model.Meeting
import com.blueskybone.arkscreen.network.model.PlayerInfoResp
import com.blueskybone.arkscreen.network.model.Recruit
import com.blueskybone.arkscreen.network.model.Tradings
import com.blueskybone.arkscreen.network.model.Training


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

        //avatar
        dataTree.status.avatar.let { avatarInfo ->
            this.avatar.type = avatarInfo.type
            this.avatar.id = avatarInfo.id
            avatarInfo.url?.let { this.avatar.url = it }
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

