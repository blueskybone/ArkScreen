package com.blueskybone.arkscreen.ui.recruit.floating

import com.blueskybone.arkscreen.domain.model.settings.AppThemeSetting
import android.animation.ValueAnimator
import android.annotation.SuppressLint
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

@SuppressLint("ClickableViewAccessibility", "InternalInsetResource")
class FloatWindowController(
    private val application: android.content.Context,
) {

    private fun themedContext(): android.content.Context {
        val configuration = android.content.res.Configuration(application.resources.configuration)
        val settings = org.koin.mp.KoinPlatform.getKoin().get<com.blueskybone.arkscreen.data.local.pref.SettingPrefManager>()
        val night = when (settings.appTheme.get()) {
            AppThemeSetting.LIGHT -> android.content.res.Configuration.UI_MODE_NIGHT_NO
            AppThemeSetting.DARK -> android.content.res.Configuration.UI_MODE_NIGHT_YES
            else -> application.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        }
        configuration.uiMode = (configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK.inv()) or night
        return androidx.appcompat.view.ContextThemeWrapper(application.createConfigurationContext(configuration), R.style.AppTheme)
    }

    private var context = themedContext()
    private val windowManager = application.getSystemService(WindowManager::class.java)
    private var contentView: View = LayoutInflater.from(context).inflate(R.layout.float_recruit, null)
    private var entranceAnimator: ValueAnimator? = null
    private var hasPosition = false

    private fun refreshTheme() {
        val updated = themedContext()
        if (updated.resources.configuration == context.resources.configuration) return
        close()
        context = updated
        contentView = LayoutInflater.from(context).inflate(R.layout.float_recruit, null)
        setupCloseButton()
    }

    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
    }

    private val settings: com.blueskybone.arkscreen.data.local.pref.SettingPrefManager = org.koin.mp.KoinPlatform.getKoin().get()
    private var appearance = FloatingAppearance.read(settings)
    private var lastTags: List<String> = emptyList()
    private var lastResults: List<RecruitResult> = emptyList()
    private var isAdded = false
    fun refreshAppearance() { if (isAdded) showResult(lastTags, lastResults) }


    fun preview(tags: List<String>, results: List<RecruitResult>, style: FloatingAppearance): View {
        refreshTheme()
        appearance = style
        render(tags, results)
        contentView.findViewById<View>(R.id.text_button_close).isEnabled = false
        return contentView
    }


    private val tagViewGroup: FlowLayout get() =
        contentView.findViewById(R.id.tag_view_group)

    private val resultContainer: LinearLayout get() =
        contentView.findViewById(R.id.linear_result)

    private val rareText: TextView get() =
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
        // Preview uses the same content without overlay drag behavior.
    }

    fun showResult(
        tags: List<String>,
        results: List<RecruitResult>,
    ) {
        entranceAnimator?.cancel()
        refreshTheme()
        setupDrag()
        lastTags = tags; lastResults = results
        appearance = FloatingAppearance.read(settings)
        render(tags, results)
        val screen = getRealScreenSize(context)
        params.width = dpToPx(appearance.width).coerceAtMost(screen.x)
        params.height = dpToPx(appearance.height).coerceAtMost(screen.y)
        if (!hasPosition) {
            params.y = dpToPx(48)
            hasPosition = true
        }
        params.x = params.x.coerceIn(0, (screen.x - params.width).coerceAtLeast(0))
        params.y = params.y.coerceIn(0, (screen.y - params.height).coerceAtLeast(0))
        if (!isAdded) {
            windowManager.addView(contentView, params)
            isAdded = true
        } else {
            windowManager.updateViewLayout(contentView, params)
        }

        playFloatUpAnimation()
    }

    fun close() {
        entranceAnimator?.cancel()
        entranceAnimator = null
        if (isAdded) {
            windowManager.removeView(contentView)
            isAdded = false
        }
    }

    private fun render(
        tags: List<String>,
        results: List<RecruitResult>,
    ) {
        rareText.textSize = 14f * appearance.font / 100f
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
            .findViewById<View>(R.id.text_button_close)
            .setOnClickListener {
                close()
            }
    }

    private fun setupDrag() {
        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0
        contentView.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    entranceAnimator?.cancel()
                    downX = event.rawX
                    downY = event.rawY
                    startX = params.x
                    startY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Raw-coordinate deltas preserve the window origin, including system insets.
                    params.x = startX + (event.rawX - downX).toInt()
                    params.y = startY + (event.rawY - downY).toInt()
                    if (isAdded) windowManager.updateViewLayout(contentView, params)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> true
                else -> false
            }
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

        entranceAnimator?.cancel()
        entranceAnimator = ValueAnimator.ofInt(startY, endY).apply {
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
        if (appearance.colorful) setPadding(dpToPx(8), dpToPx(5), dpToPx(8), dpToPx(5))
        else setPadding(4, 2, 4, 2)
        background = createRoundedBackground(if (appearance.colorful) R.color.recruit_tag_surface else R.color.blue_500)
        gravity = Gravity.CENTER
        text = textValue
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f * appearance.font / 100f)
        setTextColor(ContextCompat.getColor(context, if (appearance.colorful) R.color.home_text_primary else R.color.white))
        layoutParams = DrawerLayout.LayoutParams(
            DrawerLayout.LayoutParams.WRAP_CONTENT,
            DrawerLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun TextView.setOperatorLayout(
        textValue: String,
        rarity: Int,
    ) {
        if (appearance.colorful) setPadding(dpToPx(8), dpToPx(5), dpToPx(8), dpToPx(5))
        else setPadding(4, 2, 4, 2)
        background = createRoundedBackground(if (appearance.colorful) raritySurface(rarity) else getRarityColorId(rarity))
        gravity = Gravity.CENTER
        text = textValue
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f * appearance.font / 100f)
        setTextColor(ContextCompat.getColor(context, if (appearance.colorful) rarityText(rarity) else R.color.white))
        layoutParams = DrawerLayout.LayoutParams(
            DrawerLayout.LayoutParams.WRAP_CONTENT,
            DrawerLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun createRoundedBackground(colorResId: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = if (appearance.colorful) dpToPx(14).toFloat() else 2f
            setColor(ContextCompat.getColor(context, colorResId))
        }
    }

    private fun raritySurface(r: Int) = when (r) {
        6 -> R.color.recruit_rare_6_surface; 5 -> R.color.recruit_rare_5_surface
        4 -> R.color.recruit_rare_4_surface; 3 -> R.color.recruit_rare_3_surface
        else -> R.color.recruit_rare_1_surface
    }
    private fun rarityText(r: Int) = when (r) {
        6 -> R.color.recruit_rare_6_text; 5 -> R.color.recruit_rare_5_text
        4 -> R.color.recruit_rare_4_text; 3 -> R.color.recruit_rare_3_text
        else -> R.color.recruit_rare_1_text
    }
    private fun getRarityColorId(rarity: Int): Int {
        return rarityColorIds.getOrElse(rarity - 1) { R.color.grey_500 }
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
