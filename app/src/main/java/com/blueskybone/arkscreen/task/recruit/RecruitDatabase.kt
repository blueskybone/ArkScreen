package com.blueskybone.arkscreen.task.recruit

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *   Created by blueskybone
 *   Date: 2025/1/21
 */
data class RecruitDatabase(
    @JsonProperty("new_ope")
    val newOpe: NewOpe,
    @JsonProperty("operator_high_list")
    val operatorHighList: List<OperatorHigh>,
    @JsonProperty("operator_list")
    val operatorList: List<Operator>,
    @JsonProperty("operator_low_list")
    val operatorLowList: List<OperatorLow>,
    @JsonProperty("operator_robot_list")
    val operatorRobotList: List<OperatorRobot>,
    @JsonProperty("update")
    val update: Update
) {
    data class NewOpe(
        @JsonProperty("name")
        val name: List<String>
    )

    data class OperatorHigh(
        @JsonProperty("name")
        val name: String,
        @JsonProperty("star")
        val star: Int,
        @JsonProperty("tag")
        val tag: List<String>
    )

    data class Operator(
        @JsonProperty("name")
        val name: String,
        @JsonProperty("star")
        val star: Int,
        @JsonProperty("tag")
        val tag: List<String>
    )

    data class OperatorLow(
        @JsonProperty("name")
        val name: String,
        @JsonProperty("star")
        val star: Int,
        @JsonProperty("tag")
        val tag: List<String>
    )

    data class OperatorRobot(
        @JsonProperty("name")
        val name: String,
        @JsonProperty("star")
        val star: Int,
        @JsonProperty("tag")
        val tag: List<String>
    )

    data class Update(
        @JsonProperty("date")
        val date: String,
        @JsonProperty("version")
        val version: String
    )
}