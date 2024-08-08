package com.blueskybone.arkscreen.base.data

/**
 *   Created by blueskybone
 *   Date: 2024/8/6
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlayerInfo(
    @JsonProperty("code")
    val code: Int,
    @JsonProperty("data")
    val data: Data,
    @JsonProperty("message")
    val message: String,
    @JsonProperty("timestamp")
    val timestamp: Long
) {
    data class Data(
//        @JsonProperty("activity")
//        val activity: List<Activity>,
//        @JsonProperty("activityBannerList")
//        val activityBannerList: ActivityBannerList,
//        @JsonProperty("activityInfoMap")
//        val activityInfoMap: ActivityInfoMap,
        @JsonProperty("assistChars")
        val assistChars: List<AssistChar>,
//        @JsonProperty("bossRush")
//        val bossRush: List<BossRush>,
        @JsonProperty("building")
        val building: Building,
//        @JsonProperty("campaign")
//        val campaign: Campaign,
//        @JsonProperty("campaignInfoMap")
//        val campaignInfoMap: CampaignInfoMap,
//        @JsonProperty("campaignZoneInfoMap")
//        val campaignZoneInfoMap: CampaignZoneInfoMap,
        @JsonProperty("charAssetList")
        val charAssetList: CharAssetList,
        @JsonProperty("charAssets")
        val charAssets: List<String>,
//        @JsonProperty("charInfoMap")
//        val charInfoMap: CharInfoMap,
        @JsonProperty("chars")
        val chars: List<Char>,
        @JsonProperty("currentTs")
        val currentTs: Long,
//        @JsonProperty("equipmentInfoMap")
//        val equipmentInfoMap: EquipmentInfoMap,
//        @JsonProperty("manufactureFormulaInfoMap")
//        val manufactureFormulaInfoMap: ManufactureFormulaInfoMap,
        @JsonProperty("recruit")
        val recruit: List<Recruit>,
//        @JsonProperty("rogue")
//        val rogue: Rogue,
//        @JsonProperty("rogueInfoMap")
//        val rogueInfoMap: RogueInfoMap,
        @JsonProperty("routine")
        val routine: Routine,

        @JsonProperty("skinAssetList")
        val skinAssetList: SkinAssetList,
        @JsonProperty("skinAssets")
        val skinAssets: List<String>,
//        @JsonProperty("skinInfoMap")
//        val skinInfoMap: SkinInfoMap,
        @JsonProperty("skins")
        val skins: List<Skin>,
//        @JsonProperty("stageInfoMap")
//        val stageInfoMap: StageInfoMap,
        @JsonProperty("status")
        val status: Status,
//        @JsonProperty("tower")
//        val tower: Tower,
//        @JsonProperty("towerInfoMap")
//        val towerInfoMap: TowerInfoMap
    ) {
        data class AssistChar(
            @JsonProperty("charId")
            val charId: String,
            @JsonProperty("equip")
            val equip: Equip,
            @JsonProperty("evolvePhase")
            val evolvePhase: Int,
            @JsonProperty("level")
            val level: Int,
            @JsonProperty("mainSkillLvl")
            val mainSkillLvl: Int,
            @JsonProperty("potentialRank")
            val potentialRank: Int,
            @JsonProperty("skillId")
            val skillId: String,
            @JsonProperty("skinId")
            val skinId: String,
            @JsonProperty("specializeLevel")
            val specializeLevel: Int
        ) {
            data class Equip(
                @JsonProperty("id")
                val id: String,
                @JsonProperty("level")
                val level: Int
            )
        }

        data class Building(
            @JsonProperty("control")
            val control: Control,
            @JsonProperty("corridors")
            val corridors: List<Corridor>,
            @JsonProperty("dormitories")
            val dormitories: List<Dormitory>,
            @JsonProperty("elevators")
            val elevators: List<Elevator>,
            @JsonProperty("furniture")
            val furniture: Furniture,
            @JsonProperty("hire")
            val hire: Hire,
            @JsonProperty("labor")
            val labor: Labor,
            @JsonProperty("manufactures")
            val manufactures: List<Manufacture>,
            @JsonProperty("meeting")
            val meeting: Meeting,
            @JsonProperty("powers")
            val powers: List<Power>,
            @JsonProperty("tiredChars")
            val tiredChars: List<Any>,
            @JsonProperty("tradings")
            val tradings: List<Trading>,
            @JsonProperty("training")
            val training: Training
        ) {
            data class Control(
                @JsonProperty("chars")
                val chars: List<Char>,
                @JsonProperty("level")
                val level: Int,
                @JsonProperty("slotId")
                val slotId: String,
                @JsonProperty("slotState")
                val slotState: Int
            ) {
                data class Char(
                    @JsonProperty("ap")
                    val ap: Int,
                    @JsonProperty("bubble")
                    val bubble: Bubble,
                    @JsonProperty("charId")
                    val charId: String,
                    @JsonProperty("index")
                    val index: Int,
                    @JsonProperty("lastApAddTime")
                    val lastApAddTime: Int,
                    @JsonProperty("workTime")
                    val workTime: Int
                ) {
                    data class Bubble(
                        @JsonProperty("assist")
                        val assist: Assist,
                        @JsonProperty("normal")
                        val normal: Normal
                    ) {
                        data class Assist(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )

                        data class Normal(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )
                    }
                }
            }

            data class Corridor(
                @JsonProperty("level")
                val level: Int,
                @JsonProperty("slotId")
                val slotId: String,
                @JsonProperty("slotState")
                val slotState: Int
            )

            data class Dormitory(
                @JsonProperty("chars")
                val chars: List<Char>,
                @JsonProperty("comfort")
                val comfort: Int,
                @JsonProperty("level")
                val level: Int,
                @JsonProperty("slotId")
                val slotId: String
            ) {
                data class Char(
                    @JsonProperty("ap")
                    val ap: Int,
                    @JsonProperty("bubble")
                    val bubble: Bubble,
                    @JsonProperty("charId")
                    val charId: String,
                    @JsonProperty("index")
                    val index: Int,
                    @JsonProperty("lastApAddTime")
                    val lastApAddTime: Int,
                    @JsonProperty("workTime")
                    val workTime: Int
                ) {
                    data class Bubble(
                        @JsonProperty("assist")
                        val assist: Assist,
                        @JsonProperty("normal")
                        val normal: Normal
                    ) {
                        data class Assist(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )

                        data class Normal(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )
                    }
                }
            }

            data class Elevator(
                @JsonProperty("level")
                val level: Int,
                @JsonProperty("slotId")
                val slotId: String,
                @JsonProperty("slotState")
                val slotState: Int
            )

            data class Furniture(
                @JsonProperty("total")
                val total: Int
            )

            data class Hire(
                @JsonProperty("chars")
                val chars: List<Char>,
                @JsonProperty("completeWorkTime")
                val completeWorkTime: Int,
                @JsonProperty("level")
                val level: Int,
                @JsonProperty("refreshCount")
                val refreshCount: Int,
                @JsonProperty("slotId")
                val slotId: String,
                @JsonProperty("slotState")
                val slotState: Int,
                @JsonProperty("state")
                val state: Int
            ) {
                data class Char(
                    @JsonProperty("ap")
                    val ap: Int,
                    @JsonProperty("bubble")
                    val bubble: Bubble,
                    @JsonProperty("charId")
                    val charId: String,
                    @JsonProperty("index")
                    val index: Int,
                    @JsonProperty("lastApAddTime")
                    val lastApAddTime: Int,
                    @JsonProperty("workTime")
                    val workTime: Int
                ) {
                    data class Bubble(
                        @JsonProperty("assist")
                        val assist: Assist,
                        @JsonProperty("normal")
                        val normal: Normal
                    ) {
                        data class Assist(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )

                        data class Normal(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )
                    }
                }
            }

            data class Labor(
                @JsonProperty("lastUpdateTime")
                val lastUpdateTime: Int,
                @JsonProperty("maxValue")
                val maxValue: Int,
                @JsonProperty("remainSecs")
                val remainSecs: Int,
                @JsonProperty("value")
                val value: Int
            )

            data class Manufacture(
                @JsonProperty("capacity")
                val capacity: Int,
                @JsonProperty("chars")
                val chars: List<Char>,
                @JsonProperty("complete")
                val complete: Int,
                @JsonProperty("completeWorkTime")
                val completeWorkTime: Int,
                @JsonProperty("formulaId")
                val formulaId: String,
                @JsonProperty("lastUpdateTime")
                val lastUpdateTime: Int,
                @JsonProperty("level")
                val level: Int,
                @JsonProperty("remain")
                val remain: Int,
                @JsonProperty("slotId")
                val slotId: String,
                @JsonProperty("speed")
                val speed: Double,
                @JsonProperty("weight")
                val weight: Int
            ) {
                data class Char(
                    @JsonProperty("ap")
                    val ap: Int,
                    @JsonProperty("bubble")
                    val bubble: Bubble,
                    @JsonProperty("charId")
                    val charId: String,
                    @JsonProperty("index")
                    val index: Int,
                    @JsonProperty("lastApAddTime")
                    val lastApAddTime: Int,
                    @JsonProperty("workTime")
                    val workTime: Int
                ) {
                    data class Bubble(
                        @JsonProperty("assist")
                        val assist: Assist,
                        @JsonProperty("normal")
                        val normal: Normal
                    ) {
                        data class Assist(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )

                        data class Normal(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )
                    }
                }
            }

            data class Meeting(
                @JsonProperty("chars")
                val chars: List<Char>,
                @JsonProperty("clue")
                val clue: Clue,
                @JsonProperty("completeWorkTime")
                val completeWorkTime: Int,
                @JsonProperty("lastUpdateTime")
                val lastUpdateTime: Int,
                @JsonProperty("level")
                val level: Int,
                @JsonProperty("slotId")
                val slotId: String
            ) {
                data class Char(
                    @JsonProperty("ap")
                    val ap: Int,
                    @JsonProperty("bubble")
                    val bubble: Bubble,
                    @JsonProperty("charId")
                    val charId: String,
                    @JsonProperty("index")
                    val index: Int,
                    @JsonProperty("lastApAddTime")
                    val lastApAddTime: Int,
                    @JsonProperty("workTime")
                    val workTime: Int
                ) {
                    data class Bubble(
                        @JsonProperty("assist")
                        val assist: Assist,
                        @JsonProperty("normal")
                        val normal: Normal
                    ) {
                        data class Assist(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )

                        data class Normal(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )
                    }
                }

                data class Clue(
                    @JsonProperty("board")
                    val board: List<String>,
                    @JsonProperty("dailyReward")
                    val dailyReward: Boolean,
                    @JsonProperty("needReceive")
                    val needReceive: Int,
                    @JsonProperty("own")
                    val own: Int,
                    @JsonProperty("received")
                    val received: Int,
                    @JsonProperty("shareCompleteTime")
                    val shareCompleteTime: Int,
                    @JsonProperty("sharing")
                    val sharing: Boolean
                )
            }

            data class Power(
                @JsonProperty("chars")
                val chars: List<Char>,
                @JsonProperty("level")
                val level: Int,
                @JsonProperty("slotId")
                val slotId: String
            ) {
                data class Char(
                    @JsonProperty("ap")
                    val ap: Int,
                    @JsonProperty("bubble")
                    val bubble: Bubble,
                    @JsonProperty("charId")
                    val charId: String,
                    @JsonProperty("index")
                    val index: Int,
                    @JsonProperty("lastApAddTime")
                    val lastApAddTime: Int,
                    @JsonProperty("workTime")
                    val workTime: Int
                ) {
                    data class Bubble(
                        @JsonProperty("assist")
                        val assist: Assist,
                        @JsonProperty("normal")
                        val normal: Normal
                    ) {
                        data class Assist(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )

                        data class Normal(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )
                    }
                }
            }

            data class Trading(
                @JsonProperty("chars")
                val chars: List<Char>,
                @JsonProperty("completeWorkTime")
                val completeWorkTime: Int,
                @JsonProperty("lastUpdateTime")
                val lastUpdateTime: Int,
                @JsonProperty("level")
                val level: Int,
                @JsonProperty("slotId")
                val slotId: String,
                @JsonProperty("stock")
                val stock: List<Stock>,
                @JsonProperty("stockLimit")
                val stockLimit: Int,
                @JsonProperty("strategy")
                val strategy: String
            ) {
                data class Char(
                    @JsonProperty("ap")
                    val ap: Int,
                    @JsonProperty("bubble")
                    val bubble: Bubble,
                    @JsonProperty("charId")
                    val charId: String,
                    @JsonProperty("index")
                    val index: Int,
                    @JsonProperty("lastApAddTime")
                    val lastApAddTime: Int,
                    @JsonProperty("workTime")
                    val workTime: Int
                ) {
                    data class Bubble(
                        @JsonProperty("assist")
                        val assist: Assist,
                        @JsonProperty("normal")
                        val normal: Normal
                    ) {
                        data class Assist(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )

                        data class Normal(
                            @JsonProperty("add")
                            val add: Int,
                            @JsonProperty("ts")
                            val ts: Int
                        )
                    }
                }

                data class Stock(
                    @JsonProperty("delivery")
                    val delivery: List<Delivery>,
                    @JsonProperty("gain")
                    val gain: Gain,
                    @JsonProperty("instId")
                    val instId: Int,
                    @JsonProperty("isViolated")
                    val isViolated: Boolean,
                    @JsonProperty("type")
                    val type: String
                ) {
                    data class Delivery(
                        @JsonProperty("count")
                        val count: Int,
                        @JsonProperty("id")
                        val id: String,
                        @JsonProperty("type")
                        val type: String
                    )

                    data class Gain(
                        @JsonProperty("count")
                        val count: Int,
                        @JsonProperty("id")
                        val id: String,
                        @JsonProperty("type")
                        val type: String
                    )
                }
            }

            data class Training(
                @JsonProperty("lastUpdateTime")
                val lastUpdateTime: Int,
                @JsonProperty("level")
                val level: Int,
                @JsonProperty("remainPoint")
                val remainPoint: Int,
                @JsonProperty("remainSecs")
                val remainSecs: Int,
                @JsonProperty("slotId")
                val slotId: String,
                @JsonProperty("slotState")
                val slotState: Int,
                @JsonProperty("speed")
                val speed: Double,
                @JsonProperty("trainee")
                val trainee: Trainee,
                @JsonProperty("trainer")
                val trainer: Trainer
            ) {
                data class Trainee(
                    @JsonProperty("ap")
                    val ap: Int,
                    @JsonProperty("charId")
                    val charId: String,
                    @JsonProperty("lastApAddTime")
                    val lastApAddTime: Int,
                    @JsonProperty("targetSkill")
                    val targetSkill: Int
                )

                data class Trainer(
                    @JsonProperty("ap")
                    val ap: Int,
                    @JsonProperty("charId")
                    val charId: String,
                    @JsonProperty("lastApAddTime")
                    val lastApAddTime: Int
                )
            }
        }

        data class CharAssetList(
            @JsonProperty("ids")
            val ids: List<String>
        )

        data class Char(
            @JsonProperty("charId")
            val charId: String,
            @JsonProperty("defaultEquipId")
            val defaultEquipId: String,
            @JsonProperty("defaultSkillId")
            val defaultSkillId: String,
            @JsonProperty("equip")
            val equip: List<Equip>,
            @JsonProperty("evolvePhase")
            val evolvePhase: Int,
            @JsonProperty("favorPercent")
            val favorPercent: Int,
            @JsonProperty("gainTime")
            val gainTime: Int,
            @JsonProperty("level")
            val level: Int,
            @JsonProperty("mainSkillLvl")
            val mainSkillLvl: Int,
            @JsonProperty("potentialRank")
            val potentialRank: Int,
            @JsonProperty("skills")
            val skills: List<Skill>,
            @JsonProperty("skinId")
            val skinId: String
        ) {
            data class Equip(
                @JsonProperty("id")
                val id: String,
                @JsonProperty("level")
                val level: Int
            )

            data class Skill(
                @JsonProperty("id")
                val id: String,
                @JsonProperty("specializeLevel")
                val specializeLevel: Int
            )
        }

        data class Recruit(
            @JsonProperty("finishTs")
            val finishTs: Long,
            @JsonProperty("startTs")
            val startTs: Long,
            @JsonProperty("state")
            val state: Int
        )

        data class Routine(
            @JsonProperty("daily")
            val daily: Daily,
            @JsonProperty("weekly")
            val weekly: Weekly
        ) {
            data class Daily(
                @JsonProperty("current")
                val current: Int,
                @JsonProperty("total")
                val total: Int
            )

            data class Weekly(
                @JsonProperty("current")
                val current: Int,
                @JsonProperty("total")
                val total: Int
            )
        }

        data class SkinAssetList(
            @JsonProperty("ids")
            val ids: List<String>
        )

        data class Skin(
            @JsonProperty("id")
            val id: String,
            @JsonProperty("ts")
            val ts: Int
        )

        data class Status(
            @JsonProperty("ap")
            val ap: Ap,
            @JsonProperty("avatar")
            val avatar: Avatar,
            @JsonProperty("charCnt")
            val charCnt: Int,
            @JsonProperty("furnitureCnt")
            val furnitureCnt: Int,
            @JsonProperty("lastOnlineTs")
            val lastOnlineTs: Long,
            @JsonProperty("level")
            val level: Int,
            @JsonProperty("mainStageProgress")
            val mainStageProgress: String,
            @JsonProperty("name")
            val name: String,
            @JsonProperty("registerTs")
            val registerTs: Long,
            @JsonProperty("resume")
            val resume: String,
            @JsonProperty("secretary")
            val secretary: Secretary,
            @JsonProperty("skinCnt")
            val skinCnt: Int,
            @JsonProperty("storeTs")
            val storeTs: Long,
            @JsonProperty("subscriptionEnd")
            val subscriptionEnd: Int,
            @JsonProperty("uid")
            val uid: String
        ) {
            data class Ap(
                @JsonProperty("completeRecoveryTime")
                val completeRecoveryTime: Int,
                @JsonProperty("current")
                val current: Int,
                @JsonProperty("lastApAddTime")
                val lastApAddTime: Int,
                @JsonProperty("max")
                val max: Int
            )

            data class Avatar(
                @JsonProperty("id")
                val id: String,
                @JsonProperty("type")
                val type: String
            )

            data class Secretary(
                @JsonProperty("charId")
                val charId: String,
                @JsonProperty("skinId")
                val skinId: String
            )
        }
    }
}