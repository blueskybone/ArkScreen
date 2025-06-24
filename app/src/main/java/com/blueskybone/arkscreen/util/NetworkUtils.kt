package com.blueskybone.arkscreen.util

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


@Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
fun calculateHmacSha256(message: String, key: String): ByteArray {
    val hmac = Mac.getInstance("HmacSHA256")
    val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
    hmac.init(secretKey)
    return hmac.doFinal(message.toByteArray(StandardCharsets.UTF_8))
}

@Throws(NoSuchAlgorithmException::class)
fun calculateMD5(message: String): String {
    val md5Bytes = MessageDigest.getInstance("MD5").digest(message.toByteArray(StandardCharsets.UTF_8))
    val no = BigInteger(1, md5Bytes)
    var hashText = no.toString(16)
    while (hashText.length < 32) {
        hashText = "0$hashText"
    }
    return hashText
}

private fun bytesToHex(bytes: ByteArray): String {
    val result = StringBuilder()
    for (b in bytes) {
        result.append(String.format("%02x", b))
    }
    return result.toString()
}

@Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
fun generateSign(api: String, params: String, key: String, timeStamp: String): String {
    val jsonArgs = """{"platform":"","timestamp":"$timeStamp","dId":"","vName":""}"""
    val data = api + params + timeStamp + jsonArgs
    // 如果你使用 Android，可以用 Log.e ；纯Kotlin项目请换成其他日志打印
    println("generateSign: $data")
    val hmacData = calculateHmacSha256(data, key)
    val hmacSha256Hex = bytesToHex(hmacData)
    return calculateMD5(hmacSha256Hex)
}