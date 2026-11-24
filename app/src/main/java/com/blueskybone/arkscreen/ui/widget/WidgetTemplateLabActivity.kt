package com.blueskybone.arkscreen.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.blueskybone.arkscreen.ui.common.showChoicePopup
import androidx.lifecycle.lifecycleScope
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.WidgetTemplatePrefManager
import com.blueskybone.arkscreen.data.local.room.WidgetConfig
import com.blueskybone.arkscreen.data.local.room.dao.WidgetConfigDao
import com.blueskybone.arkscreen.databinding.ActivityWidgetTemplateLabBinding
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.platform.widget.WidgetUpdateDispatcher
import com.blueskybone.arkscreen.ui.widget.model.WidgetVisualStyle
import com.blueskybone.arkscreen.ui.widget.model.WidgetBackgroundSize
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.hjq.toast.Toaster
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.getKoin

/** One editor for launcher configuration and in-app per-widget appearance. */
class WidgetTemplateLabActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWidgetTemplateLabBinding
    private val configs: WidgetConfigDao by inject()
    private val prefs: WidgetTemplatePrefManager by inject()
    private val accounts: AccountRepository by inject()
    private val manager by lazy { AppWidgetManager.getInstance(this) }
    private var draft: WidgetConfig? = null
    private var original: WidgetConfig? = null
    private var widgetIds = emptyList<Int>()
    private var previewJob: Job? = null
    private var previewHost: FrameLayout? = null
    private var requestedId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var restored: Bundle? = null
    private var loadingConfig = false
    private var choicePopup: androidx.appcompat.widget.ListPopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetTemplateLabBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setResult(RESULT_CANCELED)
        requestedId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        restored = savedInstanceState
        binding.Toolbar.setNavigationOnClickListener { leave() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = leave()
        })
        binding.SaveAppearance.setOnClickListener { save() }
        refreshWidgets(savedInstanceState?.getInt("widgetId") ?: requestedId)
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized && !loadingConfig) {
            if (draft == null || draft?.appWidgetId?.let(::valid) == false) {
                draft = null
                refreshWidgets(requestedId)
            } else {
                widgetIds = (WidgetUpdateDispatcher(this).widgetIds() + listOfNotNull(draft?.appWidgetId)).distinct()
            }
        }
    }

    private fun valid(id: Int) = manager.getAppWidgetInfo(id)?.provider?.let {
        it.packageName == packageName && WidgetUpdateDispatcher.PROVIDERS.any { p -> p.name == it.className }
    } == true

    private fun refreshWidgets(preferred: Int) {
        widgetIds = (WidgetUpdateDispatcher(this).widgetIds() + listOf(preferred).filter(::valid)).distinct()
        val id = preferred.takeIf { it in widgetIds } ?: widgetIds.firstOrNull()
        if (id == null) showEmpty() else load(id)
    }

    private fun load(id: Int) {
        loadingConfig = true
        previewJob?.cancel()
        lifecycleScope.launch {
            val config = (configs.get(id) ?: WidgetConfig(id)).withAppearance(prefs)
            original = config
            draft = restored?.takeIf { it.getInt("widgetId") == id }?.let { state ->
                config.copy(
                    theme = state.getString("theme", config.theme),
                    layout1x2 = state.getString("layout1x2", config.layout1x2),
                    layout2x3 = state.getString("layout2x3", config.layout2x3),
                    slot1x1 = state.getString("slot1x1", config.slot1x1),
                    slot1x2 = state.getString("slot1x2", config.slot1x2),
                    slots2x2 = state.getString("slots2x2", config.slots2x2),
                    slots2x3 = state.getString("slots2x3", config.slots2x3),
                    fontScale = state.getFloat("fontScale", config.fontScale),
                    transparency = state.getInt("transparency", config.transparency),
                    accountMode = state.getString("accountMode", config.accountMode)!!,
                    roleUid = state.getString("roleUid"), serverKey = state.getString("serverKey"),
                )
            } ?: config
            restored = null
            loadingConfig = false
            buildEditor()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        draft?.let {
            outState.putInt("widgetId", it.appWidgetId)
            outState.putString("theme", it.theme)
            outState.putString("layout1x2", it.layout1x2)
            outState.putString("layout2x3", it.layout2x3)
            outState.putString("slot1x1", it.slot1x1)
            outState.putString("slot1x2", it.slot1x2)
            outState.putString("slots2x2", it.slots2x2)
            outState.putString("slots2x3", it.slots2x3)
            outState.putFloat("fontScale", it.fontScale)
            outState.putInt("transparency", it.transparency)
            outState.putString("accountMode", it.accountMode)
            outState.putString("roleUid", it.roleUid)
            outState.putString("serverKey", it.serverKey)
        }
        super.onSaveInstanceState(outState)
    }

    private fun confirmDiscard(action: () -> Unit) {
        if (draft == original) action() else MaterialAlertDialogBuilder(this)
            .setTitle("放弃未保存的修改？").setMessage("当前组件的外观和账号尚未保存。")
            .setNegativeButton("继续编辑", null).setPositiveButton("放弃") { _, _ -> action() }.show()
    }

    private fun leave() = confirmDiscard { finish() }

    private fun save() {
        val config = draft ?: return
        if (!valid(config.appWidgetId)) {
            Toaster.show("该组件已移除")
            draft = null
            refreshWidgets(AppWidgetManager.INVALID_APPWIDGET_ID)
            return
        }
        binding.SaveAppearance.isEnabled = false
        lifecycleScope.launch {
            try {
                configs.put(config)
                original = config
                WidgetUpdateDispatcher(this@WidgetTemplateLabActivity).render(config.appWidgetId)
                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, config.appWidgetId))
                Toaster.show("已保存组件外观")
                if (requestedId != AppWidgetManager.INVALID_APPWIDGET_ID) finish()
            } catch (e: Exception) {
                timber.log.Timber.e(e, "Widget appearance save failed")
                Toaster.show("保存失败，请重试")
            } finally { binding.SaveAppearance.isEnabled = true }
        }
    }

    private fun showEmpty() {
        binding.EditorContent.removeAllViews()
        binding.SaveAppearance.visibility = View.GONE
        header("桌面组件")
        val box = card()
        box.addView(text("先在桌面添加一个组件，再为它设置账号和外观。", 15f).apply { setPadding(dp(20), dp(24), dp(20), dp(24)) })
        row(box, "添加桌面组件", "选择规格") { anchor ->
            val choices = arrayOf("1×1", "1×2", "2×2", "2×3")
            popup(anchor, choices.toList(), -1) { which ->
                if (manager.isRequestPinAppWidgetSupported) {
                    manager.requestPinAppWidget(ComponentName(this, WidgetUpdateDispatcher.PROVIDERS[which]), null, null)
                } else Toaster.show("请长按桌面，在桌面组件中添加公招助手组件")
            }
        }
    }

    private fun kind(id: Int): Int = WidgetUpdateDispatcher.PROVIDERS.indexOfFirst {
        it.name == manager.getAppWidgetInfo(id)?.provider?.className
    }.coerceAtLeast(0)

    private fun sizeName(id: Int) = listOf("1×1", "1×2", "2×2", "2×3")[kind(id)]
    private fun provider(id: Int): BaseNextWidgetProvider = when (kind(id)) {
        0 -> WidgetNext1(); 1 -> WidgetNext2(); 2 -> WidgetNext3(); else -> WidgetNext4()
    }
    private fun backgroundSize(id: Int) = WidgetBackgroundSize.entries[kind(id)]
    private suspend fun accountName(c: WidgetConfig): String =
        if (c.accountMode == WidgetConfig.FOLLOW_CURRENT) "跟随当前账号" else
            accounts.observeSkAcc().first().firstOrNull { it.uid == c.roleUid && it.channelMasterId == c.serverKey }
                ?.let { it.nickName + " · " + if (it.official) "官服" else "B服" } ?: "账号已删除"

    private fun buildEditor() {
        val config = draft ?: return
        binding.EditorContent.removeAllViews()
        binding.SaveAppearance.visibility = View.VISIBLE
        header("当前组件")
        val identity = card()
        row(identity, "组件", sizeName(config.appWidgetId) + " · #" + config.appWidgetId) { anchor ->
            if (requestedId != AppWidgetManager.INVALID_APPWIDGET_ID || widgetIds.size <= 1) return@row
            popup(anchor, widgetIds.map { sizeName(it) + " · #" + it }, widgetIds.indexOf(config.appWidgetId)) { index ->
                if (widgetIds[index] != config.appWidgetId) confirmDiscard { load(widgetIds[index]) }
            }
        }
        divider(identity)
        val accountRow = row(identity, "账号", "…") { chooseAccount(it) }
        lifecycleScope.launch { accountRow.text = accountName(config) }

        header("预览", "点击保存后应用到桌面组件")
        val previewCard = card()
        (previewCard.parent as MaterialCardView).setCardBackgroundColor(color(R.color.grey_700))
        previewHost = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, -2)
        }
        previewCard.addView(previewHost)

        header("主题")
        val themes = card()
        val group = layoutInflater.inflate(R.layout.widget_theme_choices, themes, false) as com.google.android.material.chip.ChipGroup
        WidgetVisualStyle.entries.forEachIndexed { index, style ->
            val chip = group.getChildAt(index) as com.google.android.material.chip.Chip
            chip.isChecked = config.theme == style.key
            chip.setOnClickListener {
                draft = draft?.copy(theme = style.key)
                renderPreview()
            }
        }
        themes.addView(group)

        val k = kind(config.appWidgetId)
        if (k == 1 || k == 3) {
            header("布局")
            val layouts = card()
            val keys = if (k == 1) listOf("single", "dense") else listOf("four", "full")
            val labels = if (k == 1) listOf("单信息", "紧凑布局") else listOf("四项信息", "完整信息")
            val selected = keys.indexOf(if (k == 1) config.layout1x2 else config.layout2x3).coerceAtLeast(0)
            row(layouts, "布局", labels[selected]) { anchor ->
                popup(anchor, labels, selected) { i ->
                    draft = if (k == 1) draft?.copy(layout1x2 = keys[i]) else draft?.copy(layout2x3 = keys[i])
                    buildEditor()
                }
            }
        }

        header("字体大小")
        sliderCard("Aa", "按比例调整组件内的文字", 85f, 115f, config.fontScale * 100, 5f) {
            draft = draft?.copy(fontScale = it / 100); renderPreview()
        }
        header("背景透明度")
        sliderCard("背景", "仅调整背景，文字和图标保持清晰", 0f, 100f, config.transparency.toFloat(), 5f) {
            draft = draft?.copy(transparency = it.toInt()); renderPreview()
        }
        header("显示内容")
        val contents = card()
        val fixed = k == 1 && config.layout1x2 == "dense" || k == 3 && config.layout2x3 == "full"
        if (fixed) {
            contents.addView(text("当前布局使用固定的数据项", 14f).apply { setPadding(dp(20), dp(20), dp(20), dp(20)) })
        } else {
            val slots = slotNames(config)
            slots.forEachIndexed { i, type ->
                if (i > 0) divider(contents)
                row(contents, "位置 " + (i + 1), typeLabel(type)) { chooseContent(it, i) }
            }
        }
        renderPreview()
    }

    private fun chooseAccount(anchor: View) {
        lifecycleScope.launch {
            val config = draft ?: return@launch
            val all = accounts.observeSkAcc().first()
            val labels = listOf("跟随当前账号") + all.map { it.nickName + " · " + if (it.official) "官服" else "B服" }
            val selected = if (config.accountMode == WidgetConfig.FOLLOW_CURRENT) 0 else
                all.indexOfFirst { it.uid == config.roleUid && it.channelMasterId == config.serverKey }.let { if (it < 0) -1 else it + 1 }
            popup(anchor, labels, selected) { index ->
                    val account = all.getOrNull(index - 1)
                    draft = draft?.copy(accountMode = if (index == 0) WidgetConfig.FOLLOW_CURRENT else WidgetConfig.FIXED,
                        roleUid = account?.uid, serverKey = account?.channelMasterId)
                    buildEditor()
                }
        }
    }

    private val types = listOf("SANITY", "DRONE", "RECRUITMENT", "RECRUITMENT_REFRESH", "TRAINING", "MEETING")
    private fun typeLabel(type: String) = listOf("理智", "无人机", "公开招募", "公招刷新", "训练室", "会客室")
        .getOrElse(types.indexOf(type)) { type }
    private fun slotNames(c: WidgetConfig): List<String> {
        val defaults = when (kind(c.appWidgetId)) {
            0, 1 -> listOf("SANITY")
            2 -> listOf("SANITY", "TRAINING")
            else -> listOf("SANITY", "DRONE", "RECRUITMENT", "TRAINING")
        }
        val raw = when (kind(c.appWidgetId)) { 0 -> c.slot1x1; 1 -> c.slot1x2; 2 -> c.slots2x2; else -> c.slots2x3 }
        return raw.orEmpty().split(",").filter { it in types }.distinct().takeIf { it.size == defaults.size } ?: defaults
    }
    private fun chooseContent(anchor: View, index: Int) {
        val c = draft ?: return
        val slots = slotNames(c)
        val candidates = types.filter { it == slots[index] || it !in slots }
        popup(anchor, candidates.map(::typeLabel), candidates.indexOf(slots[index])) { which ->
                val value = slots.toMutableList().apply { set(index, candidates[which]) }.joinToString(",")
                draft = when (kind(c.appWidgetId)) {
                    0 -> c.copy(slot1x1 = value); 1 -> c.copy(slot1x2 = value)
                    2 -> c.copy(slots2x2 = value); else -> c.copy(slots2x3 = value)
                }
                buildEditor()
            }
    }

    private fun renderPreview() {
        val config = draft ?: return
        val host = previewHost ?: return
        previewJob?.cancel()
        previewJob = lifecycleScope.launch {
            try {
                val snapshots: com.blueskybone.arkscreen.data.local.cache.RealtimeCacheStore = getKoin().get()
                val settings: com.blueskybone.arkscreen.data.local.pref.SettingPrefManager = getKoin().get()
                val clock: com.blueskybone.arkscreen.domain.service.AppClock = getKoin().get()
                val attendanceStates: com.blueskybone.arkscreen.domain.repository.AttendanceStateRepository = getKoin().get()
                val account = com.blueskybone.arkscreen.platform.widget.WidgetAccountResolver.resolve(
                    config, accounts.observeSkAcc().first(), accounts.observeCurrentSkAcc().first())
                val cache = account?.let { snapshots.read(it) }
                    ?: com.blueskybone.arkscreen.domain.model.cache.RealtimeCacheSnapshot()
                val attendance = account?.let { attendanceStates.get(com.blueskybone.arkscreen.domain.model.account.AccountType.SK, it.uid) }
                val widget = WidgetPreviewRenderer(this@WidgetTemplateLabActivity, config,
                    kind(config.appWidgetId), cache, settings, clock, attendance).render()
                host.removeAllViews()
                host.addView(widget)

            } catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) {
                timber.log.Timber.e(e, "Widget appearance preview failed")
                host.removeAllViews()
                val cause = generateSequence(e as Throwable) { it.cause }.last()
                host.addView(text("预览加载失败：" + cause.javaClass.simpleName + "\n" +
                    cause.message.orEmpty().take(180), 12f))
            }
        }
    }

    private fun sliderCard(label: String, hint: String, from: Float, to: Float, value: Float, step: Float, changed: (Float) -> Unit) {
        val box = card()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(16), dp(20), dp(12))
        }
        box.addView(content)
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        top.addView(text(label, 20f), LinearLayout.LayoutParams(0, -2, 1f))
        val percent = TextView(this, null, 0, R.style.Widget_ArkScreen_CompactCapsule).apply {
            text = value.toInt().toString() + "%"
        }
        top.addView(percent)
        content.addView(top)
        content.addView(text(hint, 13f).apply { setTextColor(color(R.color.home_text_secondary)); setPadding(0, dp(8), 0, dp(10)) })
        content.addView(Slider(this).apply {
            valueFrom = from; valueTo = to; stepSize = step
            this.value = (kotlin.math.round(value / step) * step).coerceIn(from, to)
            trackHeight = dp(12)
            thumbRadius = dp(16)
            setCustomThumbDrawable(GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(3).toFloat()
                setColor(accent())
                setSize(dp(6), dp(32))
            })
            setLabelFormatter { it.toInt().toString() + "%" }
            addOnChangeListener { _, v, user ->
                if (user) { percent.text = v.toInt().toString() + "%"; changed(v) }
            }
        }, LinearLayout.LayoutParams(-1, dp(48)))
    }

    private fun popup(anchor: View, entries: List<String>, selected: Int, onSelect: (Int) -> Unit) {
        choicePopup?.dismiss()
        choicePopup = showChoicePopup(anchor, entries, selected, onSelect)
    }

    override fun onDestroy() {
        choicePopup?.dismiss()
        super.onDestroy()
    }

    private fun header(title: String, hint: String? = null) {
        val line = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        line.addView(text(title, 16f).apply {
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(color(R.color.function_section_title))
        })
        if (hint != null) line.addView(text(hint, 11f).apply {
            setTextColor(color(R.color.home_text_secondary))
        }, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10) })
        binding.EditorContent.addView(line, LinearLayout.LayoutParams(-1, dp(44)).apply {
            topMargin = dp(16); marginStart = dp(32); marginEnd = dp(32)
        })
    }
    private fun card(): LinearLayout {
        val outer = MaterialCardView(this).apply {
            radius = dp(24).toFloat(); cardElevation = 0f; strokeWidth = 0; clipToOutline = true
            setCardBackgroundColor(color(R.color.app_surface))
        }
        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        outer.addView(inner)
        binding.EditorContent.addView(outer, LinearLayout.LayoutParams(-1, -2).apply {
            marginStart = dp(16); marginEnd = dp(16)
        })
        return inner
    }
    private fun row(box: LinearLayout, title: String, value: String, click: (View) -> Unit): TextView {
        val line = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL; minimumHeight = dp(56)
            setPadding(dp(20), dp(8), dp(20), dp(8))
        }
        line.addView(text(title, 16f), LinearLayout.LayoutParams(0, -2, 1f))
        val trailing = text(value, 14f).apply {
            gravity = Gravity.END; maxWidth = dp(210); maxLines = 2
            setTextColor(color(R.color.home_text_secondary))
        }
        trailing.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_function_select, 0)
        trailing.compoundDrawablePadding = dp(4)
        TextViewCompat.setCompoundDrawableTintList(trailing, ContextCompat.getColorStateList(this, R.color.function_text_secondary))
        line.addView(trailing)
        line.setOnClickListener { click(trailing) }
        box.addView(line)
        return trailing
    }
    private fun divider(box: LinearLayout, inset: Int = 20) {
        box.addView(View(this).apply { setBackgroundColor(color(R.color.function_divider)) },
            LinearLayout.LayoutParams(-1, dp(1)).apply { marginStart = dp(inset); marginEnd = dp(20) })
    }
    private fun text(value: String, size: Float) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color(R.color.home_text_primary))
    }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun color(id: Int) = ContextCompat.getColor(this, id)
    private fun accent() = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorPrimary)
}
