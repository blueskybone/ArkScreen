package com.blueskybone.arkscreen.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 *   Created by blueskybone
 *   Date: 2025/1/22
 */

class FlowLayout(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs) {

    companion object {
        const val gap = 15
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
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                if (childLeft + childWidth + paddingRight > width) {
                    childLeft = paddingLeft
                    childTop += childHeight + gap
                }
                childLeft += childWidth + gap
                height = height.coerceAtLeast(childTop + childHeight + paddingBottom)
            }
        }

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val measuredWidth = if (widthMode == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(widthMeasureSpec)
        } else {
            width
        }

        val measuredHeight = if (heightMode == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(heightMeasureSpec)
        } else {
            height
        }
        setMeasuredDimension(measuredWidth, measuredHeight)
    }
}