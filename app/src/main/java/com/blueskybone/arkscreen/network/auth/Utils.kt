package com.blueskybone.arkscreen.network.auth

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


fun hmacSha256(key: String, message: String): String {
    val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(secretKey)
    val hashBytes = mac.doFinal(message.toByteArray(Charsets.UTF_8))
    return hashBytes.joinToString("") { "%02x".format(it) }
}


private val hexDigits = "0123456789abcdef".toCharArray()

fun ByteArray.toHexString() = buildString(this.size shl 1) {
    this@toHexString.forEach { byte ->
        append(hexDigits[byte.toInt() ushr 4 and 15])
        append(hexDigits[byte.toInt() and 15])
    }
}

fun String.toMD5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray())
    return digest.toHexString()
}