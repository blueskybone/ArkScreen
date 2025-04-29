package com.blueskybone.arkscreen.task.screenshot

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.updateMargins
import androidx.drawerlayout.widget.DrawerLayout
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.FlowLayout
import com.blueskybone.arkscreen.task.recruit.RecruitManager
import com.blueskybone.arkscreen.util.getRealScreenSize
import java.util.concurrent.atomic.AtomicInteger

/**
 *   Created by blueskybone
 *   Date: 2025/1/25
 */
@SuppressLint("DiscouragedApi", "ClickableViewAccessibility", "InternalInsetResource")
class FloatWindow(private val context: Context) {

    companion object {
        private var isAdd = false
        private var tagViewGroup: FlowLayout? = null
        private var statusBarHeight = 0

    }

    private lateinit var rarityValValues: List<String>
    private lateinit var rarityColorIds: List<Int>

    fun initialize() {
        val rarityValues = context.resources.getStringArray(R.array.rarity_value)
        val rarityDrawable: TypedArray = context.resources.obtainTypedArray(R.array.rarity_draw)

        rarityValValues = rarityValues.toList()
        val list = ArrayList<Int>()
        for (idx in 0..rarityValValues.size) {
            list.add(rarityDrawable.getResourceId(idx, 1))
        }
        rarityColorIds = list
        rarityDrawable.recycle()
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
    private val touchLayout = inflater.inflate(R.layout.float_recruit, floatingView)

    init {
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

        //拖动监听
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
            val tagView = TextView(context)
            tagView.setTagLayout(tag)
            tagViewGroup?.addView(tagView)
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
            for (tag in result.tags) {
                val tagView = TextView(context)
                tagView.setTagLayout(tag)
                tagGroup.addView(tagView)
            }
            val opeGroup = TagViewGroup(context, null)
            for (ope in result.operators) {
                val opeView = TextView(context)
                opeView.setOpeLayout(ope.name, ope.rare)
                opeGroup.addView(opeView)
            }
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
        playFloatUpAnimation()
    }

    fun close() {
        if (isAdd) {
            windowManager.removeView(floatingView)
            isAdd = !isAdd
        }
    }

    private fun createLine(context: Context): View {
        val line = View(context)
        val params = ViewGroup.MarginLayoutParams(DrawerLayout.LayoutParams.MATCH_PARENT, 5)
        params.updateMargins(0, 15, 0, 15)
        line.layoutParams = params
        line.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_500))
        return line
    }

    private fun createSpace(context: Context): Space {
        val space = Space(context)
        val spaceLayoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            10
        )
        space.layoutParams = spaceLayoutParams
        return space
    }

    private fun playFloatUpAnimation() {
        val startY = params.y  + dpToPx(10) // Start from below screen
        val endY = params.y // Float up 150dp

        ValueAnimator.ofInt(startY, endY).apply {
            duration = 200 // Animation duration 300ms
            interpolator = AccelerateDecelerateInterpolator() // Smooth easing
            addUpdateListener { animation ->
                params.y = animation.animatedValue as Int
                windowManager.updateViewLayout(floatingView, params)
            }
            start()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun onCloseButtonClick() {
        close()
    }

    private fun TextView.setTagLayout(text: String) {
        this.setPadding(4, 2, 4, 2)
        val shapeDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 2f
            setColor(ContextCompat.getColor(context, R.color.blue_500))
        }
        this.background = shapeDrawable
        this.gravity = Gravity.CENTER
        this.text = text
        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        this.setTextColor(ContextCompat.getColor(context, R.color.white))
        this.layoutParams = DrawerLayout.LayoutParams(
            DrawerLayout.LayoutParams.WRAP_CONTENT,
            DrawerLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun TextView.setOpeLayout(text: String, rarity: Int) {
        this.setPadding(4, 2, 4, 2)
        val rarityIdx = rarityValValues.indexOf(rarity.toString())
        val colorId = rarityColorIds[rarityIdx]
        val shapeDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 2f
            setColor(context.resources.getColor(colorId, null))
        }
        this.background = shapeDrawable
        this.gravity = Gravity.CENTER
        this.text = text
        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        this.setTextColor(ContextCompat.getColor(context, R.color.white))
        this.layoutParams = DrawerLayout.LayoutParams(
            DrawerLayout.LayoutParams.WRAP_CONTENT,
            DrawerLayout.LayoutParams.WRAP_CONTENT
        )
    }
}