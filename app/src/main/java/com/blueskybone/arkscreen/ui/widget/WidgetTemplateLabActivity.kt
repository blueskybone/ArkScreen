package com.blueskybone.arkscreen.ui.widget

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.core.view.setPadding
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.CachePrefManager
import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.data.local.pref.WidgetTemplatePrefManager
import com.blueskybone.arkscreen.databinding.ActivityWidgetTemplateLabBinding
import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.model.attendance.AccountAttendanceState
import com.blueskybone.arkscreen.domain.repository.AttendanceStateRepository
import com.blueskybone.arkscreen.ui.widget.model.WidgetCompactInfoMapper
import com.blueskybone.arkscreen.ui.widget.model.WidgetBackgroundSize
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoItem
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoMapper
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoState
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoType
import com.blueskybone.arkscreen.ui.widget.model.WidgetVisualStyle
import com.blueskybone.arkscreen.platform.time.TimeUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.getKoin
import kotlinx.coroutines.launch

class WidgetTemplateLabActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWidgetTemplateLabBinding
    private val cache: CachePrefManager by getKoin().inject()
    private val settings: SettingPrefManager by getKoin().inject()
    private val innerPrefs: InnerPrefManager by getKoin().inject()
    private val templatePrefs: WidgetTemplatePrefManager by getKoin().inject()
    private val attendanceStates: AttendanceStateRepository by getKoin().inject()
    private val appClock: AppClock by getKoin().inject()
    private val infoMapper by lazy { WidgetInfoMapper(this, cache, settings, appClock) }
    private val compactInfoMapper by lazy { WidgetCompactInfoMapper(cache, appClock) }
    private var currentAttendanceState: AccountAttendanceState? = null

    private var selectedSize = WidgetPreviewSize.TWO_BY_THREE
    private var selectedStyle = WidgetVisualStyle.CLEAR
    private var layout1x2Mode = Layout1x2Mode.SINGLE
    private var layoutMode = LayoutMode.FOUR

    private val previewItems: Map<WidgetInfoType, WidgetInfoItem>
        get() = infoMapper.mapAll(attendanceState = currentAttendanceState)
            .associateBy(WidgetInfoItem::type)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetTemplateLabBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        restoreSelection()
        bindControls()
        renderPreview()
        loadCurrentAttendanceState()
    }

    private fun loadCurrentAttendanceState() {
        lifecycleScope.launch {
            val uid = cache.accountInfo.get().uid
            currentAttendanceState = if (uid.isBlank()) {
                null
            } else {
                attendanceStates.get(AccountType.SK, uid)
            }
            renderPreview()
        }
    }

    private fun restoreSelection() {
        selectedStyle = WidgetVisualStyle.fromKey(templatePrefs.style.get())
        selectedSize = WidgetPreviewSize.fromKey(templatePrefs.previewSize.get())
        layout1x2Mode = Layout1x2Mode.fromKey(templatePrefs.layout1x2.get())
        layoutMode = LayoutMode.fromKey(templatePrefs.layout2x3.get())
        if (templatePrefs.layout1x2.get() != layout1x2Mode.key) {
            templatePrefs.layout1x2.set(layout1x2Mode.key)
        }
        if (templatePrefs.layout2x3.get() != layoutMode.key) {
            templatePrefs.layout2x3.set(layoutMode.key)
        }

        binding.StyleGroup.check(
            when (selectedStyle) {
                WidgetVisualStyle.CLEAR -> R.id.StyleTransparent
                WidgetVisualStyle.GRAPHITE -> R.id.StyleDark
                WidgetVisualStyle.MIST -> R.id.StyleMist
                WidgetVisualStyle.RHODES -> R.id.StyleRhodes
                WidgetVisualStyle.SKLAND -> R.id.StyleSkland
                WidgetVisualStyle.RHINE -> R.id.StyleRhine
                WidgetVisualStyle.SUI -> R.id.StyleSui
                WidgetVisualStyle.LONETRAIL -> R.id.StyleLonetrail
            }
        )
        binding.SizeGroup.check(selectedSize.buttonId)
        binding.Layout1x2Group.check(layout1x2Mode.chipId)
        binding.LayoutModeGroup.check(layoutMode.chipId)
    }

    private fun bindControls() {
        binding.SizeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            selectedSize = WidgetPreviewSize.fromButtonId(checkedId)
            templatePrefs.previewSize.set(selectedSize.key)
            renderPreview()
        }
        binding.StyleGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedStyle = when (checkedIds.firstOrNull()) {
                R.id.StyleDark -> WidgetVisualStyle.GRAPHITE
                R.id.StyleMist -> WidgetVisualStyle.MIST
                R.id.StyleRhodes -> WidgetVisualStyle.RHODES
                R.id.StyleSkland -> WidgetVisualStyle.SKLAND
                R.id.StyleRhine -> WidgetVisualStyle.RHINE
                R.id.StyleSui -> WidgetVisualStyle.SUI
                R.id.StyleLonetrail -> WidgetVisualStyle.LONETRAIL
                else -> WidgetVisualStyle.CLEAR
            }
            templatePrefs.style.set(selectedStyle.key)
            renderPreview()
            WidgetReceiver.renderCached(this)
        }
        binding.LayoutModeGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            layoutMode = LayoutMode.fromChipId(checkedIds.firstOrNull())
            templatePrefs.layout2x3.set(layoutMode.key)
            renderPreview()
            WidgetReceiver.renderCached(this)
        }
        binding.Layout1x2Group.setOnCheckedStateChangeListener { _, checkedIds ->
            layout1x2Mode = Layout1x2Mode.fromChipId(checkedIds.firstOrNull())
            templatePrefs.layout1x2.set(layout1x2Mode.key)
            renderPreview()
            WidgetReceiver.renderCached(this)
        }
    }

    private fun renderPreview() {
        binding.Layout1x2Section.visibility =
            if (selectedSize == WidgetPreviewSize.ONE_BY_TWO) View.VISIBLE else View.GONE
        binding.LayoutModeSection.visibility =
            if (selectedSize == WidgetPreviewSize.TWO_BY_THREE) View.VISIBLE else View.GONE
        binding.PreviewWidget.layoutParams = binding.PreviewWidget.layoutParams.apply {
            width = dp(selectedSize.width)
            height = dp(selectedSize.height)
        }
        binding.PreviewBackground.visibility = View.VISIBLE
        binding.PreviewBackground.layoutParams = binding.PreviewBackground.layoutParams.apply {
            width = dp(selectedSize.width)
            height = dp(selectedSize.height)
        }
        binding.PreviewBackground.setImageResource(
            selectedStyle.backgroundRes(selectedSize.backgroundSize)
        )
        binding.PreviewWidget.removeAllViews()
        binding.PreviewWidget.orientation = LinearLayout.VERTICAL
        when (selectedSize) {
            WidgetPreviewSize.ONE_BY_ONE -> {
                binding.PreviewWidget.setPadding(dp(3))
                renderOneByOne(selectedItems().single())
            }
            WidgetPreviewSize.ONE_BY_TWO -> {
                if (layout1x2Mode == Layout1x2Mode.DENSE) {
                    binding.PreviewWidget.setPadding(dp(8), dp(2), dp(8), dp(2))
                    renderDenseOneByTwo()
                } else {
                    binding.PreviewWidget.setPadding(dp(14), dp(7), dp(14), dp(5))
                    renderOneByTwo(selectedItems().single())
                }
            }
            WidgetPreviewSize.TWO_BY_TWO -> {
                binding.PreviewWidget.setPadding(dp(14), dp(8), dp(14), dp(8))
                renderTwoByTwo(selectedItems())
            }
            WidgetPreviewSize.TWO_BY_THREE -> {
                binding.PreviewWidget.setPadding(dp(8), dp(5), dp(8), dp(5))
                renderTwoByThree(selectedItems())
            }
        }
        renderContentControls()
    }

    private fun renderOneByOne(item: WidgetInfoItem) {
        binding.PreviewWidget.gravity = Gravity.CENTER
        val compact = compactInfoMapper.map(item)
        binding.PreviewWidget.addView(
            valueText(compact.primary, 18f, stateColor(compact.state)).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
            }
        )
        binding.PreviewWidget.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                addView(iconView(item, 9))
                compact.supporting?.let {
                    addView(detailText(it).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                        ).apply { marginStart = dp(3) }
                    })
                }
            }
        )
        binding.ContentSummary.text = "1 个最简信息槽 · ${item.title}"
    }

    private fun renderOneByTwo(item: WidgetInfoItem) {
        binding.PreviewWidget.gravity = Gravity.CENTER_VERTICAL
        binding.PreviewWidget.addView(previewInfoRow(item, 1f))
        binding.PreviewWidget.addView(footer(item(WidgetInfoType.ATTENDANCE), alignEnd = true))
        binding.ContentSummary.text = "1 个标准信息槽 · ${item.title}"
    }

    private fun renderDenseOneByTwo() {
        val fixed = fullInformationItems()
        binding.PreviewWidget.gravity = Gravity.CENTER_VERTICAL
        binding.PreviewWidget.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                addView(denseSummary("公招", fixed[2]))
                addView(denseSummary("刷新", fixed[3]))
                addView(denseSummary("会客", fixed[5]))
            }
        )
        binding.PreviewWidget.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f,
                )
                addView(densePrimary(fixed[0]))
                addView(densePrimary(fixed[1]))
            }
        )
        binding.PreviewWidget.addView(footer(item(WidgetInfoType.ATTENDANCE), alignEnd = true))
        binding.ContentSummary.text = getString(R.string.widget_dense_layout_fixed)
    }

    private fun denseSummary(label: String, item: WidgetInfoItem) =
        TextView(this).apply {
            text = "$label ${primaryText(item)}"
            textSize = 9f
            setTextColor(stateColor(item.state))
            includeFontPadding = false
            maxLines = 1
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

    private fun densePrimary(item: WidgetInfoItem) =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            addView(valueText(primaryText(item), 17f, stateColor(item.state)))
            item.restTime?.takeIf(String::isNotBlank)?.let {
                addView(detailText(it).apply { textSize = 9f })
            }
        }

    private fun renderTwoByTwo(items: List<WidgetInfoItem>) {
        binding.PreviewWidget.gravity = Gravity.CENTER
        binding.PreviewWidget.addView(footer(item(WidgetInfoType.ATTENDANCE)))
        binding.PreviewWidget.addView(previewInfoRow(items[0], 1f))
        binding.PreviewWidget.addView(divider())
        binding.PreviewWidget.addView(previewInfoRow(items[1], 1f))
        binding.ContentSummary.text = "2 个标准信息槽 · ${items.joinToString("、") { it.title }}"
    }

    private fun renderTwoByThree(selected: List<WidgetInfoItem>) {
        val attendance = item(WidgetInfoType.ATTENDANCE)
        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f,
            )
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
        }
        when (layoutMode) {
            LayoutMode.FOUR -> {
                content.addView(twoItemRow(selected[0], selected[1]))
                content.addView(divider())
                content.addView(twoItemRow(selected[2], selected[3]))
            }
            LayoutMode.FULL -> {
                val fixed = fullInformationItems()
                content.addView(
                    LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0,
                            1.08f,
                        )
                        addView(
                            fullModuleBlock(
                                fixed[0],
                                1.18f,
                                valueSize = 25f,
                                detailBelow = true,
                            )
                        )
                        addView(
                            LinearLayout(this@WidgetTemplateLabActivity).apply {
                                orientation = LinearLayout.VERTICAL
                                gravity = Gravity.CENTER_VERTICAL
                                setPadding(dp(7), 0, dp(2), 0)
                                layoutParams = LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    0.82f,
                                )
                                addView(fullSideItem(fixed[2]))
                                addView(fullSideItem(fixed[3]))
                                addView(fullSideItem(fixed[5]))
                            }
                        )
                    }
                )
                content.addView(
                    LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0,
                            0.92f,
                        )
                        addView(
                            fullModuleBlock(
                                fixed[4],
                                1f,
                                valueSize = 17f,
                                detailBelow = true,
                            )
                        )
                        addView(
                            fullModuleBlock(
                                fixed[1],
                                1f,
                                valueSize = 17f,
                                detailBelow = true,
                            )
                        )
                    }
                )
            }
        }
        left.addView(content)
        binding.PreviewWidget.addView(twoByThreeHeader(attendance))
        binding.PreviewWidget.addView(divider())
        binding.PreviewWidget.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f,
                )
                addView(left)
                addView(actionRail())
            }
        )
        binding.ContentSummary.text = when (layoutMode) {
            LayoutMode.FULL -> getString(R.string.widget_full_layout_fixed)
            LayoutMode.FOUR -> "4 个均衡内容槽"
        }
    }

    private fun renderContentControls() {
        binding.ContentSlots.removeAllViews()
        if (
            (selectedSize == WidgetPreviewSize.TWO_BY_THREE && layoutMode == LayoutMode.FULL) ||
            (selectedSize == WidgetPreviewSize.ONE_BY_TWO && layout1x2Mode == Layout1x2Mode.DENSE)
        ) {
            binding.ContentSlots.visibility = View.GONE
            return
        }
        binding.ContentSlots.visibility = View.VISIBLE
        val selectedTypes = selectedTypes()
        selectedTypes.forEachIndexed { index, type ->
            val currentItem = item(type)
            binding.ContentSlots.addView(
                LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    background = ContextCompat.getDrawable(
                        this@WidgetTemplateLabActivity,
                        R.drawable.bg_widget_content_slot,
                    )
                    isClickable = true
                    isFocusable = true
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        if (index > 0) topMargin = dp(8)
                    }
                    addView(
                        LinearLayout(this@WidgetTemplateLabActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(dp(12), dp(9), dp(10), dp(9))
                            addView(
                                ImageView(this@WidgetTemplateLabActivity).apply {
                                    layoutParams = LinearLayout.LayoutParams(dp(22), dp(22))
                                    setImageResource(type.defaultIcon.drawableRes)
                                    setColorFilter(
                                        ContextCompat.getColor(
                                            this@WidgetTemplateLabActivity,
                                            R.color.on_sec_con,
                                        )
                                    )
                                    contentDescription = currentItem.title
                                }
                            )
                            addView(
                                LinearLayout(this@WidgetTemplateLabActivity).apply {
                                    orientation = LinearLayout.VERTICAL
                                    layoutParams = LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1f,
                                    ).apply { marginStart = dp(11) }
                                    addView(
                                        text(
                                            getString(R.string.widget_slot_index, index + 1),
                                            11f,
                                            false,
                                            ContextCompat.getColor(
                                                this@WidgetTemplateLabActivity,
                                                R.color.on_sec_con,
                                            ),
                                        ).apply { gravity = Gravity.START }
                                    )
                                    addView(
                                        text(
                                            currentItem.title,
                                            14f,
                                            true,
                                            ContextCompat.getColor(
                                                this@WidgetTemplateLabActivity,
                                                R.color.on_sec_con,
                                            ),
                                        ).apply { gravity = Gravity.START }
                                    )
                                }
                            )
                            addView(
                                ImageView(this@WidgetTemplateLabActivity).apply {
                                    layoutParams = LinearLayout.LayoutParams(dp(18), dp(18))
                                    setImageResource(R.drawable.ic_right_arrow)
                                    setColorFilter(
                                        ContextCompat.getColor(
                                            this@WidgetTemplateLabActivity,
                                            R.color.on_sec_con,
                                        )
                                    )
                                    contentDescription = getString(R.string.widget_choose_content)
                                }
                            )
                        }
                    )
                    setOnClickListener { showContentPicker(index, selectedTypes) }
                }
            )
        }
    }

    private fun showContentPicker(slotIndex: Int, current: List<WidgetInfoType>) {
        val candidates = WidgetInfoType.entries.filter { type ->
            type != WidgetInfoType.ATTENDANCE &&
                (type == current[slotIndex] || type !in current)
        }
        val labels = candidates.map { item(it).title }.toTypedArray()
        val checked = candidates.indexOf(current[slotIndex])
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.widget_choose_content)
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                val updated = current.toMutableList().apply { set(slotIndex, candidates[which]) }
                saveSelectedTypes(updated)
                dialog.dismiss()
                renderPreview()
                WidgetReceiver.renderCached(this)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun selectedItems(): List<WidgetInfoItem> =
        selectedTypes().map(::item)

    private fun selectedTypes(): List<WidgetInfoType> {
        val defaults = when (selectedSize) {
            WidgetPreviewSize.ONE_BY_ONE -> listOf(WidgetInfoType.SANITY)
            WidgetPreviewSize.ONE_BY_TWO ->
                if (layout1x2Mode == Layout1x2Mode.DENSE) {
                    fullInformationItems().map(WidgetInfoItem::type)
                } else {
                    listOf(WidgetInfoType.SANITY)
                }
            WidgetPreviewSize.TWO_BY_TWO ->
                listOf(WidgetInfoType.SANITY, WidgetInfoType.TRAINING)
            WidgetPreviewSize.TWO_BY_THREE -> when (layoutMode) {
                LayoutMode.FOUR -> listOf(
                    WidgetInfoType.SANITY,
                    WidgetInfoType.DRONE,
                    WidgetInfoType.RECRUITMENT,
                    WidgetInfoType.TRAINING,
                )
                LayoutMode.FULL -> fullInformationItems().map(WidgetInfoItem::type)
            }
        }
        if (
            (selectedSize == WidgetPreviewSize.TWO_BY_THREE && layoutMode == LayoutMode.FULL) ||
            (selectedSize == WidgetPreviewSize.ONE_BY_TWO && layout1x2Mode == Layout1x2Mode.DENSE)
        ) {
            return defaults
        }
        val stored = selectedPreferenceValue()
        val parsed = stored.split(",").mapNotNull { value ->
            WidgetInfoType.entries.firstOrNull { it.name == value }
        }.filterNot { it == WidgetInfoType.ATTENDANCE }.distinct()
        return if (parsed.size == defaults.size) parsed else defaults
    }

    private fun saveSelectedTypes(types: List<WidgetInfoType>) {
        val value = types.joinToString(",") { it.name }
        when (selectedSize) {
            WidgetPreviewSize.ONE_BY_ONE -> templatePrefs.slot1x1.set(value)
            WidgetPreviewSize.ONE_BY_TWO -> templatePrefs.slot1x2.set(value)
            WidgetPreviewSize.TWO_BY_TWO -> templatePrefs.slots2x2.set(value)
            WidgetPreviewSize.TWO_BY_THREE -> when (layoutMode) {
                LayoutMode.FOUR -> templatePrefs.slots2x3Four.set(value)
                LayoutMode.FULL -> Unit
            }
        }
    }

    private fun selectedPreferenceValue(): String = when (selectedSize) {
        WidgetPreviewSize.ONE_BY_ONE -> templatePrefs.slot1x1.get()
        WidgetPreviewSize.ONE_BY_TWO -> templatePrefs.slot1x2.get()
        WidgetPreviewSize.TWO_BY_TWO -> templatePrefs.slots2x2.get()
        WidgetPreviewSize.TWO_BY_THREE -> when (layoutMode) {
            LayoutMode.FOUR -> templatePrefs.slots2x3Four.get()
            LayoutMode.FULL -> ""
        }
    }

    private fun fullInformationItems(): List<WidgetInfoItem> = listOf(
        item(WidgetInfoType.SANITY),
        item(WidgetInfoType.DRONE),
        item(WidgetInfoType.RECRUITMENT),
        item(WidgetInfoType.RECRUITMENT_REFRESH),
        item(WidgetInfoType.TRAINING),
        item(WidgetInfoType.MEETING),
    )

    private fun footer(attendance: WidgetInfoItem, alignEnd: Boolean = false) =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val footerColor =
                if (
                    settings.backAutoAtd.get() &&
                    attendance.state == WidgetInfoState.COMPLETED
                ) {
                    selectedStyle.palette.completed
                } else {
                    selectedStyle.palette.secondaryText
                }
            gravity = Gravity.CENTER_VERTICAL or if (alignEnd) Gravity.END else Gravity.START
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            setPadding(dp(4), dp(3), dp(4), 0)
            if (settings.backAutoAtd.get()) {
                attendance.value?.takeIf(String::isNotBlank)?.let { value ->
                    addView(
                        detailText(
                            value,
                            footerColor,
                        )
                    )
                    addView(detailText("  ·  ", footerColor))
                }
            }
            val refreshTs = innerPrefs.lastWidgetRefreshTs.get()
            val refreshTime = if (refreshTs > 0L) {
                TimeUtils.getTimeStr(refreshTs * 1000, "HH:mm")
            } else {
                getString(R.string.widget_last_update_time_empty)
            }
            addView(
                detailText(
                    getString(R.string.widget_footer_refresh, refreshTime),
                    footerColor,
                )
            )
        }

    private fun twoByThreeHeader(attendance: WidgetInfoItem) =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            cache.accountInfo.get()
                .takeIf { it.uid.isNotBlank() }
                ?.let { account ->
                    addView(
                        detailText(
                            if (account.official) {
                                getString(R.string.official_server)
                            } else {
                                getString(R.string.bilibili_server)
                            },
                            selectedStyle.palette.secondaryText,
                        )
                    )
                    addView(
                        detailText(account.nickname, selectedStyle.palette.primaryText).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                            ).apply { marginStart = dp(2) }
                        }
                    )
                }
            addView(View(this@WidgetTemplateLabActivity).apply {
                layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
            })
            addView(
                footer(attendance).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    )
                }
            )
        }

    private fun twoItemRow(first: WidgetInfoItem, second: WidgetInfoItem) =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
            addView(infoBlock(first, 1f, horizontal = true))
            addView(verticalDivider())
            addView(infoBlock(second, 1f, horizontal = true))
        }

    private fun fullSideItem(item: WidgetInfoItem) =
        TextView(this).apply {
            text = compactLabel(item)
            textSize = 10f
            setTextColor(stateColor(item.state))
            includeFontPadding = false
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
        }

    private fun fullModuleBlock(
        item: WidgetInfoItem,
        weight: Float,
        valueSize: Float,
        detailBelow: Boolean = false,
    ) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(5), 0, dp(5), 0)
        layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            weight,
        )
        addView(
            LinearLayout(this@WidgetTemplateLabActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                addView(iconView(item, if (valueSize > 20f) 14 else 12))
                addView(titleText(item.title).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply { marginStart = dp(4) }
                })
                item.restTime?.takeIf(String::isNotBlank)?.takeUnless { detailBelow }?.let {
                    addView(detailText(it).apply {
                        gravity = Gravity.END
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f,
                        ).apply { marginStart = dp(4) }
                    })
                }
            }
        )
        addView(valueText(primaryText(item), valueSize, stateColor(item.state)).apply {
            gravity = Gravity.START
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        })
        if (detailBelow) {
            item.restTime?.takeIf(String::isNotBlank)?.let {
                addView(detailText(it).apply {
                    gravity = Gravity.START
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    )
                })
            }
        }
    }

    private fun actionRail() =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                dp(44),
                LinearLayout.LayoutParams.MATCH_PARENT,
            ).apply { marginStart = dp(4) }
            addView(ImageView(this@WidgetTemplateLabActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(42), dp(42))
                setPadding(dp(7))
                setImageResource(R.drawable.ic_refresh)
                setColorFilter(selectedStyle.palette.actionIcon)
                contentDescription = getString(R.string.update_data)
            })
            addView(ImageView(this@WidgetTemplateLabActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(42), dp(42)).apply {
                    topMargin = dp(8)
                }
                setPadding(dp(6))
                setImageResource(R.drawable.ic_starter)
                selectedStyle.palette.starterIcon?.let(::setColorFilter)
                contentDescription = getString(R.string.widget_start_game)
            })
        }

    private fun compactLabel(item: WidgetInfoItem): String =
        "${item.title} ${primaryText(item)}"

    private fun previewInfoRow(
        item: WidgetInfoItem,
        weight: Float? = null,
    ) = LinearLayout(this).apply {
        orientation = if (item.type == WidgetInfoType.ATTENDANCE) {
            LinearLayout.HORIZONTAL
        } else {
            LinearLayout.VERTICAL
        }
        gravity = Gravity.CENTER_VERTICAL or Gravity.START
        layoutParams = if (weight == null) {
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        } else {
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                weight,
            )
        }
        if (item.type == WidgetInfoType.ATTENDANCE) {
            addView(iconView(item, 22))
            addView(
                text(item.title, 12f, false, selectedStyle.palette.secondaryText).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply { marginStart = dp(7) }
                }
            )
            addView(
                valueText(primaryText(item), 24f, stateColor(item.state)).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply { marginStart = dp(8) }
                }
            )
        } else {
            addView(
                LinearLayout(this@WidgetTemplateLabActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL or Gravity.START
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    )
                    addView(
                        iconView(
                            item,
                            if (selectedSize == WidgetPreviewSize.TWO_BY_TWO) 16 else 20,
                        )
                    )
                    addView(
                        text(item.title, 12f, false, selectedStyle.palette.secondaryText).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                            ).apply { marginStart = dp(6) }
                        }
                    )
                    item.restTime
                        ?.takeIf(String::isNotBlank)
                        ?.takeUnless { it == primaryText(item) }
                        ?.let {
                            addView(
                                text(it, 12f, false, selectedStyle.palette.secondaryText).apply {
                                    gravity = Gravity.END
                                    layoutParams = LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1f,
                                    ).apply { marginStart = dp(8) }
                                }
                            )
                        }
                }
            )
            addView(valueText(primaryText(item), 24f, stateColor(item.state)))
        }
    }

    private fun infoBlock(
        item: WidgetInfoItem,
        weight: Float,
        horizontal: Boolean = false,
    ) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(4))
        layoutParams = if (horizontal) {
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
        } else {
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, weight)
        }
        addView(headerView(item))
        addView(
            valueText(
                primaryText(item),
                if (selectedSize == WidgetPreviewSize.TWO_BY_THREE) 19f else 20f,
                stateColor(item.state),
            )
        )
        item.restTime?.takeIf { it.isNotBlank() && it != item.value }?.let {
            addView(detailText(it))
        }
    }

    private fun headerView(item: WidgetInfoItem) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        addView(iconView(item, 12))
        addView(titleText(item.title).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { marginStart = dp(4) }
        })
    }

    private fun iconView(item: WidgetInfoItem, size: Int) = ImageView(this).apply {
        layoutParams = LinearLayout.LayoutParams(dp(size), dp(size))
        setImageResource(item.icon.drawableRes)
        setColorFilter(stateColor(item.state))
        contentDescription = item.title
    }

    private fun primaryText(item: WidgetInfoItem): String =
        item.value?.takeIf(String::isNotBlank)
            ?: item.restTime?.takeIf(String::isNotBlank)
            ?: getString(R.string.widget_no_data)

    private fun stateColor(state: WidgetInfoState): Int =
        selectedStyle.palette.stateColor(state)

    private fun titleText(value: String) =
        text(value, 11f, false, selectedStyle.palette.secondaryText)

    private fun valueText(value: String, size: Float, color: Int) =
        text(value, size, true, color)

    private fun detailText(value: String, color: Int? = null) =
        text(value, 10f, false, color ?: selectedStyle.palette.secondaryText)

    private fun text(value: String, size: Float, bold: Boolean, color: Int) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            includeFontPadding = false
            maxLines = 1
            gravity = Gravity.CENTER
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    private fun divider() = View(this).apply {
        setBackgroundColor(selectedStyle.palette.divider)
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1))
    }

    private fun verticalDivider() = View(this).apply {
        setBackgroundColor(selectedStyle.palette.divider)
        layoutParams = LinearLayout.LayoutParams(dp(1), LinearLayout.LayoutParams.MATCH_PARENT)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun item(type: WidgetInfoType): WidgetInfoItem =
        requireNotNull(previewItems[type]) { "Missing preview item for $type" }

    private enum class LayoutMode(val key: String, val chipId: Int) {
        FULL(WidgetTemplatePrefManager.LAYOUT_FULL, R.id.LayoutFull),
        FOUR(WidgetTemplatePrefManager.LAYOUT_FOUR, R.id.LayoutFour);

        companion object {
            fun fromKey(key: String) = entries.firstOrNull { it.key == key } ?: FOUR
            fun fromChipId(id: Int?) = entries.firstOrNull { it.chipId == id } ?: FOUR
        }
    }

    private enum class Layout1x2Mode(val key: String, val chipId: Int) {
        SINGLE(WidgetTemplatePrefManager.LAYOUT_SINGLE, R.id.LayoutSingle),
        DENSE(WidgetTemplatePrefManager.LAYOUT_DENSE, R.id.LayoutDense);

        companion object {
            fun fromKey(key: String) = entries.firstOrNull { it.key == key } ?: SINGLE
            fun fromChipId(id: Int?) = entries.firstOrNull { it.chipId == id } ?: SINGLE
        }
    }

    private enum class WidgetPreviewSize(
        val key: String,
        val buttonId: Int,
        val width: Int,
        val height: Int,
    ) {
        ONE_BY_ONE(WidgetTemplatePrefManager.SIZE_1X1, R.id.Size1x1, 92, 92),
        ONE_BY_TWO(WidgetTemplatePrefManager.SIZE_1X2, R.id.Size1x2, 190, 92),
        TWO_BY_TWO(WidgetTemplatePrefManager.SIZE_2X2, R.id.Size2x2, 190, 150),
        TWO_BY_THREE(WidgetTemplatePrefManager.SIZE_2X3, R.id.Size2x3, 280, 168);

        val backgroundSize: WidgetBackgroundSize
            get() = when (this) {
                ONE_BY_ONE -> WidgetBackgroundSize.ONE_BY_ONE
                ONE_BY_TWO -> WidgetBackgroundSize.ONE_BY_TWO
                TWO_BY_TWO -> WidgetBackgroundSize.TWO_BY_TWO
                TWO_BY_THREE -> WidgetBackgroundSize.TWO_BY_THREE
            }

        companion object {
            fun fromKey(key: String) = entries.firstOrNull { it.key == key } ?: TWO_BY_THREE
            fun fromButtonId(id: Int) =
                entries.firstOrNull { it.buttonId == id } ?: TWO_BY_THREE
        }
    }
}
