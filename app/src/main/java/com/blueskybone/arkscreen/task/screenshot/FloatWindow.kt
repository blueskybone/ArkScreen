package com.blueskybone.arkscreen.task.screenshot

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.createLine
import com.blueskybone.arkscreen.common.createSpace
import com.blueskybone.arkscreen.common.opeTextView
import com.blueskybone.arkscreen.common.tagTextView
import com.blueskybone.arkscreen.task.recruit.RecruitManager
import com.blueskybone.arkscreen.common.TagViewGroup
import com.blueskybone.arkscreen.util.getRealScreenSize
import java.util.concurrent.atomic.AtomicInteger

/**
 *   Created by blueskybone
 *   Date: 2024/7/26
 */
@SuppressLint(
    "ServiceCast", "MissingInflatedId", "ClickableViewAccessibility", "DiscouragedApi",
    "InternalInsetResource"
)
class FloatingWindow(context: Context) {

    companion object {
        private var isAdd = false
        private var tagViewGroup: TagViewGroup? = null
        private var statusBarHeight = 0
    }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val floatingView = LinearLayout(context)
    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    )
    private val inflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val touchLayout = inflater.inflate(R.layout.recruit_float, floatingView)

    init {
//        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val touchLayout = inflater.inflate(R.layout.recruit_float, floatingView)
        tagViewGroup = touchLayout.findViewById(R.id.tag_view_group)
        val textCloseButton = touchLayout.findViewById<TextView>(R.id.text_button_close)
        params.gravity = Gravity.START
        textCloseButton.setOnClickListener { _ ->
            run {
                onCloseButtonClick()
            }
        }
        val resourceId: Int =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
        }
        val lastX = AtomicInteger()
        val lastY = AtomicInteger()
        touchLayout.setOnTouchListener { _, event ->
            val action = event.action
            val mWidth: Int = getRealScreenSize(context).x
            val mHeight: Int = getRealScreenSize(context).y
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX.set(event.x.toInt())
                    lastY.set(event.y.toInt())
                }
                //TODO：旋转后位置计算不正确（Y）
                MotionEvent.ACTION_MOVE -> if (mWidth > mHeight) {
                    params.x = event.rawX.toInt() - lastX.get() - statusBarHeight
                    params.y = event.rawY.toInt() - lastY.get() - statusBarHeight * 2
                    windowManager.updateViewLayout(touchLayout, params)
                } else {
                    params.x = event.rawX.toInt() - lastX.get()
                    params.y = event.rawY.toInt()
                    windowManager.updateViewLayout(touchLayout, params)
                }
            }
            false
        }


    }

    @SuppressLint("SetTextI18n")
    private fun setView(
        context: Context,
        tags: List<String>,
        resultList: List<RecruitManager.RecruitResult>
    ) {
        tagViewGroup?.removeAllViews()
        for (tag in tags) {
            tagViewGroup?.addView(tagTextView(context, tag))
        }
        val linearView = touchLayout.findViewById<LinearLayout>(R.id.linear_result)
        val rareText = touchLayout.findViewById<TextView>(R.id.result_rare)
        linearView.removeAllViews()

        if (resultList.isEmpty()) {
            rareText.text = "无4星以上组合"
            return
        }
        val rare = resultList[0].rare
        rareText.text = "可锁$rare★"

        for (result in resultList) {
            val tagGroup = TagViewGroup(context, null)
            for (tag in result.tags) tagGroup.addView(tagTextView(context, tag))
            val opeGroup = TagViewGroup(context, null)
            for (ope in result.operators) opeGroup.addView(opeTextView(context, ope))

            linearView.addView(tagGroup)
            linearView.addView(createSpace(context))
            linearView.addView(opeGroup)
            linearView.addView(createLine(context))
        }
    }



    fun openAndUpdateWindow(
        context: Context,
        tags: List<String>,
        resultList: List<RecruitManager.RecruitResult>
    ) {
        setView(context, tags, resultList)
        if (!isAdd) {
            windowManager.addView(floatingView, params)
            isAdd = !isAdd
        }
    }

    private fun close() {
        if (isAdd) {
            windowManager.removeView(floatingView)
            isAdd = !isAdd
        }
    }

    private fun onCloseButtonClick() {
        close()
    }
}