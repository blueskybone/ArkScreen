package com.blueskybone.arkscreen.ui.widget

import android.content.Context
import android.view.ContextThemeWrapper
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.view.setPadding
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.data.local.pref.WidgetTemplatePrefManager
import com.blueskybone.arkscreen.domain.model.widget.WidgetConfig
import com.blueskybone.arkscreen.domain.model.cache.RealtimeCacheSnapshot
import com.blueskybone.arkscreen.domain.model.attendance.AccountAttendanceState
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.ui.widget.model.*
import com.blueskybone.arkscreen.platform.time.TimeUtils

/**
 * Original WidgetTemplateLabActivity preview drawing code, extracted without redesigning its layouts.
 * Inputs now belong to the selected widget; opacity and text scaling are applied after rendering.
 */
internal class WidgetPreviewRenderer(
    context: Context,
    private val config: WidgetConfig,
    sizeIndex: Int,
    private val cache: RealtimeCacheSnapshot,
    private val settings: SettingPrefManager,
    appClock: AppClock,
    attendance: AccountAttendanceState?,
) : ContextThemeWrapper(context, 0) {
    private val selectedSize = WidgetPreviewSize.entries[sizeIndex]
    private val selectedStyle = WidgetVisualStyle.fromKey(config.theme.orEmpty())
    private val layout1x2Mode = if (config.layout1x2 == "dense") Layout1x2Mode.DENSE else Layout1x2Mode.SINGLE
    private val layoutMode = if (config.layout2x3 == "full") LayoutMode.FULL else LayoutMode.FOUR
    private val compactInfoMapper = WidgetCompactInfoMapper(context, cache, appClock)
    private val previewItems = WidgetInfoMapper(this, cache, settings, appClock)
        .mapAll(attendanceState = attendance).associateBy(WidgetInfoItem::type)
    private val frame = FrameLayout(this).apply {
        layoutParams = LinearLayout.LayoutParams(-1, dp(230))
        setPadding(dp(18))
    }
    private val previewBackground = ImageView(this).apply {
        layoutParams = FrameLayout.LayoutParams(dp(selectedSize.width), dp(selectedSize.height), Gravity.CENTER)
        setBackgroundResource(R.drawable.widget_preview_clip)
        clipToOutline = true
        scaleType = ImageView.ScaleType.CENTER_CROP
    }
    private val previewWidget = LinearLayout(this).apply {
        layoutParams = FrameLayout.LayoutParams(dp(selectedSize.width), dp(selectedSize.height), Gravity.CENTER)
        setBackgroundResource(R.drawable.widget_preview_clip)
        clipToOutline = true
        orientation = LinearLayout.VERTICAL
    }
    init { frame.addView(previewBackground); frame.addView(previewWidget) }
    fun render(): View {
        previewWidget.layoutParams = previewWidget.layoutParams.apply {
            width = dp(selectedSize.width)
            height = dp(selectedSize.height)
        }
        previewBackground.visibility = View.VISIBLE
        previewBackground.layoutParams = previewBackground.layoutParams.apply {
            width = dp(selectedSize.width)
            height = dp(selectedSize.height)
        }
        previewBackground.setImageResource(
            selectedStyle.palette.backgroundRes
        )
        previewWidget.removeAllViews()
        previewWidget.orientation = LinearLayout.VERTICAL
        when (selectedSize) {
            WidgetPreviewSize.ONE_BY_ONE -> {
                previewWidget.setPadding(dp(3))
                renderOneByOne(selectedItems().single())
            }
            WidgetPreviewSize.ONE_BY_TWO -> {
                if (layout1x2Mode == Layout1x2Mode.DENSE) {
                    previewWidget.setPadding(dp(8), dp(2), dp(8), dp(2))
                    renderDenseOneByTwo()
                } else {
                    previewWidget.setPadding(dp(14), dp(7), dp(14), dp(5))
                    renderOneByTwo(selectedItems().single())
                }
            }
            WidgetPreviewSize.TWO_BY_TWO -> {
                previewWidget.setPadding(dp(14), dp(8), dp(14), dp(8))
                renderTwoByTwo(selectedItems())
            }
            WidgetPreviewSize.TWO_BY_THREE -> {
                previewWidget.setPadding(dp(8), dp(5), dp(8), dp(5))
                renderTwoByThree(selectedItems())
            }
        }
        previewBackground.alpha = 1f - config.transparency.coerceIn(0, 100) / 100f
        fun scaleText(view: View) {
            if (view is TextView) view.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,
                view.textSize * config.fontScale.coerceIn(0.85f, 1.15f))
            if (view is android.view.ViewGroup) for (i in 0 until view.childCount) scaleText(view.getChildAt(i))
        }
        scaleText(previewWidget)
        return frame
    }

    private fun renderOneByOne(item: WidgetInfoItem) {
        previewWidget.gravity = Gravity.CENTER
        val compact = compactInfoMapper.map(item)
        previewWidget.addView(
            valueText(compact.primary, 18f, stateColor(compact.state)).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
            }
        )
        previewWidget.addView(
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
    }

    private fun renderOneByTwo(item: WidgetInfoItem) {
        previewWidget.gravity = Gravity.CENTER_VERTICAL
        previewWidget.addView(previewInfoRow(item, 1f))
        previewWidget.addView(footer(item(WidgetInfoType.ATTENDANCE), alignEnd = true))
    }

    private fun renderDenseOneByTwo() {
        val fixed = fullInformationItems()
        previewWidget.gravity = Gravity.CENTER_VERTICAL
        previewWidget.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                addView(denseSummary(getString(R.string.recruit_short_label), fixed[2]))
                addView(denseSummary(getString(R.string.refresh), fixed[3]))
                addView(denseSummary(getString(R.string.meeting_short_label), fixed[5]))
            }
        )
        previewWidget.addView(
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
        previewWidget.addView(footer(item(WidgetInfoType.ATTENDANCE), alignEnd = true))
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
        previewWidget.gravity = Gravity.CENTER
        previewWidget.addView(footer(item(WidgetInfoType.ATTENDANCE)))
        previewWidget.addView(previewInfoRow(items[0], 1f))
        previewWidget.addView(divider())
        previewWidget.addView(previewInfoRow(items[1], 1f))
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
                            LinearLayout(this@WidgetPreviewRenderer).apply {
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
        previewWidget.addView(twoByThreeHeader(attendance))
        previewWidget.addView(divider())
        previewWidget.addView(
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
        val stored = when (selectedSize) {
            WidgetPreviewSize.ONE_BY_ONE -> config.slot1x1
            WidgetPreviewSize.ONE_BY_TWO -> config.slot1x2
            WidgetPreviewSize.TWO_BY_TWO -> config.slots2x2
            WidgetPreviewSize.TWO_BY_THREE -> config.slots2x3
        }.orEmpty()
        val parsed = stored.split(",").mapNotNull { value ->
            WidgetInfoType.entries.firstOrNull { it.name == value }
        }.filterNot { it == WidgetInfoType.ATTENDANCE }.distinct()
        return if (parsed.size == defaults.size) parsed else defaults
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
            val refreshTs = cache.apCache.lastSyncTs
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
            cache.accountInfo
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
            addView(View(this@WidgetPreviewRenderer).apply {
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
            LinearLayout(this@WidgetPreviewRenderer).apply {
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
            addView(ImageView(this@WidgetPreviewRenderer).apply {
                layoutParams = LinearLayout.LayoutParams(dp(42), dp(42))
                setPadding(dp(7))
                setImageResource(R.drawable.ic_refresh)
                setColorFilter(selectedStyle.palette.actionIcon)
                contentDescription = getString(R.string.update_data)
            })
            addView(ImageView(this@WidgetPreviewRenderer).apply {
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
                LinearLayout(this@WidgetPreviewRenderer).apply {
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

    private enum class LayoutMode { FULL, FOUR }
    private enum class Layout1x2Mode { SINGLE, DENSE }
    private enum class WidgetPreviewSize(val width: Int, val height: Int) {
        ONE_BY_ONE(92, 92), ONE_BY_TWO(190, 92), TWO_BY_TWO(190, 150), TWO_BY_THREE(280, 168);
    }
}
