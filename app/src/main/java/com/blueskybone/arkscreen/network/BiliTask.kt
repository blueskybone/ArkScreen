package com.blueskybone.arkscreen.network

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL


data class BiliVideo(
    val pic: String,
    val bvid: String
)

suspend fun getVideoList(): List<BiliVideo> {
//    val params = mapOf("mid" to 161775300,
//        "pn" to "1",
//        "ps" to "3",
//        "tid" to "0",
//        "mid" to "161775300",
//        "dm_img_list" to "[]",
//        "dm_img_str" to "V2ViR0wgMS4wIChPcGVuR0wgRVMgMi4wIENocm9taXVtKQ",
//        "dm_cover_img_str" to "QU5HTEUgKE5WSURJQSwgTlZJRElBIEdlRm9yY2UgUlRYIDQwNjAgVGkgKDB4MDAwMDI4MDMpIERpcmVjdDNEMTEgdnNfNV8wIHBzXzVfMCwgRDNEMTEpR29vZ2xlIEluYy4gKE5WSURJQS",
//        "dm_img_inter" to "{\"ds\":[],\"wh\":[4574,3608,34],\"of\":[16,32,16]}",
//    )
//    if (wbiParams == null) {
//        wbiParams = getBiliWbi()
//    }
//    val url = "https://api.bilibili.com/x/space/wbi/arc/search?" + wbiParams!!.enc(params)
    val url = "https://app.biliapi.com/x/v2/space/archive/cursor?order=pubdate&vmid=161775300&order=click&ps=3"
    val resp = makeSuspendRequest(URL(url))
    try {
        val biliList = arrayListOf<BiliVideo>()
        val om = ObjectMapper()
        val tree = om.readTree(resp)
        val vlist = tree["data"]["item"]
        for (v in vlist) {
            biliList.add(
                BiliVideo(
                    v["cover"].asText(),
                    v["bvid"].asText()
                )
            )
        }
        return biliList
    } catch (e: Exception) {
        e.printStackTrace()

        throw Exception("bili video json read failed: e.msg: ${e.message} json content: $resp")
    }
}


//suspend fun getSpaceTitleImageUrl(): String {
//    val resp = makeSuspendRequest(URL(biliSettingUrl))
//    return titleImageUrl + getJsonContent(resp, "s_img")
//}