package com.blueskybone.arkscreen.ui.realtime.model

import android.content.Context
import androidx.annotation.StringRes

sealed interface UiText {
    data class Raw(val value: String) : UiText
    data class Resource(
        @StringRes val id: Int,
        val args: List<Any> = emptyList(),
    ) : UiText

    fun resolve(context: Context): String = when (this) {
        is Raw -> value
        is Resource -> context.getString(id, *args.toTypedArray())
    }
}

data class RealTimeUi(
    val nickName: String,
    val lastLogin: UiText,
    val level: Int,
    val avatarUrl: String,
    val apMax: Int,
    val apNow: Int,
    val apFullTime: UiText,
    val apResTime: UiText,
    val recruit: PairInfo,
    val recruitRefresh: PairInfo,
    val labor: PairInfo,
    val meeting: PairInfo,
    val manufacture: PairInfo,
    val trading: PairInfo,
    val dormitories: PairInfo,
    val tired: PairInfo,
    val train: PairInfo,
    val campaign: PairInfo,
    val logosChange: TrainChange?,
    val ireneChange: TrainChange?,
    val official: Boolean,
) {
    data class PairInfo(
        val value: UiText,
        val time: UiText = UiText.Raw(""),
        val notify: Boolean = false,
    )

    data class TrainChange(
        val text: UiText,
        val timeStamp: Long,
    )
}
