package com.blueskybone.arkscreen.common

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Space
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.updateMargins
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.util.dpToPx
import com.nex3z.flowlayout.FlowLayout

/**
 *   Created by blueskybone
 *   Date: 2025/1/22
 */

fun line(context: Context): View {
    val line = View(context)
    val params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2)
    params.updateMargins(0, 0, 0, 15)
    line.layoutParams = params
    line.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_500))
    return line
}

fun space(context: Context, dp: Float): Space {
    val px = dpToPx(context, dp)
    return Space(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            px.toInt()
        )
    }
}


//TODO：赶紧改了
private fun Button.setTagLayout(text: String) {
    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
    )
    layoutParams.setMargins(0, 0, dpToPx(context, 5F).toInt(), dpToPx(context, 5F).toInt())
    this.layoutParams = layoutParams
    this.text = text
    this.minWidth = 0
    this.minHeight = 0
    this.minimumWidth = 0
    this.minimumHeight = 0
    this.setPadding(
        dpToPx(context, 10F).toInt(),
        dpToPx(context, 5F).toInt(),
        dpToPx(context, 10F).toInt(),
        dpToPx(context, 5F).toInt()
    )
    this.isEnabled = true
    this.isSelected = false
    this.isClickable = true
    this.stateListAnimator = null
    this.setTextColor(Color.WHITE)
}

fun getFlowLayout(context: Context): FlowLayout {
    val flowLayout = FlowLayout(context)
    flowLayout.childSpacingForLastRow = FlowLayout.SPACING_ALIGN
    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
    )
    flowLayout.layoutParams = layoutParams
    return flowLayout
}

fun getFlowRadioGroup(context: Context): FlowRadioGroup {
    val flowRadioGroup = FlowRadioGroup(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val sectionView = findViewById<View>(R.id.Section)
            (layoutParams as? FrameLayout.LayoutParams)?.apply {
                topMargin = sectionView.bottom + sectionView.marginBottom
            }
        }
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

fun tagButton(context: Context, text: String): Button {
    val button = Button(context)
    button.setTagLayout(text)
    button.setBackgroundResource(R.drawable.button_tag)
    button.setOnClickListener {
        button.isSelected = !button.isSelected
    }
    return button
}


fun getCustomRadioGroup(context: Context): CustomRadioGroup {
    val customRadioGroup = CustomRadioGroup(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // 设置可聚焦
        isFocusable = true
        // 设置内边距
        setPadding(
            dpToPx(16), // 左
            dpToPx(16), // 上
            dpToPx(16), // 右
            dpToPx(16)  // 下
        )
    }
    return customRadioGroup
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
        background = ContextCompat.getDrawable(context, R.drawable.button_tag)
        contentDescription = contentInfo

        // 设置内边距
        val padding = (dpToPx(2))
        setPadding(padding, padding, padding, padding)
        scaleType = ImageView.ScaleType.FIT_XY
        setImageResource(drawableResId)
    }
}


fun bgImageButton(
    context: Context,
    drawableResId: Int,
    layoutWidth: Int = dpToPx(100),
    layoutHeight: Int = dpToPx(140)
): ImageButton {
    return ImageButton(context).apply {
        // 设置尺寸
        layoutParams = FrameLayout.LayoutParams(layoutWidth, layoutHeight).apply {
            // 转换dp为px
            val margin = dpToPx(8)
            // 设置margin方法1：统一设置
            setMargins(margin, margin, margin, margin)
        }
        // 设置图片缩放方式
        scaleType = ImageView.ScaleType.CENTER_CROP
        cropToPadding = true

        // 设置内边距
        val padding = (dpToPx(2))
        setPadding(padding, padding, padding, padding)

        // 设置背景
        background = ContextCompat.getDrawable(context, R.drawable.button_tag)
        // 设置图片资源
        setImageResource(drawableResId)
        tag = drawableResId
    }
}

fun getRadioButton(context: Context): RadioButton {
    return RadioButton(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // 设置 margin（需要转换为 MarginLayoutParams）
        val margin = (dpToPx(5))
        (layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(margin, margin, margin, margin)
    }
}