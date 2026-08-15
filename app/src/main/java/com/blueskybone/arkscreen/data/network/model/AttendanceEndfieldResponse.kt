package com.blueskybone.arkscreen.data.network.model

import com.fasterxml.jackson.databind.JsonNode

data class AttendanceEndfieldResponse(
    val code: Int,
    val message: String,
    val data: EndfieldAwards
)

data class EndfieldAwards(
    val awardIds: List<Awards>,
    val resourceInfoMap: JsonNode? = null
)

data class Awards(
    val id: String,
    val type: Int
)
