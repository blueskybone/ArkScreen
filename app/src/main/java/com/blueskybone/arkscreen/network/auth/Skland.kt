package com.blueskybone.arkscreen.network.auth


//Skland Authentication in HEADER <sign>
fun generateSign(api: String, params: String, key: String, timeStamp: String, dId: String = ""): String {
    val jsonArgs = "{\"platform\":\"\",\"timestamp\":\"$timeStamp\",\"dId\":\"$dId\",\"vName\":\"\"}"
    val data = api + params + timeStamp + jsonArgs
//    println("generateSign: $data") // Replaced Log.e with println for simplicity
    val hmacData = hmacSha256(key, data)
    return hmacData.toMD5()
}