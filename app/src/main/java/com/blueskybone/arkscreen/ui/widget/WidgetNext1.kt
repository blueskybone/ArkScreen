package com.blueskybone.arkscreen.ui.widget

import android.content.Context
import com.blueskybone.arkscreen.domain.model.widget.WidgetConfig
import android.widget.RemoteViews
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoType

/** 使用新版模板模型渲染的 1x1 桌面组件。 */
class WidgetNext1 : BaseNextWidgetProvider() {
    override val pendingIntentOffset = 10_000
    override fun defaultTypes(config: WidgetConfig) = listOf(WidgetInfoType.SANITY)

    override fun selectedTypeNames(config: WidgetConfig): List<String> = listOf(config.slot1x1.orEmpty())

    override fun createRemoteViews(
        context: Context,
        appWidgetId: Int,
        data: NextWidgetRenderData,
    ): RemoteViews = RemoteViews(context.packageName, R.layout.widget_next_1x1).apply {
        val item = data.compactItems.getValue(data.items.single().type)
        val color = stateColor(data, item.state)

        setImageViewResource(
            R.id.widget_background,
            backgroundRes(data),
        )
        setImageViewResource(R.id.widget_icon, item.icon.drawableRes)
        setInt(R.id.widget_icon, "setColorFilter", color)
        setInt(R.id.widget_value, "setTextColor", color)
        setInt(R.id.widget_supporting, "setTextColor", palette(data).secondaryText)
        setTextViewText(R.id.widget_value, item.primary)
        setTextViewText(R.id.widget_supporting, item.supporting.orEmpty())
        setContentDescription(
            R.id.widget_root,
            listOfNotNull(item.primary, item.supporting).joinToString(" "),
        )
        setOnClickPendingIntent(R.id.widget_root, refreshIntent(context, appWidgetId))
    }
}
