package com.blueskybone.arkscreen.ui.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * 用于包裹 ViewPager2 内部可滚动组件的布局，解决子页面滚动方向与 ViewPager2 相同时的
 * 手势冲突。可滚动元素必须是该布局唯一的直接子元素。
 *
 * 多层嵌套滚动时此方案存在限制，例如横向 ViewPager2 中的纵向 RecyclerView 又嵌套
 * 横向 RecyclerView。
 */

class NestedScrollableHost : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private var touchSlop = 0
    private var initialX = 0f
    private var initialY = 0f

    private val parentViewPager: ViewPager2?
        get() {
            var v: View? = parent as? View
            while (v != null && v !is ViewPager2) {
                v = v.parent as? View
            }
            return v as? ViewPager2
        }

    private val child: View? get() = if (childCount > 0) getChildAt(0) else null

    init {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    private fun canChildScroll(orientation: Int, delta: Float): Boolean {
        val direction = -delta.sign.toInt()
        return when (orientation) {
            0 -> child?.canScrollHorizontally(direction) ?: false
            1 -> child?.canScrollVertically(direction) ?: false
            else -> throw IllegalArgumentException()
        }
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        handleInterceptTouchEvent(e)
        return super.onInterceptTouchEvent(e)
    }

    private fun handleInterceptTouchEvent(e: MotionEvent) {
        val orientation = parentViewPager?.orientation ?: return
        // 子视图无法沿父视图方向滚动时无需处理手势冲突。
        if (!canChildScroll(orientation, -1f) && !canChildScroll(orientation, 1f)) {
            return
        }

        if (e.action == MotionEvent.ACTION_DOWN) {
            initialX = e.x
            initialY = e.y
            parent.requestDisallowInterceptTouchEvent(true)
        } else if (e.action == MotionEvent.ACTION_MOVE) {
            val dx = e.x - initialX
            val dy = e.y - initialY
            val isVpHorizontal = orientation == ORIENTATION_HORIZONTAL
            // ViewPager2 的触摸阈值约为子视图的两倍。
            val scaledDx = dx.absoluteValue * if (isVpHorizontal) .5f else 1f
            val scaledDy = dy.absoluteValue * if (isVpHorizontal) 1f else .5f

            if (scaledDx > touchSlop || scaledDy > touchSlop) {

                if (isVpHorizontal == (scaledDy > scaledDx)) {
                    // 手势方向垂直，允许父视图拦截。
                    parent.requestDisallowInterceptTouchEvent(false)
                } else {
                    // 手势方向平行，检查子视图能否继续向该方向滚动。
                    if (canChildScroll(orientation, if (isVpHorizontal) dx else dy)) {
                        // 子视图仍可滚动，禁止父视图拦截。
                        parent.requestDisallowInterceptTouchEvent(true)
                    } else {
                        // 子视图无法继续滚动，允许父视图拦截。
                        parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
            }
        }
    }
}
