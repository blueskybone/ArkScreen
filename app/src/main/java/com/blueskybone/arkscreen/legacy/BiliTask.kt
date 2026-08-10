package com.blueskybone.arkscreen.legacy

import com.blueskybone.arkscreen.data.network.makeBiliWbiRequest
import com.fasterxml.jackson.databind.ObjectMapper


//TODO：处理一下这个
//data class BiliVideo(
//    val pic: String,
//    val bvid: String
//)
//
//suspend fun getVideoList(): List<BiliVideo> {
//    val url = "https://app.biliapi.com/x/v2/space/archive/cursor"
//    val timestamp = System.currentTimeMillis() / 1000
//    val params = mutableMapOf(
//        "vmid" to "161775300",
//        "order" to "pubdate",
//        "ps" to "3",
//        "ts" to timestamp.toString()
//    )
//    val resp = makeBiliWbiRequest(url, params)
//    try {
//        val biliList = arrayListOf<BiliVideo>()
//        val om = ObjectMapper()
//        val tree = om.readTree(resp)
//        val vlist = tree["data"]["item"]
//        for (v in vlist) {
//            biliList.add(
//                BiliVideo(
//                    v["cover"].asText(),
//                    v["bvid"].asText()
//                )
//            )
//        }
//        return biliList
//    } catch (e: Exception) {
//        e.printStackTrace()
//        throw Exception("bili video json read failed: e.msg: ${e.message} json content: $resp")
//    }
//}