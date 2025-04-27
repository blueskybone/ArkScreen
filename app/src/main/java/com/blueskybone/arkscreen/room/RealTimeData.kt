package com.blueskybone.arkscreen.room

/**
 *   Created by blueskybone
 *   Date: 2024/3/11
 */

data class RealTimeData(
    var currentTs: Long = -1L,
    val playerStatus: PlayerStatus = PlayerStatus(),
    val apInfo: Ap = Ap(),
    val train: Train = Train(),
    val recruits: Recruits = Recruits(),
    val hire: Hire = Hire(),
    val labor: Labor = Labor(),
    val dormitories: Dormitories = Dormitories(),
    val tired: Tired = Tired(),
    val meeting: Meeting = Meeting(),
    val manufactures: Manufactures = Manufactures(),
    val tradings: Tradings = Tradings(),
    val routine: Routine = Routine()
) {

    data class PlayerStatus(
        var uid: String = "",
        var nickname: String = "",
        var registerTs: Long = 0L,
        var level: Int = 0,
        var lastOnlineTs: Long = 0L
    )

    data class Ap(
        var current: Int = -1,
        var max: Int = -1,
        var recoverTime: Long = 0L,
        var remainSecs: Long = 0L,
        var recoverTimeStr: String = "",
        var remainSecsStr: String = "",
    )

    data class Train(
        var isNull: Boolean = false,
        var trainee: String = "",               //real name from mapinfo
        var traineeIsNull: Boolean = false,     //null: 空闲中
        var profession: String = "",            //职业
        var trainer: String = "",               //real name from mapinfo
        var trainerIsNull: Boolean = false,     //null: 空闲中
        var targetSkill: Int = 0,
//        var targetLevel: Int = 0,             //算不出来，只有专精三可以判断(12h,24h)，专精一二由于艾丽妮的技能导致无法区分。
        var totalPoint: Long = 0L,
        var remainPoint: Long = 0L,
        var status: Int = 0,
        var remainSecs: Long = 0L,
        var completeTime: Long = 0L,
        var changeRemainSecsIrene: Long = -1L,       //计算换班时间
        var changeTimeIrene: Long = -1L,
        var changeRemainSecsLogos: Long = -1L,  //计算换班时间Logos
        var changeTimeLogos: Long = -1L,
        //var changeRemainSecsIrene: Long = -1L,     //计算换班时间
        //var changeTimeIrene Long = -1L,
    )

    data class Recruits(
        var isNull: Boolean = false,
        var max: Int = -1,
        var complete: Int = -1,
        var remainSecs: Long = 0L,
        var completeTime: Long = 0L
    )

    data class Hire(
        var isNull: Boolean = false,
        var count: Int = -1,
        var completeTime: Long = 0L,
        var remainSecs: Long = 0L
    )

    data class Labor(
        var isNull: Boolean = false,
        var current: Int = -1,
        var max: Int = -1,
        var remainSecs: Long = 0L,
        var recoverTime: Long = 0L
    )

    data class Dormitories(
        var isNull: Boolean = false,
        var current: Int = -1,
        var max: Int = -1,
        var remainSecs: Long = 0L,
        var recoverTime: Long = 0L
    )

    data class Tired(
        var current: Int = -1,
        var remainSecs: Long = -1,      //算不出来
    )

    data class Meeting(
        var isNull: Boolean = false,
        var current: Int = -1,
        var max: Int = 7,
        var remainSecs: Long = -1L,
        var completeTime: Long = -1L,
        var status: Int = -1,
    )

    data class Manufactures(
        var isNull: Boolean = false,
        var current: Int = -1,
        var max: Int = 7,
        var manufactures: MutableList<Manufacture> = mutableListOf(),
        var completeTime: Long = 0L,    //直接取值
        var remainSecs: Long = 0L
    )

    data class Manufacture(
        var compelete: Int = -1,
        var max: Int = -1,
        var formula: String = "",
        var completeTime: Long = 0L,    //直接取值
        var remainSecs: Long = 0L
    )

    data class Tradings(
        var isNull: Boolean = false,
        var current: Int = -1,
        var max: Int = 7,
        var tradings: MutableList<Trade> = mutableListOf(),
        var completeTime: Long = 0L,    //直接取值
        var remainSecs: Long = 0L
    )

    data class Trade(
        var max: Int = -1,
        var stock: Int = -1,
        var strategy: String = "",      //获取策略
        var completeTime: Long = 0L,    //直接取值
        var remainSecs: Long = 0L
    )

    data class Routine(
        var dailyCurrent: Int = -1,
        var dailyTotal: Int = -1,
        var weeklyCurrent: Int = -1,
        var weeklyTotal: Int = -1,
        var campaignCurrent: Int = -1,
        var campaignTotal: Int = -1,
        var towerHigherCurrent: Int = -1,
        var towerHigherTotal: Int = -1,
        var towerLowerCurrent: Int = -1,
        var towerLowerTotal: Int = -1,
    )

}