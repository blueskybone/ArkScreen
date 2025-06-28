package com.blueskybone.arkscreen.network.model

import kotlin.Char


data class PlayerInfoResp(

    var code: Int? = null,
    var message: String? = null,
    var timestamp: Long? = null,
    var data: PlayerInfoData? = null
)

data class PlayerInfoData(
    val currentTs: Long,
    val status: Status,
    val building: Building,
    val recruit: List<Recruit>?,
    val manufactureFormulaInfoMap: Map<String, ManufactureFormulaInfo>,
    var chars: List<CharsDetail>,
    val charInfoMap: Map<String, CharInfo>,
    val equipmentInfoMap: Map<String, EquipmentInfo>,
    val campaign: Campaign?
)

data class Status(

    var uid: String,
    var name: String,
    var level: Int,
    var avatar: Avatar = Avatar(),
    var registerTs: Long,
    var mainStageProgress: String,
    var ap: Ap? = null,
    var lastOnlineTs: Long,
    var charCnt: Int,
    var furnitureCnt: Int,
    var skinCnt: Int

)

data class Ap(

    var current: Int,
    var max: Int,
    var lastApAddTime: Long,
    var completeRecoveryTime: Long,

    )

data class Avatar(
    var type: String? = null,
    var id: String? = null,
    var url: String? = null
)


data class Building(

    var tiredChars: List<MChars> = arrayListOf(),
    var powers: List<Powers> = arrayListOf(),
    var manufactures: List<Manufactures>? = arrayListOf(),
    var tradings: List<Tradings>? = arrayListOf(),
    var dormitories: List<Dormitories>? = arrayListOf(),
    var meeting: Meeting? = null,
    var hire: Hire? = null,
    var training: Training? = null,
    var labor: Labor,
    var control: Control? = null,
)

data class MChars(

    var charId: String,
    var ap: Int,
    var lastApAddTime: Long,
    var index: Int,
    var workTime: Long
)

data class Powers(

    var slotId: String? = null,
    var level: Int? = null,
    var chars: ArrayList<MChars> = arrayListOf()
)

data class Manufactures(

    var chars: ArrayList<MChars> = arrayListOf(),
    var completeWorkTime: Long,
    var lastUpdateTime: Long,
    var formulaId: String,
    var capacity: Int,
    var weight: Int,
    var complete: Int,
    var remain: Int,
    var speed: Double

)

data class Tradings(

    var chars: ArrayList<MChars> = arrayListOf(),
    var completeWorkTime: Long,
    var lastUpdateTime: Long,
    var strategy: String,
    var stock: ArrayList<Stock> = arrayListOf(),
    var stockLimit: Int

)

data class Stock(

    var instId: Int? = null,
    var type: String? = null,
    var delivery: ArrayList<Delivery> = arrayListOf(),
    var gain: Gain? = Gain(),
    var isViolated: Boolean? = null

)

data class Gain(

    var id: String? = null,
    var count: Int? = null,
    var type: String? = null

)

data class Delivery(

    var id: String? = null,
    var count: Int? = null,
    var type: String? = null

)

data class Dormitories(

    var level: Int,
    var chars: ArrayList<MChars> = arrayListOf(),
    var comfort: Int

)

data class Meeting(

    var slotId: String? = null,
    var level: Int? = null,
    var chars: ArrayList<MChars> = arrayListOf(),
    var clue: Clue? = null,
    var lastUpdateTime: Long? = null,
    var completeWorkTime: Long? = null

)

data class Clue(

    var own: Int,
    var received: Int,
    var dailyReward: Boolean,
    var needReceive: Int,
    var board: ArrayList<String> = arrayListOf(),
    var sharing: Boolean,
    var shareCompleteTime: Long

)

data class Hire(

    var chars: ArrayList<MChars> = arrayListOf(),
    var state: Int,
    var refreshCount: Int,
    var completeWorkTime: Long

)

data class Training(

    var trainee: Trainee? = null,
    var trainer: Trainer? = null,
    var remainPoint: Double? = null,
    var speed: Double? = null,
    var lastUpdateTime: Long? = null,
    var remainSecs: Long? = null,
    var slotState: Int? = null

)

data class Trainee(

    var charId: String,
    var targetSkill: Int,
    var ap: Int,
    var lastApAddTime: Long

)

data class Trainer(

    var charId: String,
    var ap: Int,
    var lastApAddTime: Long

)

data class Labor(

    var maxValue: Int,
    var value: Int,
    var lastUpdateTime: Long,
    var remainSecs: Long

)

data class Control(
    var slotState: Int,
    var level: Int,
    var chars: ArrayList<MChars> = arrayListOf()

)

data class Recruit(

    var startTs: Long? = null,
    var finishTs: Long? = null,
    var state: Int? = null

)

data class ManufactureFormulaInfo(
    var id: String,
    var itemId: String,
    var count: Int,
    var weight: Int,
    var costs: ArrayList<Costs> = arrayListOf(),
    var costPoint: Int
)

data class Costs(

    var id: String,
    var count: Int,
    var type: String

)

data class CharInfo(
    var id: String,
    var name: String,
    var nationId: String,
    var groupId: String,
    var displayNumber: String,
    var rarity: Int,
    var profession: String,
    var subProfessionId: String,
    var subProfessionName: String,
    var appellation: String,
    var sortId: Int
)

data class Campaign(
    var reward: Reward
)

data class Reward(
    var current: Int,
    var total: Int
)


data class CharsDetail(
    var charId: String,
    var skinId: String,
    var level: Int,
    var evolvePhase: Int,
    var potentialRank: Int,
    var mainSkillLvl: Int,
    var skills: ArrayList<Skills> = arrayListOf(),
    var equip: ArrayList<Equip> = arrayListOf(),
    var favorPercent: Int,
    var defaultSkillId: String,
    var gainTime: Long,
    var defaultEquipId: String,
    var sortId: Int,
    var exp: Int,
    var gold: Int,
    var rarity: Int
)

data class Skills(

    var id: String,
    var specializeLevel: Int

)

data class Equip(

    var id: String,
    var level: Int,
    var locked: Boolean

)

data class EquipmentInfo(
    var id: String,
    var name: String,
    var typeIcon: String,
    var typeName2: String? = null,
    var shiningColor: String
)

//"id": "uniequip_001_absin",
//                "name": "苦艾证章",
//                "typeIcon": "original",
//                "shiningColor": "grey"
//data class PlayerInfoResp(
//    val currentTs: Long,
//    val status: Status,
//    val building: Building,
//    val recruit: List<Recruit>,
////    val routine: Routine,
//)
//
//data class Status(
//    var uid: String? = null,
//    var name: String? = null,
//    var level: Int? = null,
//    var avatar: Avatar? = Avatar(),
//    var registerTs: Long? = null,
//    var mainStageProgress: String? = null,
//    var resume: String? = null,
//    var ap: Ap? = Ap(),
//    var lastOnlineTs: Long? = null,
//    var charCnt: Int? = null,
//    var furnitureCnt: Int? = null,
//    var skinCnt: Int? = null
//)
//
//data class Ap(
//    var current: Int? = null,
//    var max: Int? = null,
//    var lastApAddTime: Long? = null,
//    var completeRecoveryTime: Long? = null
//
//)
//
//data class Avatar(
//    var type: String? = null,
//    var id: String? = null,
//    var url: String? = null
//
//)

//data class Building(
//    var tiredChars: List<TiredChars>,
//    var powers: List<Powers>,
//    var manufactures: List<Manufactures>?,
//    var tradings: List<Tradings>?,
//    var dormitories: List<Dormitories>?,
//    var meeting: Meeting? ,
//    var hire: String? = null,
//    var training: String? = null,
//    var labor: Labor?
//)
//
//data class TiredChars(
//    var charId: String? = null
//)
//
//
//data class Manufactures (
//   var chars            : ArrayList<MChars> = arrayListOf(),
//   var completeWorkTime : Int?             = null,
//   var lastUpdateTime   : Int?             = null,
//   var formulaId        : String?          = null,
//   var capacity         : Int?             = null,
//   var weight           : Int?             = null,
//   var complete         : Int?             = null,
//   var remain           : Int?             = null,
//   var speed            : Double?          = null
//)
//
//data class MChars (
//   var charId        : String? = null,
//   var ap            : Int?    = null,
//   var lastApAddTime : Int?    = null,
//   var index         : Int?    = null,
//   var workTime      : Int?    = null
//
//)
//
//data class Powers (
//
//    var slotId : String?           = null,
//    var level  : Int?              = null,
//    var chars  : ArrayList<MChars> = arrayListOf()
//
//)