package com.blueskybone.arkscreen.ui.common.view

import android.content.Context
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.util.dpToPx
import com.google.android.material.chip.Chip

/**
 *   Created by blueskybone
 *   Date: 2025/1/22
 */

fun getFlowRadioGroup(context: Context): FlowRadioGroup {
    val flowRadioGroup = FlowRadioGroup(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // 设置可聚焦
        isFocusable = true
        // 设置内边距
        setPadding(
            dpToPx(8), // 左
            dpToPx(8), // 上
            dpToPx(8), // 右
            dpToPx(8)  // 下
        )
        val typedArray = context.obtainStyledAttributes(
            null,
            R.styleable.FlowRadioGroup,
            0,
            0
        )
        try {
            // 设置水平间距
            horizontalSpacing = typedArray.getDimensionPixelSize(
                R.styleable.FlowRadioGroup_horizontalSpacing,
                dpToPx(5)
            )

            // 设置垂直间距
            verticalSpacing = typedArray.getDimensionPixelSize(
                R.styleable.FlowRadioGroup_verticalSpacing,
                dpToPx(5)
            )
        } finally {
            typedArray.recycle()
        }
    }
    return flowRadioGroup
}

/** Inflate the same filter capsule style used by XML pages. Selection is owned by the group. */
fun tagButton(context: Context, text: String): Chip =
    (android.view.LayoutInflater.from(context).inflate(R.layout.item_filter_chip, null) as Chip).apply {
        this.text = text
    }

fun profImageButton(
    context: Context,
    drawableResId: Int,
    contentInfo: String,
    layoutWidth: Int = dpToPx(36),
    layoutHeight: Int = dpToPx(36)
): ImageButton {
    return ImageButton(context).apply {
        // 设置布局参数
        layoutParams = FrameLayout.LayoutParams(layoutWidth, layoutHeight)

        // 设置背景
        background = ContextCompat.getDrawable(context, R.drawable.bg_profession_filter)
        contentDescription = contentInfo

        // 设置内边距
        val padding = (dpToPx(2))
        setPadding(padding, padding, padding, padding)
        scaleType = ImageView.ScaleType.FIT_XY
        setImageResource(drawableResId)
    }
}
