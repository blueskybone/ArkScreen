package com.blueskybone.arkscreen.domain.model.cache

data class EndfieldStaminaCache(
    val uid: String = "",
    val sampledAt: Long = 0,
    val current: Long = 0,
    val maximum: Long = 0,
    val fullAt: Long = 0,
) {
    val isValid: Boolean get() = uid.isNotBlank() && sampledAt > 0 && maximum > 0
}
