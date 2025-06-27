package com.blueskybone.arkscreen.network.auth


import java.math.BigInteger
import java.security.MessageDigest


//Skland Authentication in HEADER <sign>
fun generateSign(api: String, params: String, key: String, timeStamp: String): String {
    val jsonArgs = "{\"platform\":\"\",\"timestamp\":\"$timeStamp\",\"dId\":\"\",\"vName\":\"\"}"
    val data = api + params + timeStamp + jsonArgs
//    println("generateSign: $data") // Replaced Log.e with println for simplicity
    val hmacData = hmacSha256(key, data)
    return hmacData.toMD5()
}