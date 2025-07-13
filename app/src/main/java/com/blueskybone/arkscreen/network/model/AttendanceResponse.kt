package com.blueskybone.arkscreen.network.model

data class AttendanceResponse(
    val code: Int,
    val message: String,
    val data: AwardsData
)

data class AwardsData(
    val awards: List<AwardsItem>
)

data class AwardsItem(
    val resource: AwardResource,
    val count:Int
)

data class AwardResource(
    val name:String,
    val rarity:Int,
    val type:String
)