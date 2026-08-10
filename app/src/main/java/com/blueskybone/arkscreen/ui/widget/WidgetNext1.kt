package com.blueskybone.arkscreen.ui.widget

import android.content.Context
import android.widget.RemoteViews
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.widget.model.WidgetInfoType
import com.blueskybone.arkscreen.ui.widget.model.WidgetBackgroundSize

/** Experimental 1x1 widget backed by the next-generation template model. */
class WidgetNext1 : BaseNextWidgetProvider() {
    override val pendingIntentOffset = 10_000
    override val defaultTypes = listOf(WidgetInfoType.SANITY)

    override fun selectedTypeNames(): List<String> = listOf(templatePrefs.slot1x1.get())

    override fun createRemoteViews(
        context: Context,
        appWidgetId: Int,
        data: NextWidgetRenderData,
    ): RemoteViews = RemoteViews(context.packageName, R.layout.widget_next_1x1).apply {
        val item = data.compactItems.getValue(data.items.single().type)
        val color = stateColor(data, item.state)

        setImageViewResource(
            R.id.widget_background,
            backgroundRes(data, WidgetBackgroundSize.ONE_BY_ONE),
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
