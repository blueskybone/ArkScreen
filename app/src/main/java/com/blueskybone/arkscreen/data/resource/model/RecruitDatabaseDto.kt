package com.blueskybone.arkscreen.data.resource.model

import com.blueskybone.arkscreen.domain.model.recruit.RecruitDatabase
import com.fasterxml.jackson.annotation.JsonProperty

data class RecruitDatabaseDto(
    @JsonProperty("new_ope") val newOpe: NewOpeDto,
    @JsonProperty("operator_high_list") val operatorHighList: List<OperatorDto>,
    @JsonProperty("operator_list") val operatorList: List<OperatorDto>,
    @JsonProperty("operator_low_list") val operatorLowList: List<OperatorDto>,
    @JsonProperty("operator_robot_list") val operatorRobotList: List<OperatorDto>,
    val update: UpdateDto,
) {
    data class NewOpeDto(val name: List<String>)
    data class OperatorDto(
        val name: String,
        val star: Int,
        val tag: List<String>,
        val skin: String,
    )
    data class UpdateDto(val date: String, val version: String)

    fun toDomain(): RecruitDatabase = RecruitDatabase(
        newOpe = RecruitDatabase.NewOpe(newOpe.name),
        operatorHighList = operatorHighList.map {
            RecruitDatabase.OperatorHigh(it.name, it.star, it.tag, it.skin)
        },
        operatorList = operatorList.map {
            RecruitDatabase.Operator(it.name, it.star, it.tag, it.skin)
        },
        operatorLowList = operatorLowList.map {
            RecruitDatabase.OperatorLow(it.name, it.star, it.tag, it.skin)
        },
        operatorRobotList = operatorRobotList.map {
            RecruitDatabase.OperatorRobot(it.name, it.star, it.tag, it.skin)
        },
        update = RecruitDatabase.Update(update.date, update.version),
    )
}
