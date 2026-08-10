package com.blueskybone.arkscreen.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.WidgetTemplatePrefManager
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoItem
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoState
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoType
import com.blueskybone.arkscreen.ui.widget.model.WidgetBackgroundSize

/** Experimental 2x3 widget with fixed full-information and selectable compact layouts. */
class WidgetNext4 : BaseNextWidgetProvider() {
    override val pendingIntentOffset = 40_000
    override val defaultTypes: List<WidgetInfoType>
        get() = if (usesFullLayout()) FULL_DEFAULTS else FOUR_DEFAULTS

    override fun selectedTypeNames(): List<String> =
        if (usesFullLayout()) {
            FULL_DEFAULTS.map(WidgetInfoType::name)
        } else {
            templatePrefs.slots2x3Four.get().split(",")
        }

    override fun createRemoteViews(
        context: Context,
        appWidgetId: Int,
        data: NextWidgetRenderData,
    ): RemoteViews =
        if (data.items.size == FULL_DEFAULTS.size) {
            renderFull(context, appWidgetId, data)
        } else {
            renderFour(context, appWidgetId, data)
        }

    private fun renderFour(
        context: Context,
        appWidgetId: Int,
        data: NextWidgetRenderData,
    ) = RemoteViews(context.packageName, R.layout.widget_next_2x3_four).apply {
        setImageViewResource(
            R.id.widget_background,
            backgroundRes(data, WidgetBackgroundSize.TWO_BY_THREE),
        )
        bindBlock(context, data, data.items[0], R.id.widget_icon_1, R.id.widget_title_1, R.id.widget_value_1, R.id.widget_detail_1)
        bindBlock(context, data, data.items[1], R.id.widget_icon_2, R.id.widget_title_2, R.id.widget_value_2, R.id.widget_detail_2)
        bindBlock(context, data, data.items[2], R.id.widget_icon_3, R.id.widget_title_3, R.id.widget_value_3, R.id.widget_detail_3)
        bindBlock(context, data, data.items[3], R.id.widget_icon_4, R.id.widget_title_4, R.id.widget_value_4, R.id.widget_detail_4)
        bindFooter(context, data)
        bindRoot(context, appWidgetId, data)
    }

    private fun renderFull(
        context: Context,
        appWidgetId: Int,
        data: NextWidgetRenderData,
    ) = RemoteViews(context.packageName, R.layout.widget_next_2x3_full).apply {
        setImageViewResource(
            R.id.widget_background,
            backgroundRes(data, WidgetBackgroundSize.TWO_BY_THREE),
        )
        bindBlock(context, data, data.items[0], R.id.widget_icon_1, R.id.widget_title_1, R.id.widget_value_1, R.id.widget_detail_1)
        bindBlock(context, data, data.items[1], R.id.widget_icon_2, R.id.widget_title_2, R.id.widget_value_2, R.id.widget_detail_2)
        bindBlock(context, data, data.items[2], R.id.widget_icon_3, R.id.widget_title_3, R.id.widget_value_3, R.id.widget_detail_3)
        bindCompact(context, data, data.items[3], R.id.widget_compact_1)
        bindCompact(context, data, data.items[4], R.id.widget_compact_2)
        bindCompact(context, data, data.items[5], R.id.widget_compact_3)
        bindFooter(context, data)
        bindRoot(context, appWidgetId, data)
    }

    private fun RemoteViews.bindCompact(
        context: Context,
        data: NextWidgetRenderData,
        item: WidgetInfoItem,
        textId: Int,
    ) {
        setTextViewText(textId, "${item.title} ${primaryText(context, item)}")
        setInt(textId, "setTextColor", stateColor(data, item.state))
    }

    private fun RemoteViews.bindBlock(
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

    private fun RemoteViews.bindFooter(context: Context, data: NextWidgetRenderData) {
        val attendance = data.attendance?.value?.takeIf(String::isNotBlank)
        val footerColor = if (data.attendance?.state == WidgetInfoState.COMPLETED) {
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

        val account = data.cacheAccountInfo
        val accountVisibility = if (account == null) View.GONE else View.VISIBLE
        setViewVisibility(R.id.widget_account_name, accountVisibility)
        setViewVisibility(R.id.widget_account_server, accountVisibility)
        account?.let {
            setTextViewText(R.id.widget_account_name, it.nickname)
            setTextViewText(
                R.id.widget_account_server,
                context.getString(
                    if (it.official) R.string.official_server else R.string.bilibili_server
                ),
            )
        }
        setInt(R.id.widget_account_name, "setTextColor", palette(data).primaryText)
        setInt(R.id.widget_account_server, "setTextColor", palette(data).secondaryText)
    }

    private fun RemoteViews.bindRoot(
        context: Context,
        appWidgetId: Int,
        data: NextWidgetRenderData,
    ) {
        setContentDescription(
            R.id.widget_root,
            data.items.joinToString("，") { "${it.title} ${primaryText(context, it)}" },
        )
        setInt(R.id.widget_action_refresh, "setColorFilter", palette(data).actionIcon)
        palette(data).starterIcon?.let { color ->
            setInt(R.id.widget_action_start, "setColorFilter", color)
        }
        setInt(R.id.widget_header_divider, "setBackgroundColor", palette(data).divider)
        setOnClickPendingIntent(
            R.id.widget_action_refresh,
            refreshIntent(context, appWidgetId),
        )
        setOnClickPendingIntent(
            R.id.widget_action_start,
            startGameIntent(context, appWidgetId),
        )
    }

    private fun startGameIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, Widget4::class.java).apply {
            action = Widget4.START_GAME
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getBroadcast(
            context,
            START_GAME_REQUEST_CODE_OFFSET + appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun usesFullLayout(): Boolean =
        templatePrefs.layout2x3.get() == WidgetTemplatePrefManager.LAYOUT_FULL

    private companion object {
        const val START_GAME_REQUEST_CODE_OFFSET = 50_000
        val FOUR_DEFAULTS = listOf(
            WidgetInfoType.SANITY,
            WidgetInfoType.DRONE,
            WidgetInfoType.RECRUITMENT,
            WidgetInfoType.TRAINING,
        )
        val FULL_DEFAULTS = listOf(
            WidgetInfoType.SANITY,
            WidgetInfoType.DRONE,
            WidgetInfoType.TRAINING,
            WidgetInfoType.RECRUITMENT,
            WidgetInfoType.RECRUITMENT_REFRESH,
            WidgetInfoType.MEETING,
        )
    }
}
