package com.blueskybone.arkscreen.ui.recruit.screenshot

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Application
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
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
import com.blueskybone.arkscreen.domain.model.recruit.RecruitResult
import com.blueskybone.arkscreen.ui.common.view.FlowLayout
import com.blueskybone.arkscreen.util.getRealScreenSize
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("ClickableViewAccessibility", "InternalInsetResource")
class FloatWindowController(
    private val application: Application,
) {

    private val context = application.applicationContext

    private val windowManager =
        context.getSystemService(WindowManager::class.java)

    private val inflater =
        LayoutInflater.from(context)

    private val floatingRoot = LinearLayout(context)

    private val contentView: View =
        inflater.inflate(R.layout.float_recruit, floatingRoot)

    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.START
    }

    private var isAdded = false
    private var statusBarHeight = 0

    private val tagViewGroup: FlowLayout =
        contentView.findViewById(R.id.tag_view_group)

    private val resultContainer: LinearLayout =
        contentView.findViewById(R.id.linear_result)

    private val rareText: TextView =
        contentView.findViewById(R.id.result_rare)

    private val rarityColorIds = intArrayOf(
        R.color.rare_1,
        R.color.rare_2,
        R.color.rare_3,
        R.color.rare_4,
        R.color.rare_5,
        R.color.rare_6,
    )

    init {
        setupCloseButton()
        setupStatusBarHeight()
        setupDrag()
    }

    fun showResult(
        tags: List<String>,
        results: List<RecruitResult>,
    ) {
        render(tags, results)

        if (!isAdded) {
            windowManager.addView(contentView, params)
            isAdded = true
        } else {
            windowManager.updateViewLayout(contentView, params)
        }

        playFloatUpAnimation()
    }

    fun close() {
        if (isAdded) {
            windowManager.removeView(contentView)
            isAdded = false
        }
    }

    private fun render(
        tags: List<String>,
        results: List<RecruitResult>,
    ) {
        renderTags(tags)
        renderResults(results)
    }

    private fun renderTags(tags: List<String>) {
        tagViewGroup.removeAllViews()

        tags.forEach { tag ->
            tagViewGroup.addView(
                createTagTextView(tag)
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderResults(results: List<RecruitResult>) {
        resultContainer.removeAllViews()

        if (results.isEmpty()) {
            rareText.setText(R.string.recruit_no_high_rarity_result)
            return
        }

        val maxRarity = results.maxOf { it.rare }
        rareText.text = context.getString(R.string.recruit_guaranteed_rarity, maxRarity)

        results.forEach { result ->
            val tagGroup = FlowLayout(context, null)
            result.tags.forEach { tag ->
                tagGroup.addView(createTagTextView(tag))
            }

            val operatorGroup = FlowLayout(context, null)
            result.operators.forEach { operator ->
                operatorGroup.addView(
                    createOperatorTextView(
                        text = operator.name,
                        rarity = operator.rare,
                    )
                )
            }

            resultContainer.addView(tagGroup)
            resultContainer.addView(createSpace())
            resultContainer.addView(operatorGroup)
            resultContainer.addView(createLine())
        }
    }

    private fun setupCloseButton() {
        contentView
            .findViewById<TextView>(R.id.text_button_close)
            .setOnClickListener {
                close()
            }
    }

    private fun setupStatusBarHeight() {
        val resourceId =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")

        if (resourceId > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
        }
    }

    private fun setupDrag() {
        val lastX = AtomicInteger()
        val lastY = AtomicInteger()

        contentView.setOnTouchListener { _, event ->
            val screenSize = getRealScreenSize(context)
            val screenWidth = screenSize.x
            val screenHeight = screenSize.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX.set(event.x.toInt())
                    lastY.set(event.y.toInt())
                }

                MotionEvent.ACTION_MOVE -> {
                    if (screenWidth > screenHeight) {
                        params.x = event.rawX.toInt() - lastX.get() - statusBarHeight
                        params.y = event.rawY.toInt() - lastY.get() - statusBarHeight * 2
                    } else {
                        params.x = event.rawX.toInt() - lastX.get()
                        params.y = event.rawY.toInt()
                    }

                    if (isAdded) {
                        windowManager.updateViewLayout(contentView, params)
                    }
                }
            }

            false
        }
    }

    private fun createTagTextView(text: String): TextView {
        return TextView(context).apply {
            setTagLayout(text)
        }
    }

    private fun createOperatorTextView(
        text: String,
        rarity: Int,
    ): TextView {
        return TextView(context).apply {
            setOperatorLayout(text, rarity)
        }
    }

    private fun createLine(): View {
        return View(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                DrawerLayout.LayoutParams.MATCH_PARENT,
                5
            ).apply {
                updateMargins(0, 15, 0, 15)
            }

            setBackgroundColor(
                ContextCompat.getColor(context, R.color.grey_500)
            )
        }
    }

    private fun createSpace(): Space {
        return Space(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                10
            )
        }
    }

    private fun playFloatUpAnimation() {
        if (!isAdded) return

        val endY = params.y
        val startY = endY + dpToPx(10)

        ValueAnimator.ofInt(startY, endY).apply {
            duration = 200L
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                params.y = animation.animatedValue as Int
                if (isAdded) {
                    windowManager.updateViewLayout(contentView, params)
                }
            }
            start()
        }
    }

    private fun TextView.setTagLayout(textValue: String) {
        setPadding(4, 2, 4, 2)
        background = createRoundedBackground(R.color.blue_500)
        gravity = Gravity.CENTER
        text = textValue
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setTextColor(ContextCompat.getColor(context, R.color.white))
        layoutParams = DrawerLayout.LayoutParams(
            DrawerLayout.LayoutParams.WRAP_CONTENT,
            DrawerLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun TextView.setOperatorLayout(
        textValue: String,
        rarity: Int,
    ) {
        setPadding(4, 2, 4, 2)
        background = createRoundedBackground(getRarityColorId(rarity))
        gravity = Gravity.CENTER
        text = textValue
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setTextColor(ContextCompat.getColor(context, R.color.white))
        layoutParams = DrawerLayout.LayoutParams(
            DrawerLayout.LayoutParams.WRAP_CONTENT,
            DrawerLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun createRoundedBackground(colorResId: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 2f
            setColor(ContextCompat.getColor(context, colorResId))
        }
    }

    private fun getRarityColorId(rarity: Int): Int {
        return rarityColorIds.getOrElse(rarity - 1) { R.color.grey_500 }
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
