package com.blueskybone.arkscreen.ui.recruit.screenshot

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager

data class FloatingAppearance(
    val width: Int = 200, val height: Int = 240, val font: Int = 85,
    val colorful: Boolean = true, val ballSize: Int = 56, val ballColor: String = "blue",
) {
    companion object {
        val colors = listOf("blue", "cyan", "violet", "pink", "red", "orange", "yellow", "green", "teal", "gray")
        val colorNames = listOf("蓝色", "蓝青", "紫色", "玫红", "红色", "橙色", "黄色", "绿色", "青色", "灰色")
        fun read(p: SettingPrefManager) = FloatingAppearance(
            p.floatingWidth.get().coerceIn(140, 260), p.floatingHeight.get().coerceIn(160, 360),
            p.floatingFont.get().coerceIn(60, 100), true,
            p.floatingBallSize.get().coerceIn(48, 72), p.floatingBallColor.get().takeIf { it in colors } ?: "blue")
    }
    fun save(p: SettingPrefManager) {
        p.floatingWidth.set(width); p.floatingHeight.set(height); p.floatingFont.set(font)
        p.floatWindowAppearance.set(if (colorful) "colorful" else "simple")
        p.floatingBallSize.set(ballSize); p.floatingBallColor.set(ballColor)
    }
    fun fillColor(): Int = Color.parseColor(when (ballColor) {
        "cyan" -> "#009FC4"; "violet" -> "#913DE0"; "pink" -> "#E63278"
        "red" -> "#E94242"; "orange" -> "#F58220"; "yellow" -> "#F5CE32"
        "teal" -> "#00B8A5"; "gray" -> "#898B91"
        "green" -> "#38B54A"; else -> "#2877F0"
    })
    fun iconColor(context: Context): Int = if (ballColor == "yellow") Color.rgb(48, 42, 12) else Color.WHITE
    fun ball(context: Context): FrameLayout {
        fun dp(v: Int) = (v * context.resources.displayMetrics.density).toInt()
        return FrameLayout(context).apply {
            setPadding(dp(8), dp(8), dp(8), dp(8))
            background = CompactBallShadow(context.resources.displayMetrics.density)
            clipChildren = false; clipToPadding = false
            addView(ImageView(context).apply {
                id = android.R.id.icon
                contentDescription = context.getString(R.string.recruit)
                setImageResource(R.drawable.ic_ball_scan)
                setColorFilter(iconColor(context))
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(fillColor())
                }
                elevation = 0f
                setPadding(dp(ballSize / 4), dp(ballSize / 4), dp(ballSize / 4), dp(ballSize / 4))
            }, FrameLayout.LayoutParams(dp(ballSize), dp(ballSize)))
        }
    }
}
