package com.blueskybone.arkscreen.domain.model.recruit

/**
 *   Created by blueskybone
 *   Date: 2025/1/21
 */
data class RecruitDatabase(
    val newOpe: NewOpe,
    val operatorHighList: List<OperatorHigh>,
    val operatorList: List<Operator>,
    val operatorLowList: List<OperatorLow>,
    val operatorRobotList: List<OperatorRobot>,
    val update: Update
) {
    data class NewOpe(
        val name: List<String>
    )

    data class OperatorHigh(
        val name: String,
        val star: Int,
        val tag: List<String>,
        val skin: String
    )

    data class Operator(
        val name: String,
        val star: Int,
        val tag: List<String>,
        val skin: String
    )

    data class OperatorLow(
        val name: String,
        val star: Int,
        val tag: List<String>,
        val skin: String
    )

    data class OperatorRobot(
        val name: String,
        val star: Int,
        val tag: List<String>,
        val skin: String
    )

    data class Update(
        val date: String,
        val version: String
    )
}
