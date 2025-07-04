package com.blueskybone.arkscreen.common


import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.blueskybone.arkscreen.R

@SuppressLint("CustomViewStyleable")
class FlowRadioGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private var checkedId = -1
    private var onCheckedChangeListener: OnCheckedChangeListener? = null
    var horizontalSpacing = 0
    var verticalSpacing = 0

    init {
        // 获取自定义属性
        val a = context.obtainStyledAttributes(attrs, R.styleable.FlowRadioGroup)
        horizontalSpacing = a.getDimensionPixelSize(
            R.styleable.FlowRadioGroup_horizontalSpacing,
            0
        )
        verticalSpacing = a.getDimensionPixelSize(
            R.styleable.FlowRadioGroup_verticalSpacing,
            0
        )
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width = 0
        var height = 0
        var lineWidth = 0
        var lineHeight = 0

        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue

            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
            val lp = child.layoutParams as MarginLayoutParams

            val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin + horizontalSpacing
            val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

            if (lineWidth + childWidth > widthSize - paddingLeft - paddingRight) {
                width = maxOf(width, lineWidth)
                lineWidth = childWidth
                height += lineHeight + verticalSpacing
                lineHeight = childHeight
            } else {
                lineWidth += childWidth
                lineHeight = maxOf(lineHeight, childHeight)
            }

            if (i == count - 1) {
                width = maxOf(width, lineWidth)
                height += lineHeight
            }
        }

        setMeasuredDimension(
            if (widthMode == MeasureSpec.EXACTLY) widthSize else width + paddingLeft + paddingRight,
            if (heightMode == MeasureSpec.EXACTLY) heightSize else height + paddingTop + paddingBottom
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        var x = paddingLeft
        var y = paddingTop
        var lineHeight = 0

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue

            val lp = child.layoutParams as MarginLayoutParams
            val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin + horizontalSpacing
            val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

            if (x + childWidth > width - paddingRight) {
                x = paddingLeft
                y += lineHeight + verticalSpacing
                lineHeight = 0
            }

            val left = x + lp.leftMargin
            val top = y + lp.topMargin
            val right = left + child.measuredWidth
            val bottom = top + child.measuredHeight

            child.layout(left, top, right, bottom)

            x += childWidth
            lineHeight = maxOf(lineHeight, childHeight)
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun checkLayoutParams(p: LayoutParams?): Boolean {
        return p is MarginLayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun addView(child: View?, index: Int, params: LayoutParams?) {
        if (child != null && shouldAddAsRadioChild(child)) {
            setupRadioChild(child)
        }
        super.addView(child, index, params)
    }

    private fun shouldAddAsRadioChild(child: View) =
        child is android.widget.Button || child is android.widget.ImageButton

    private fun setupRadioChild(child: View) {
        if (child.id == View.NO_ID) {
            child.id = View.generateViewId()
        }

        child.isClickable = true
        child.setOnClickListener(childOnClickListener)
        child.isFocusable = true
    }

    private val childOnClickListener = OnClickListener { v ->
        onChildClicked(v)
    }

    private fun onChildClicked(v: View) {
        val id = v.id
        if (id == checkedId) {
            clearCheck()
        } else {
            check(id)
        }
    }

    private fun check(id: Int) {
        if (id != -1 && id == checkedId) {
            return
        }

        if (checkedId != -1) {
            setCheckedStateForView(checkedId, false)
        }

        if (id != -1) {
            setCheckedStateForView(id, true)
        }

        setCheckedId(id)
    }

    private fun setCheckedId(id: Int) {
        checkedId = id
        onCheckedChangeListener?.onCheckedChanged(this, checkedId)
    }

    private fun setCheckedStateForView(viewId: Int, checked: Boolean) {
        findViewById<View>(viewId)?.isSelected = checked
    }

    private fun clearCheck() {
        check(-1)
    }

    fun getCheckedRadioButtonId(): Int = checkedId

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener) {
        onCheckedChangeListener = listener
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(group: FlowRadioGroup, checkedId: Int)
    }
}