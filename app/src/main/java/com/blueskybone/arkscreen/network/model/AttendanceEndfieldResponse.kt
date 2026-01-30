package com.blueskybone.arkscreen.network.model

import org.json.JSONObject

data class AttendanceEndfieldResponse(
    val code: Int,
    val message: String,
    val data: EndfieldAwards
)

data class EndfieldAwards(
    val awardIds: List<Awards>,
    val resourceInfoMap: JSONObject? = null
)

data class Awards(
    val id: String,
    val type: Int
)