package com.blueskybone.arkscreen.task.screenshot

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 *   Created by blueskybone
 *   Date: 2025/1/25
 */
class TagViewGroup(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs) {

    companion object {
        const val gap = 10
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = width
        var childLeft = paddingLeft
        var childTop = paddingTop

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                if (childLeft + childWidth + paddingRight > width) {
                    childLeft = paddingLeft
                    childTop += childHeight + gap
                }
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
                childLeft += childWidth + gap
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        var height = 0
        var childLeft = paddingLeft
        var childTop = paddingTop

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                if (childLeft + childWidth + paddingRight > width) {
                    childLeft = paddingLeft
                    childTop += childHeight + gap
                }
                childLeft += childWidth
                height = childTop + childHeight + paddingBottom
            }
        }
        setMeasuredDimension(width, height)
    }
}