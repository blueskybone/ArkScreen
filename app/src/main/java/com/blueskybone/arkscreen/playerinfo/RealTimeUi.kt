package com.blueskybone.arkscreen.playerinfo

/**
 *   Created by blueskybone
 *   Date: 2025/1/17
 */
data class RealTimeUi(
    var nickName: String = "",
    var lastLogin: String = "",
    var level: Int = 0,
    var avatarUrl: String = "",
    var apMax: String = "",
    var apNow: String = "",
    var apResTime: String = "",
    var recruit: PairInfo = PairInfo(),
    var recruitRefresh: PairInfo = PairInfo(),
    var labor: PairInfo = PairInfo(),
    var meeting: PairInfo = PairInfo(),
    var manufacture: PairInfo = PairInfo(),
    var trading: PairInfo = PairInfo(),
    var dormitories: PairInfo = PairInfo(),
    var tired: PairInfo = PairInfo(),
    var train: PairInfo = PairInfo(),
    var campaign: PairInfo = PairInfo(),
    var displayChange: Boolean = false,
    var logosChange: TrainChange = TrainChange(),
    var ireneChange: TrainChange = TrainChange(),
    var official: Boolean = true
) {
    data class PairInfo(
        var value: String = "",
        var time: String = ""
    )

    data class TrainChange(
        var display: Boolean = false,
        var text: String = "",
        var timeStamp: Long = -1L
    )
}