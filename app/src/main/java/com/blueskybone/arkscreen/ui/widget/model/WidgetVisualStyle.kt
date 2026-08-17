package com.blueskybone.arkscreen.ui.widget.model

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.WidgetTemplatePrefManager

data class WidgetPalette(
    @param:DrawableRes val backgroundRes: Int,
    @param:ColorInt val primaryText: Int,
    @param:ColorInt val secondaryText: Int,
    @param:ColorInt val divider: Int,
    @param:ColorInt val actionIcon: Int,
    @param:ColorInt val starterIcon: Int?,
    @param:ColorInt val attention: Int,
    @param:ColorInt val completed: Int,
    @param:ColorInt val error: Int,
    @param:ColorInt val idle: Int,
    @param:ColorInt val unavailable: Int,
) {
    fun stateColor(state: WidgetInfoState): Int = when (state) {
        WidgetInfoState.NORMAL -> primaryText
        WidgetInfoState.ATTENTION -> attention
        WidgetInfoState.COMPLETED -> completed
        WidgetInfoState.ERROR -> error
        WidgetInfoState.IDLE -> idle
        WidgetInfoState.DISABLED,
        WidgetInfoState.UNAVAILABLE -> unavailable
    }
}

enum class WidgetBackgroundSize {
    ONE_BY_ONE,
    ONE_BY_TWO,
    TWO_BY_TWO,
    TWO_BY_THREE,
}

enum class WidgetVisualStyle(
    val key: String,
    val palette: WidgetPalette,
) {
    CLEAR(
        WidgetTemplatePrefManager.STYLE_TRANSPARENT,
        WidgetPalette(
            backgroundRes = R.drawable.widget_next_bg_transparent,
            primaryText = Color.WHITE,
            secondaryText = Color.rgb(200, 203, 210),
            divider = Color.argb(61, 255, 255, 255),
            actionIcon = Color.WHITE,
            starterIcon = Color.rgb(248,222,5),
            attention = Color.rgb(255, 209, 102),
            completed = Color.rgb(126, 217, 154),
            error = Color.rgb(255, 138, 128),
            idle = Color.rgb(184, 188, 198),
            unavailable = Color.rgb(138, 141, 150),
        ),
    ),
    GRAPHITE(
        WidgetTemplatePrefManager.STYLE_DARK,
        WidgetPalette(
            backgroundRes = R.drawable.widget_next_bg_dark,
            primaryText = Color.rgb(246, 247, 249),
            secondaryText = Color.rgb(190, 194, 202),
            divider = Color.rgb(70, 73, 82),
            actionIcon = Color.rgb(246, 247, 249),
            starterIcon = Color.rgb(248,222,5),
            attention = Color.rgb(255, 202, 82),
            completed = Color.rgb(113, 210, 145),
            error = Color.rgb(255, 126, 120),
            idle = Color.rgb(169, 173, 183),
            unavailable = Color.rgb(125, 129, 139),
        ),
    ),
    MIST(
        WidgetTemplatePrefManager.STYLE_MIST,
        WidgetPalette(
            backgroundRes = R.drawable.widget_next_bg_mist,
            primaryText = Color.rgb(38, 42, 49),
            secondaryText = Color.rgb(91, 97, 108),
            divider = Color.argb(65, 49, 54, 63),
            actionIcon = Color.rgb(38, 42, 49),
            starterIcon = Color.rgb(61, 72, 84),
            attention = Color.rgb(154, 103, 0),
            completed = Color.rgb(19, 115, 51),
            error = Color.rgb(179, 38, 30),
            idle = Color.rgb(107, 111, 120),
            unavailable = Color.rgb(137, 141, 149),
        ),
    ),
    RHODES(
        WidgetTemplatePrefManager.STYLE_RHODES,
        WidgetPalette(
            backgroundRes = R.drawable.widget_next_bg_rhodes_island,
            primaryText = Color.rgb(238, 248, 250),
            secondaryText = Color.rgb(158, 190, 198),
            divider = Color.argb(80, 100, 200, 220),
            actionIcon = Color.rgb(114, 213, 231),
            starterIcon = Color.rgb(248,222,5),
            attention = Color.rgb(240, 182, 74),
            completed = Color.rgb(100, 218, 151),
            error = Color.rgb(255, 121, 112),
            idle = Color.rgb(148, 164, 171),
            unavailable = Color.rgb(106, 122, 130),
        ),
    ),
    SKLAND(
        WidgetTemplatePrefManager.STYLE_SKLAND,
        WidgetPalette(
            backgroundRes = R.drawable.widget_next_bg_skland,
            primaryText = Color.rgb(42, 49, 32),
            secondaryText = Color.rgb(86, 98, 65),
            divider = Color.argb(64, 91, 111, 55),
            actionIcon = Color.rgb(91, 115, 37),
            starterIcon = Color.rgb(103, 130, 48),
            attention = Color.rgb(146, 104, 8),
            completed = Color.rgb(35, 112, 54),
            error = Color.rgb(177, 53, 43),
            idle = Color.rgb(104, 113, 87),
            unavailable = Color.rgb(137, 145, 121),
        ),
    ),
    RHINE(
        WidgetTemplatePrefManager.STYLE_RHINE,
        WidgetPalette(
            backgroundRes = R.drawable.widget_next_bg_rhine,
            primaryText = Color.rgb(255, 255, 255),
            secondaryText = Color.rgb(226, 233, 220),
            divider = Color.argb(102, 244, 248, 239),
            actionIcon = Color.rgb(255, 255, 255),
            starterIcon = Color.rgb(218, 239, 158),
            attention = Color.rgb(255, 220, 112),
            completed = Color.rgb(184, 236, 174),
            error = Color.rgb(255, 167, 153),
            idle = Color.rgb(211, 220, 205),
            unavailable = Color.rgb(172, 183, 166),
        ),
    ),
    SUI(
        WidgetTemplatePrefManager.STYLE_SUI,
        WidgetPalette(
            backgroundRes = R.drawable.widget_next_bg_sui,
            primaryText = Color.rgb(243, 239, 232),
            secondaryText = Color.rgb(185, 176, 165),
            divider = Color.argb(62, 198, 190, 180),
            actionIcon = Color.rgb(215, 208, 199),
            starterIcon = Color.rgb(169, 67, 58),
            attention = Color.rgb(215, 166, 89),
            completed = Color.rgb(112, 188, 137),
            error = Color.rgb(218, 101, 88),
            idle = Color.rgb(157, 151, 143),
            unavailable = Color.rgb(112, 108, 103),
        ),
    ),
    LONETRAIL(
        WidgetTemplatePrefManager.STYLE_LONETRAIL,
        WidgetPalette(
            backgroundRes = R.drawable.widget_next_bg_lonetrail,
            primaryText = Color.rgb(244, 239, 226),
            secondaryText = Color.rgb(194, 181, 158),
            divider = Color.argb(72, 205, 190, 164),
            actionIcon = Color.rgb(232, 108, 58),
            starterIcon = Color.rgb(232, 108, 58),
            attention = Color.rgb(210, 165, 75),
            completed = Color.rgb(112, 166, 157),
            error = Color.rgb(226, 101, 72),
            idle = Color.rgb(169, 158, 140),
            unavailable = Color.rgb(126, 119, 108),
        ),
    );

    fun backgroundRes(size: WidgetBackgroundSize): Int = palette.backgroundRes

    companion object {
        fun fromKey(key: String): WidgetVisualStyle =
            entries.firstOrNull { it.key == key } ?: CLEAR
    }
}
