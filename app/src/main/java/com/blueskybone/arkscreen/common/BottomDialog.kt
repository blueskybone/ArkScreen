package com.blueskybone.arkscreen.common

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class BottomDialog(context: Context) : BottomSheetDialog(context) {

    private lateinit var textView: TextView
    private lateinit var button: Button
    private var buttonClickListener: View.OnClickListener? = null

    init {
        createDialog()
    }

    private fun createDialog() {
        // 主容器
        val container = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // 内容容器（包含滚动文本和按钮）
        val contentContainer = CoordinatorLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dpToPx(300) // 固定高度为200dp
            )
        }

        // NestedScrollView
        val scrollView = NestedScrollView(context).apply {
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(64))
        }

        // TextView
        textView = TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Button
        button = Button(context).apply {
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                dpToPx(48)
            ).apply {
                gravity = Gravity.BOTTOM
                bottomMargin = dpToPx(16)
                leftMargin = dpToPx(16)
                rightMargin = dpToPx(16)
            }
            setOnClickListener { buttonClickListener?.onClick(it) }
        }

        // 组装视图
        scrollView.addView(textView)
        contentContainer.addView(scrollView)
        contentContainer.addView(button)
        container.addView(contentContainer)

        setContentView(container)

        // 设置BottomSheet行为
        setOnShowListener {
            val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isHideable = true
        }
    }

    // 设置文本内容
    fun setText(text: String): BottomDialog {
        textView.text = text
        return this
    }

    // 设置按钮文本
    fun setButtonText(text: String): BottomDialog {
        button.text = text
        return this
    }

    // 设置按钮点击事件
    fun setButtonOnclick(listener: View.OnClickListener): BottomDialog {
        buttonClickListener = listener
        return this
    }

    // dp转px
    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}