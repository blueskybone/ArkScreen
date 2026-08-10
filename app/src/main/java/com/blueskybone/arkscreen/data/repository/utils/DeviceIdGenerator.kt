package com.blueskybone.arkscreen.data.repository.utils

import java.security.SecureRandom
import java.util.Base64

fun generateDId(): String {
    val randomBytes = ByteArray(32).also(SecureRandom()::nextBytes)
    return "BL${Base64.getEncoder().encodeToString(randomBytes)}"
}
