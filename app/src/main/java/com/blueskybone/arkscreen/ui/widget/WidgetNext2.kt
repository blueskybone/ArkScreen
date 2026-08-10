package com.blueskybone.arkscreen.ui.widget

import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoType
import com.blueskybone.arkscreen.ui.widget.model.WidgetBackgroundSize

/** 支持单信息布局和固定紧凑布局切换的 1x2 桌面组件。 */
class WidgetNext2 : BaseNextWidgetProvider() {
    override val pendingIntentOffset = 20_000
    override val defaultTypes: List<WidgetInfoType>
        get() = if (usesDenseLayout()) DENSE_DEFAULTS else SINGLE_DEFAULTS

    override fun selectedTypeNames(): List<String> =
        if (usesDenseLayout()) {
            DENSE_DEFAULTS.map(WidgetInfoType::name)
        } else {
            listOf(templatePrefs.slot1x2.get())
        }

    override fun createRemoteViews(
        context: Context,
        appWidgetId: Int,
        data: NextWidgetRenderData,
    ): RemoteViews =
        if (usesDenseLayout()) {
            renderDense(context, appWidgetId, data)
        } else {
            renderSingle(context, appWidgetId, data)
        }

    private fun renderSingle(
        context: Context,
        appWidgetId: Int,
        data: NextWidgetRenderData,
    ) = RemoteViews(context.packageName, R.layout.widget_next_1x2).apply {
        val item = data.items.single()
        val color = stateColor(data, item.state)
        val primary = primaryText(context, item)
        val detail = item.restTime
            ?.takeIf(String::isNotBlank)
            ?.takeUnless { it == primary }
        val attendance = data.attendance?.value?.takeIf(String::isNotBlank)

        setImageViewResource(
            R.id.widget_background,
            backgroundRes(data, WidgetBackgroundSize.ONE_BY_TWO),
        )
        setImageViewResource(R.id.widget_icon, item.icon.drawableRes)
        setInt(R.id.widget_icon, "setColorFilter", color)
        setTextViewText(R.id.widget_title, item.title)
        setInt(R.id.widget_title, "setTextColor", palette(data).secondaryText)
        setTextViewText(R.id.widget_value, primary)
        setInt(R.id.widget_value, "setTextColor", color)
        setInt(R.id.widget_detail, "setTextColor", palette(data).secondaryText)
        setViewVisibility(R.id.widget_detail, if (detail == null) View.GONE else View.VISIBLE)
        detail?.let { setTextViewText(R.id.widget_detail, it) }
        setViewVisibility(
            R.id.widget_footer_attendance,
            if (attendance == null) View.GONE else View.VISIBLE,
        )
        setViewVisibility(
            R.id.widget_footer_separator,
            if (attendance == null) View.GONE else View.VISIBLE,
        )
        attendance?.let { setTextViewText(R.id.widget_footer_attendance, it) }
        val footerColor =
            if (data.attendance?.state == com.blueskybone.arkscreen.ui.widget.model.WidgetInfoState.COMPLETED) {
                palette(data).completed
            } else {
                palette(data).secondaryText
            }
        setInt(
            R.id.widget_footer_attendance,
            "setTextColor",
            footerColor,
        )
        setInt(R.id.widget_footer_separator, "setTextColor", footerColor)
        setInt(R.id.widget_footer_refresh, "setTextColor", footerColor)
        setTextViewText(R.id.widget_footer_refresh, refreshStatusText(context, data))
        setContentDescription(
            R.id.widget_root,
            listOfNotNull(item.title, primary, detail, attendance).joinToString(" "),
        )
        setOnClickPendingIntent(R.id.widget_root, refreshIntent(context, appWidgetId))
    }

    private fun renderDense(
        context: Context,
        appWidgetId: Int,
        data: NextWidgetRenderData,
    ) = RemoteViews(context.packageName, R.layout.widget_next_1x2_dense).apply {
        val items = data.items.associateBy { it.type }
        val sanity = items.getValue(WidgetInfoType.SANITY)
        val drone = items.getValue(WidgetInfoType.DRONE)
        val recruitment = items.getValue(WidgetInfoType.RECRUITMENT)
        val refresh = items.getValue(WidgetInfoType.RECRUITMENT_REFRESH)
        val meeting = items.getValue(WidgetInfoType.MEETING)

        setImageViewResource(
            R.id.widget_background,
            backgroundRes(data, WidgetBackgroundSize.ONE_BY_TWO),
        )
        bindDensePrimary(
            context,
            data,
            sanity,
            R.id.widget_sanity_icon,
            R.id.widget_sanity_value,
            R.id.widget_sanity_detail,
        )
        bindDensePrimary(
            context,
            data,
            drone,
            R.id.widget_drone_icon,
            R.id.widget_drone_value,
            R.id.widget_drone_detail,
        )
        bindSummary(
            context,
            data,
            recruitment,
            R.id.widget_recruitment,
            R.string.widget_dense_recruitment,
        )
        bindSummary(
            context,
            data,
            refresh,
            R.id.widget_recruitment_refresh,
            R.string.widget_dense_refresh,
        )
        bindSummary(
            context,
            data,
            meeting,
            R.id.widget_meeting,
            R.string.widget_dense_meeting,
            preferDetail = true,
        )
        bindDenseFooter(context, data)
        setContentDescription(
            R.id.widget_root,
            data.items.joinToString("，") { "${it.title} ${primaryText(context, it)}" },
        )
        setOnClickPendingIntent(R.id.widget_root, refreshIntent(context, appWidgetId))
    }

    private fun RemoteViews.bindDensePrimary(
        context: Context,
        data: NextWidgetRenderData,
        item: com.blueskybone.arkscreen.ui.widget.model.WidgetInfoItem,
        iconId: Int,
        valueId: Int,
        detailId: Int,
    ) {
        val color = stateColor(data, item.state)
        setImageViewResource(iconId, item.icon.drawableRes)
        setInt(iconId, "setColorFilter", color)
        setTextViewText(valueId, primaryText(context, item))
        setInt(valueId, "setTextColor", color)
        val detail = item.restTime?.takeIf(String::isNotBlank)
        setViewVisibility(detailId, if (detail == null) View.GONE else View.VISIBLE)
        detail?.let { setTextViewText(detailId, it) }
        setInt(detailId, "setTextColor", palette(data).secondaryText)
    }

    private fun RemoteViews.bindSummary(
        context: Context,
        data: NextWidgetRenderData,
        item: com.blueskybone.arkscreen.ui.widget.model.WidgetInfoItem,
        viewId: Int,
        formatRes: Int,
        preferDetail: Boolean = false,
    ) {
        val value = if (preferDetail) {
            item.restTime?.takeIf(String::isNotBlank) ?: primaryText(context, item)
        } else {
            primaryText(context, item)
        }.replace(" ", "")
        setTextViewText(viewId, context.getString(formatRes, value))
        setInt(viewId, "setTextColor", stateColor(data, item.state))
    }

    private fun RemoteViews.bindDenseFooter(context: Context, data: NextWidgetRenderData) {
        val attendance = data.attendance?.value?.takeIf(String::isNotBlank)
        val footerColor =
            if (data.attendance?.state == com.blueskybone.arkscreen.ui.widget.model.WidgetInfoState.COMPLETED) {
                palette(data).completed
            } else {
                palette(data).secondaryText
            }
        setViewVisibility(
            R.id.widget_footer_attendance,
            if (attendance == null) View.GONE else View.VISIBLE,
        )
        setViewVisibility(
            R.id.widget_footer_separator,
            if (attendance == null) View.GONE else View.VISIBLE,
        )
        attendance?.let { setTextViewText(R.id.widget_footer_attendance, it) }
        setTextViewText(R.id.widget_footer_refresh, refreshStatusText(context, data))
        setInt(R.id.widget_footer_attendance, "setTextColor", footerColor)
        setInt(R.id.widget_footer_separator, "setTextColor", footerColor)
        setInt(R.id.widget_footer_refresh, "setTextColor", footerColor)
    }

    private fun usesDenseLayout(): Boolean =
        templatePrefs.layout1x2.get() == com.blueskybone.arkscreen.data.local.pref.WidgetTemplatePrefManager.LAYOUT_DENSE

    private companion object {
        val SINGLE_DEFAULTS = listOf(WidgetInfoType.SANITY)
        val DENSE_DEFAULTS = listOf(
            WidgetInfoType.SANITY,
            WidgetInfoType.DRONE,
            WidgetInfoType.RECRUITMENT,
            WidgetInfoType.RECRUITMENT_REFRESH,
            WidgetInfoType.MEETING,
        )
    }
}
