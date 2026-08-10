package com.blueskybone.arkscreen.ui.widget

import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoItem
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoType
import com.blueskybone.arkscreen.ui.widget.model.WidgetBackgroundSize

/** Experimental 2x2 widget composed of two stacked standard information rows. */
class WidgetNext3 : BaseNextWidgetProvider() {
    override val pendingIntentOffset = 30_000
    override val defaultTypes = listOf(WidgetInfoType.SANITY, WidgetInfoType.TRAINING)

    override fun selectedTypeNames(): List<String> =
        templatePrefs.slots2x2.get().split(",")

    override fun createRemoteViews(
        context: Context,
        appWidgetId: Int,
        data: NextWidgetRenderData,
    ): RemoteViews = RemoteViews(context.packageName, R.layout.widget_next_2x2).apply {
        val items = data.items
        setImageViewResource(
            R.id.widget_background,
            backgroundRes(data, WidgetBackgroundSize.TWO_BY_TWO),
        )
        setInt(R.id.widget_divider, "setBackgroundColor", palette(data).divider)
        bindRow(
            context = context,
            data = data,
            item = items[0],
            iconId = R.id.widget_icon_1,
            titleId = R.id.widget_title_1,
            valueId = R.id.widget_value_1,
            detailId = R.id.widget_detail_1,
        )
        val attendance = data.attendance?.value?.takeIf(String::isNotBlank)
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
        bindRow(
            context = context,
            data = data,
            item = items[1],
            iconId = R.id.widget_icon_2,
            titleId = R.id.widget_title_2,
            valueId = R.id.widget_value_2,
            detailId = R.id.widget_detail_2,
        )
        setContentDescription(
            R.id.widget_root,
            items.joinToString("，") { "${it.title} ${primaryText(context, it)}" },
        )
        setOnClickPendingIntent(R.id.widget_root, refreshIntent(context, appWidgetId))
    }

    private fun RemoteViews.bindRow(
        context: Context,
        data: NextWidgetRenderData,
        item: WidgetInfoItem,
        iconId: Int,
        titleId: Int,
        valueId: Int,
        detailId: Int,
    ) {
        val color = stateColor(data, item.state)
        val primary = primaryText(context, item)
        val detail = item.restTime
            ?.takeIf(String::isNotBlank)
            ?.takeUnless { it == primary }
        setImageViewResource(iconId, item.icon.drawableRes)
        setInt(iconId, "setColorFilter", color)
        setTextViewText(titleId, item.title)
        setInt(titleId, "setTextColor", palette(data).secondaryText)
        setTextViewText(valueId, primary)
        setInt(valueId, "setTextColor", color)
        setInt(detailId, "setTextColor", palette(data).secondaryText)
        setViewVisibility(detailId, if (detail == null) View.GONE else View.VISIBLE)
        detail?.let { setTextViewText(detailId, it) }
    }
}
