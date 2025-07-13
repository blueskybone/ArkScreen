package com.blueskybone.arkscreen.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.google.android.material.R

class CustomRadioGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.radioButtonStyle
) : androidx.appcompat.widget.LinearLayoutCompat(context, attrs, defStyleAttr) {

    private var checkedId = View.NO_ID
    private var protectFromCheckedChange = false
    private var onCheckedChangeListener: OnCheckedChangeListener? = null

    init {
        orientation = HORIZONTAL
        val padding = dpToPx(16)
        setPadding(padding, padding, padding, padding)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        if (child.id == View.NO_ID) {
            child.id = View.generateViewId()
        }

        super.addView(child, index, params)

        if (child is ImageButton) {
            child.setOnClickListener {
                check(child.id)
            }

            // 如果此时子View标记为Selected，设置checkedId
            if (child.isSelected) {
                protectFromCheckedChange = true
                if (checkedId != View.NO_ID && checkedId != child.id) {
                    findViewById<ImageButton>(checkedId)?.isSelected = false
                }
                checkedId = child.id
                protectFromCheckedChange = false
            } else if (child.id == checkedId) {
                // 对应XML默认选中id，主动设置selected状态
                child.isSelected = true
            }
        }
    }

    override fun onViewRemoved(child: View) {
        super.onViewRemoved(child)

        if (child is ImageButton) {
            if (child.id == checkedId) {
                // 移除的是选中的按钮，重置checkedId
                clearCheck()
            }
        }
    }

    fun check(id: Int) {
        if (id == checkedId) return

        protectFromCheckedChange = true

        // 取消之前选中
        if (checkedId != View.NO_ID) {
            findViewById<ImageButton>(checkedId)?.isSelected = false
        }

        if (id != View.NO_ID) {
            findViewById<ImageButton>(id)?.isSelected = true
        }

        checkedId = id
        protectFromCheckedChange = false

        onCheckedChangeListener?.let {
                // 刚才已关闭保护，安全调用即可
                it.onCheckedChanged(this, checkedId)

        }
    }

    fun getCheckedImageButtonId(): Int = checkedId

    fun clearCheck() {
        check(View.NO_ID)
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        onCheckedChangeListener = listener
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(group: CustomRadioGroup, checkedId: Int)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }

}